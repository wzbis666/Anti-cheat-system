package com.anticheat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AntiCheatWebSocketClient extends WebSocketClient {

    private final AntiCheatPlugin plugin;
    private final Gson gson = new Gson();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "WebSocket-Reconnect");
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> reconnectTask;
    private ScheduledFuture<?> heartbeatTask;
    private static final int RECONNECT_DELAY_SECONDS = 5;
    private static final int MAX_RECONNECT_DELAY_SECONDS = 60;
    private static final int HEARTBEAT_INTERVAL_SECONDS = 15;
    private int currentDelay = RECONNECT_DELAY_SECONDS;
    private volatile boolean intentionalClose = false;

    public AntiCheatWebSocketClient(URI serverUri, String apiKey, AntiCheatPlugin plugin) {
        super(serverUri);
        this.plugin = plugin;
        addHeader("X-Api-Key", apiKey);
        setConnectionLostTimeout(30);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        currentDelay = RECONNECT_DELAY_SECONDS;
        intentionalClose = false;
        plugin.getLogger().info("WebSocket连接已建立");
        startHeartbeat();
    }

    @Override
    public void onMessage(String message) {
        if ("ping".equals(message) || "pong".equals(message)) return;

        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.has("type") ? json.get("type").getAsString() : "";

            switch (type) {
                case "KICK_PLAYER":
                    handleKickCommand(json);
                    break;
                case "BAN_PLAYER":
                    handleBanCommand(json);
                    break;
                case "REFRESH_CACHE":
                    handleRefreshCacheCommand();
                    break;
                case "UNBAN_PLAYER":
                    handleUnbanCommand(json);
                    break;
                case "WHITELIST_ADD":
                    handleWhitelistAddCommand(json);
                    break;
                case "WHITELIST_REMOVE":
                    handleWhitelistRemoveCommand(json);
                    break;
                case "CONFIG_UPDATE":
                    handleConfigUpdateCommand(json);
                    break;
                case "CONFIG_SYNC":
                    handleConfigSyncCommand();
                    break;
                default:
                    plugin.getLogger().info("收到未处理的服务器消息类型: " + type);
            }
        } catch (Exception e) {
            plugin.getLogger().info("收到服务器消息: " + message);
        }
    }

    private void handleKickCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        String reason = json.has("reason") ? json.get("reason").getAsString() : "被管理员踢出";

        if (uuid.isEmpty()) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Player player = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
                if (player != null && player.isOnline()) {
                    player.kickPlayer("§c§l[AntiCheat] §f" + reason);
                    plugin.getLogger().info("已执行服务器踢人命令: " + player.getName()
                            + " - " + reason);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[AntiCheat] 无效的UUID格式: " + uuid);
            }
        });
    }

    private void handleBanCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        String playerName = json.has("playerName") ? json.get("playerName").getAsString() : "";
        String reason = json.has("reason") ? json.get("reason").getAsString() : "作弊行为";
        String punishmentType = json.has("punishmentType")
                ? json.get("punishmentType").getAsString() : "PERMANENT";
        long duration = json.has("duration") ? json.get("duration").getAsLong() : 0;

        if (uuid.isEmpty()) return;

        plugin.getCacheManager().addBanned(uuid, reason);
        plugin.banPlayer(playerName, uuid, punishmentType, duration, reason);

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Player player = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
                if (player != null && player.isOnline()) {
                    player.kickPlayer("§c§l[AntiCheat] §f你已被封禁!\n§7原因: §f" + reason);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[AntiCheat] 无效的UUID格式: " + uuid);
            }
        });
    }

    private void handleUnbanCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        if (!uuid.isEmpty()) {
            plugin.getCacheManager().removeBanned(uuid);
            plugin.getLogger().info("已执行服务器解封命令: " + uuid);
        }
    }

    private void handleWhitelistAddCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        if (!uuid.isEmpty()) {
            plugin.getCacheManager().addWhitelist(uuid);
            plugin.getLogger().info("已执行服务器白名单添加命令: " + uuid);
        }
    }

    private void handleWhitelistRemoveCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        if (!uuid.isEmpty()) {
            plugin.getCacheManager().removeWhitelist(uuid);
            plugin.getLogger().info("已执行服务器白名单移除命令: " + uuid);
        }
    }

    private void handleRefreshCacheCommand() {
        plugin.getCacheManager().forceRefresh();
        plugin.getLogger().info("已执行服务器缓存刷新命令");
    }

    private void handleConfigUpdateCommand(JsonObject json) {
        try {
            JsonObject configData = json.getAsJsonObject("config");
            java.util.Map<String, Object> configMap = new java.util.HashMap<>();

            for (String key : configData.keySet()) {
                com.google.gson.JsonElement element = configData.get(key);
                if (element.isJsonPrimitive()) {
                    com.google.gson.JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if (primitive.isBoolean()) {
                        configMap.put(key, primitive.getAsBoolean());
                    } else if (primitive.isNumber()) {
                        try {
                            configMap.put(key, primitive.getAsInt());
                        } catch (Exception e) {
                            configMap.put(key, primitive.getAsDouble());
                        }
                    } else {
                        configMap.put(key, primitive.getAsString());
                    }
                }
            }

            plugin.getConfigManager().updateFromServerConfig(configMap);
            plugin.getLogger().info("[AntiCheat] 已接收并应用配置更新");
        } catch (Exception e) {
            plugin.getLogger().warning("[AntiCheat] 处理配置更新失败: " + e.getMessage());
        }
    }

    private void handleConfigSyncCommand() {
        plugin.getLogger().info("[AntiCheat] 收到配置同步指令，从服务器重新加载配置...");
        plugin.getConfigManager().loadFromServer();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        plugin.getLogger().info("WebSocket连接已关闭 (code=" + code
                + ", reason=" + reason + ", remote=" + remote + ")");
        stopHeartbeat();
        if (!intentionalClose) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "WebSocket错误: " + ex.getMessage(), ex);
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            if (isOpen()) {
                try {
                    send("ping");
                } catch (Exception e) {
                    plugin.getLogger().warning("WebSocket 心跳发送失败: " + e.getMessage());
                }
            }
        }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null && !heartbeatTask.isDone()) {
            heartbeatTask.cancel(false);
        }
    }

    private void scheduleReconnect() {
        if (reconnectTask != null && !reconnectTask.isDone()) return;

        plugin.getLogger().info("将在 " + currentDelay + " 秒后尝试重连WebSocket...");

        reconnectTask = scheduler.schedule(() -> {
            try {
                if (!isOpen()) {
                    plugin.getLogger().info("正在重新连接WebSocket...");
                    reconnectBlocking();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "WebSocket重连失败: " + e.getMessage());
                currentDelay = Math.min(currentDelay * 2, MAX_RECONNECT_DELAY_SECONDS);
                scheduleReconnect();
            }
        }, currentDelay, TimeUnit.SECONDS);
    }

    public void closeIntentionally() {
        intentionalClose = true;
        stopHeartbeat();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        try {
            closeBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void sendCheatData(String playerName, String uuid, String cheatType,
                               int severity, String details) {
        if (!isOpen()) {
            plugin.getLogger().warning("WebSocket未连接，无法发送作弊数据: "
                    + playerName + " - " + cheatType);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHEAT_DETECTED");
        data.put("playerName", playerName);
        data.put("uuid", uuid);
        data.put("cheatType", cheatType);
        data.put("severity", severity);
        data.put("details", details);
        data.put("timestamp", System.currentTimeMillis());

        String json = gson.toJson(data);
        this.send(json);
    }
}
