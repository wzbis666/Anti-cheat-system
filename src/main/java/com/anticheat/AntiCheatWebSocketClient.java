package com.anticheat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AntiCheatWebSocketClient extends WebSocketClient {
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

    public AntiCheatWebSocketClient(URI serverUri, String apiKey) {
        super(serverUri);
        addHeader("X-Api-Key", apiKey);
        setConnectionLostTimeout(30);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        currentDelay = RECONNECT_DELAY_SECONDS;
        intentionalClose = false;
        AntiCheatPlugin.getInstance().getLogger().info("WebSocket连接已建立");

        startHeartbeat();
    }

    @Override
    public void onMessage(String message) {
        if ("ping".equals(message) || "pong".equals(message)) {
            return;
        }

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
                default:
                    AntiCheatPlugin.getInstance().getLogger().info(
                            "收到未处理的服务器消息类型: " + type);
            }
        } catch (Exception e) {
            AntiCheatPlugin.getInstance().getLogger().info("收到服务器消息: " + message);
        }
    }

    private void handleKickCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        String reason = json.has("reason") ? json.get("reason").getAsString() : "被管理员踢出";

        if (uuid.isEmpty()) return;

        Bukkit.getScheduler().runTask(AntiCheatPlugin.getInstance(), () -> {
            Player player = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
            if (player != null && player.isOnline()) {
                player.kickPlayer("§c§l[AntiCheat] §f" + reason);
                AntiCheatPlugin.getInstance().getLogger().info(
                        "已执行服务器踢人命令: " + player.getName() + " - " + reason);
            }
        });
    }

    private void handleBanCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        String playerName = json.has("playerName") ? json.get("playerName").getAsString() : "";
        String reason = json.has("reason") ? json.get("reason").getAsString() : "作弊行为";
        String punishmentType = json.has("punishmentType") ? json.get("punishmentType").getAsString() : "PERMANENT";
        long duration = json.has("duration") ? json.get("duration").getAsLong() : 0;

        if (uuid.isEmpty()) return;

        AntiCheatPlugin.getInstance().getCacheManager().addBanned(uuid, reason);
        AntiCheatPlugin.getInstance().banPlayer(playerName, uuid, punishmentType, duration, reason);

        Bukkit.getScheduler().runTask(AntiCheatPlugin.getInstance(), () -> {
            Player player = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
            if (player != null && player.isOnline()) {
                player.kickPlayer("§c§l[AntiCheat] §f你已被封禁!\n§7原因: §f" + reason);
            }
        });
    }

    private void handleUnbanCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        if (!uuid.isEmpty()) {
            AntiCheatPlugin.getInstance().getCacheManager().removeBanned(uuid);
            AntiCheatPlugin.getInstance().getLogger().info("已执行服务器解封命令: " + uuid);
        }
    }

    private void handleWhitelistAddCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        if (!uuid.isEmpty()) {
            AntiCheatPlugin.getInstance().getCacheManager().addWhitelist(uuid);
            AntiCheatPlugin.getInstance().getLogger().info("已执行服务器白名单添加命令: " + uuid);
        }
    }

    private void handleWhitelistRemoveCommand(JsonObject json) {
        String uuid = json.has("uuid") ? json.get("uuid").getAsString() : "";
        if (!uuid.isEmpty()) {
            AntiCheatPlugin.getInstance().getCacheManager().removeWhitelist(uuid);
            AntiCheatPlugin.getInstance().getLogger().info("已执行服务器白名单移除命令: " + uuid);
        }
    }

    private void handleRefreshCacheCommand() {
        AntiCheatPlugin.getInstance().getCacheManager().forceRefresh();
        AntiCheatPlugin.getInstance().getLogger().info("已执行服务器缓存刷新命令");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        AntiCheatPlugin.getInstance().getLogger().info(
            "WebSocket连接已关闭 (code=" + code + ", reason=" + reason + ", remote=" + remote + ")");

        stopHeartbeat();

        if (!intentionalClose) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        AntiCheatPlugin.getInstance().getLogger().log(Level.SEVERE,
            "WebSocket错误: " + ex.getMessage(), ex);
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            if (isOpen()) {
                try {
                    send("ping");
                } catch (Exception e) {
                    AntiCheatPlugin.getInstance().getLogger().warning(
                        "WebSocket 心跳发送失败: " + e.getMessage());
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
        if (reconnectTask != null && !reconnectTask.isDone()) {
            return;
        }

        AntiCheatPlugin.getInstance().getLogger().info(
            "将在 " + currentDelay + " 秒后尝试重连WebSocket...");

        reconnectTask = scheduler.schedule(() -> {
            try {
                if (!isOpen()) {
                    AntiCheatPlugin.getInstance().getLogger().info("正在重新连接WebSocket...");
                    reconnectBlocking();
                }
            } catch (Exception e) {
                AntiCheatPlugin.getInstance().getLogger().log(Level.WARNING,
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

    public void sendCheatData(String playerName, String uuid, String cheatType, int severity, String details) {
        if (!isOpen()) {
            AntiCheatPlugin.getInstance().getLogger().warning(
                "WebSocket未连接，无法发送作弊数据: " + playerName + " - " + cheatType);
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
