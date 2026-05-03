package com.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AntiCheatPlugin extends JavaPlugin {
    private static AntiCheatPlugin instance;
    private AntiCheatWebSocketClient webSocketClient;
    private AntiCheatListener antiCheatListener;
    private CacheManager cacheManager;
    private String wsServerHost;
    private int wsServerPort;
    private String apiBaseUrl;
    private String apiKey;
    private HttpHelper http;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadConfig();

        apiKey = getConfig().getString("api.key", "anticheat-plugin-secret-api-key-change-in-production");
        validateApiKey();
        http = new HttpHelper(apiBaseUrl, apiKey, getLogger());

        initWebSocketClient();

        antiCheatListener = new AntiCheatListener(this);
        getServer().getPluginManager().registerEvents(antiCheatListener, this);

        // 每5分钟清理一次离线玩家数据，防止内存泄漏
        new BukkitRunnable() {
            @Override
            public void run() {
                antiCheatListener.cleanupOfflinePlayers();
            }
        }.runTaskTimerAsynchronously(AntiCheatPlugin.this, 6000L, 6000L);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("anticheat").setExecutor(new AntiCheatCommand(this));
        getCommand("ac").setExecutor(new AntiCheatCommand(this));

        // 初始化本地缓存并启动定时刷新
        cacheManager = new CacheManager(this);
        cacheManager.startRefreshTask();

        getLogger().info("AntiCheatPlugin 已成功启动!");
    }

    /**
     * 验证 API 密钥是否已更改默认值，若未更改则打印严重警告。
     */
    private void validateApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            getLogger().severe("===================================================");
            getLogger().severe("[AntiCheat] 严重安全警告: api.key 未配置！");
            getLogger().severe("[AntiCheat] 插件将继续运行，但所有 API 请求将被后端拒绝。");
            getLogger().severe("[AntiCheat] 请在 config.yml 中设置 api.key 并与后端保持一致。");
            getLogger().severe("===================================================");
        } else if (apiKey.equals("anticheat-plugin-secret-api-key-change-in-production")) {
            getLogger().severe("===================================================");
            getLogger().severe("[AntiCheat] 严重安全警告: 您仍在使用默认 API 密钥！");
            getLogger().severe("[AntiCheat] 这等同于公开了您的后端管理权限。");
            getLogger().severe("[AntiCheat] 请立即在 config.yml 中修改 api.key 为一个强随机值，");
            getLogger().severe("[AntiCheat] 并确保后端 application.yml 中的 api.key 与此一致。");
            getLogger().severe("===================================================");
        }
    }

    private void loadConfig() {
        wsServerHost = getConfig().getString("websocket.host", "localhost");
        wsServerPort = getConfig().getInt("websocket.port", 8080);
        apiBaseUrl = "http://" + wsServerHost + ":" + wsServerPort;
    }

    private void initWebSocketClient() {
        try {
            String wsUrl = String.format("ws://%s:%d/ws/cheats", wsServerHost, wsServerPort);
            URI serverUri = new URI(wsUrl);
            webSocketClient = new AntiCheatWebSocketClient(serverUri, apiKey);
            webSocketClient.connect();

            getLogger().info("WebSocket 客户端正在连接: " + wsUrl);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "初始化 WebSocket 客户端失败", e);
        }
    }

    @Override
    public void onDisable() {
        if (webSocketClient != null) {
            try {
                webSocketClient.closeIntentionally();
            } catch (Exception e) {
                getLogger().warning("关闭 WebSocket 连接时出错: " + e.getMessage());
            }
        }

        getLogger().info("AntiCheatPlugin 已关闭!");
    }

    public static AntiCheatPlugin getInstance() {
        return instance;
    }

    public AntiCheatWebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    public boolean isWebSocketConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public HttpHelper getHttp() {
        return http;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public AntiCheatListener getAntiCheatListener() {
        return antiCheatListener;
    }

    /**
     * 检查玩家封禁状态，返回结果封装为 CheckResult。
     * 消除原先 checkBanStatus/getBanReason 两次 HTTP 调用的问题。
     */
    public static class CheckResult {
        public final boolean known;    // 后端是否可达
        public final boolean banned;
        public final String reason;

        CheckResult(boolean known, boolean banned, String reason) {
            this.known = known;
            this.banned = banned;
            this.reason = reason;
        }
    }

    public CheckResult checkBanAndGetReason(String uuid) {
        Map<String, Object> result = http.get("/api/punishment/check/" + uuid);
        if (result != null && result.containsKey("banned")) {
            boolean banned = Boolean.TRUE.equals(result.get("banned"));
            String reason = result.getOrDefault("reason", "作弊行为").toString();
            // 同步到本地缓存
            if (banned) {
                cacheManager.addBanned(uuid, reason);
            } else {
                cacheManager.removeBanned(uuid);
            }
            return new CheckResult(true, banned, reason);
        }
        // 后端不可用时使用本地缓存
        getLogger().warning("[AntiCheat] 无法连接后端，使用本地缓存检查封禁状态: " + uuid);
        if (cacheManager.isBanned(uuid)) {
            return new CheckResult(false, true, cacheManager.getBanReason(uuid));
        }
        return new CheckResult(false, false, "无法验证");
    }

    @Deprecated
    public Boolean checkBanStatus(String uuid) {
        CheckResult r = checkBanAndGetReason(uuid);
        return r.known ? r.banned : null;
    }

    public String getBanReason(String uuid) {
        return checkBanAndGetReason(uuid).reason;
    }

    public void banPlayer(String playerName, String uuid, String punishmentType,
                           long duration, String reason) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<String, Object> data = new HashMap<>();
                data.put("uuid", uuid);
                data.put("playerName", playerName);
                data.put("punishmentType", punishmentType);
                data.put("duration", duration);
                data.put("reason", reason);

                String banTypeDesc = "PERMANENT".equals(punishmentType) ? "永久封禁" : "临时封禁";
                getLogger().info("[AntiCheat] 正在" + banTypeDesc + "玩家: " + playerName
                        + ", UUID: " + uuid + ", 原因: " + reason);

                // 立即更新本地缓存
                cacheManager.addBanned(uuid, reason);

                Map<String, Object> result = http.postWithRetry("/api/punishment/ban", data);

                if (result != null) {
                    getLogger().info("[AntiCheat] 玩家 " + playerName + " 已成功" + banTypeDesc + "，原因: " + reason);
                } else {
                    getLogger().severe("[AntiCheat] " + banTypeDesc + "玩家 " + playerName + " 最终失败，请手动" + banTypeDesc + "！");
                }
            }
        }.runTaskAsynchronously(AntiCheatPlugin.this);
    }

    public boolean isWhitelisted(String uuid) {
        Map<String, Object> result = http.get("/api/whitelist/check/" + uuid);
        if (result != null && result.containsKey("whitelisted")) {
            boolean whitelisted = Boolean.TRUE.equals(result.get("whitelisted"));
            // 同步到本地缓存
            if (whitelisted) {
                cacheManager.addWhitelist(uuid);
            } else {
                cacheManager.removeWhitelist(uuid);
            }
            return whitelisted;
        }
        // 后端不可用时使用本地缓存
        getLogger().warning("[AntiCheat] 无法连接后端，使用本地缓存检查白名单: " + uuid);
        return cacheManager.isWhitelisted(uuid);
    }

    public void incrementKickCount(String playerName, String uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<String, Object> body = new HashMap<>();
                body.put("playerName", playerName);

                Map<String, Object> result = http.post("/api/player/kick/" + uuid, body);
                if (result != null && result.containsKey("kickCount")) {
                    int kickCount = ((Number) result.get("kickCount")).intValue();
                    getLogger().info("[AntiCheat] 踢出次数更新成功: " + kickCount);
                } else {
                    getLogger().warning("[AntiCheat] 更新踢出次数失败");
                }
            }
        }.runTaskAsynchronously(AntiCheatPlugin.this);
    }

    public void getKickCountAsync(String uuid, java.util.function.IntConsumer callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<String, Object> result = http.get("/api/player/kick/" + uuid);
                int kickCount = 0;
                if (result != null && result.containsKey("kickCount")) {
                    kickCount = ((Number) result.get("kickCount")).intValue();
                }
                final int count = kickCount;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.accept(count);
                    }
                }.runTask(AntiCheatPlugin.this);
            }
        }.runTaskAsynchronously(AntiCheatPlugin.this);
    }

    public void resetKickCount(String uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                http.post("/api/player/kick/reset/" + uuid, null);
            }
        }.runTaskAsynchronously(AntiCheatPlugin.this);
    }

    private class PlayerJoinListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();

            if (player.hasPermission("anticheat.bypass")) {
                getLogger().info("[AntiCheat] 玩家 " + player.getName() + " 拥有绕过权限，跳过封禁检查");
                return;
            }

            final String uuid = player.getUniqueId().toString();
            final String playerName = player.getName();

            getLogger().info("[AntiCheat] 玩家 " + playerName + " 加入服务器，UUID: " + uuid + "，正在检查封禁状态...");

            player.sendMessage("§e[AntiCheat] §f正在验证账户状态，请稍候...");

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        CheckResult checkResult = checkBanAndGetReason(uuid);
                        getLogger().info("[AntiCheat] 玩家 " + playerName + " 封禁状态检查结果: known="
                                + checkResult.known + ", banned=" + checkResult.banned);

                        if (!checkResult.known && !checkResult.banned) {
                            // 后端不可用且本地缓存无封禁记录：降级放行
                            getLogger().warning("[AntiCheat] 无法验证玩家 " + playerName
                                    + " 的封禁状态（后端不可用），降级放行");
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (player.isOnline()) {
                                        player.sendMessage("§e[AntiCheat] §f无法验证账户状态，已临时放行"
                                                + "，如有问题请联系管理员");
                                    }
                                }
                            }.runTask(AntiCheatPlugin.this);
                            return;
                        }

                        if (checkResult.banned) {
                            String reason = checkResult.reason;
                            getLogger().warning("[AntiCheat] 玩家 " + playerName + " 已被封禁，正在踢出... 原因: " + reason);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (player.isOnline()) {
                                        player.kickPlayer(
                                            "§c§l[AntiCheat] §f你已被封禁!\n" +
                                            "§7原因: §f" + reason + "\n" +
                                            "§7如有疑问请联系管理员"
                                        );
                                        getLogger().info("[AntiCheat] 玩家 " + playerName + " 已被踢出服务器（封禁）");
                                    } else {
                                        getLogger().info("[AntiCheat] 玩家 " + playerName + " 已离线，无需踢出");
                                    }
                                }
                            }.runTask(AntiCheatPlugin.this);
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (player.isOnline()) {
                                        player.sendMessage("§a[AntiCheat] §f账户验证通过，祝您游戏愉快!");
                                    }
                                }
                            }.runTask(AntiCheatPlugin.this);
                        }
                    } catch (Exception e) {
                        getLogger().severe("[AntiCheat] 检查玩家 " + playerName + " 封禁状态时出错: " + e.getMessage());
                        e.printStackTrace();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    player.kickPlayer(
                                        "§c§l[AntiCheat] §f验证账户状态时发生错误\n" +
                                        "§7请稍后重试或联系管理员"
                                    );
                                }
                            }
                        }.runTask(AntiCheatPlugin.this);
                    }
                }
            }.runTaskAsynchronously(AntiCheatPlugin.this);
        }
    }

    private class PlayerQuitListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();

            if (antiCheatListener != null) {
                antiCheatListener.clearPlayerData(player.getUniqueId());
            }
        }
    }
}
