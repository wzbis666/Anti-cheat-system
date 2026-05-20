package com.anticheat;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AntiCheatPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private AntiCheatWebSocketClient webSocketClient;
    private HttpHelper http;
    private CacheManager cacheManager;
    private PlayerDataManager playerDataManager;
    private PunishmentManager punishmentManager;

    private FlyDetector flyDetector;
    private SpeedDetector speedDetector;
    private AimbotDetector aimbotDetector;
    private AutoClickDetector autoClickDetector;
    private KillAuraDetector killAuraDetector;
    private XrayDetector xrayDetector;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();
        configManager.validateApiKey();

        http = new HttpHelper(configManager.getApiBaseUrl(), configManager.getApiKey(), getLogger());

        cacheManager = new CacheManager(this);
        cacheManager.startRefreshTask();

        playerDataManager = new PlayerDataManager();

        punishmentManager = new PunishmentManager(this, playerDataManager, cacheManager);

        initWebSocketClient();
        registerDetectors();
        registerListeners();
        registerCommands();
        startCleanupTask();

        getLogger().info("AntiCheatPlugin 已成功启动!");
    }

    private void initWebSocketClient() {
        try {
            String wsUrl = String.format("ws://%s:%d/ws/cheats",
                    configManager.getWsServerHost(), configManager.getWsServerPort());
            URI serverUri = new URI(wsUrl);
            webSocketClient = new AntiCheatWebSocketClient(serverUri, configManager.getApiKey(), this);
            webSocketClient.connect();
            getLogger().info("WebSocket 客户端正在连接: " + wsUrl);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "初始化 WebSocket 客户端失败", e);
        }
    }

    private void registerDetectors() {
        flyDetector = new FlyDetector(this, playerDataManager, punishmentManager);
        speedDetector = new SpeedDetector(this, playerDataManager, punishmentManager);
        aimbotDetector = new AimbotDetector(this, playerDataManager, punishmentManager);
        autoClickDetector = new AutoClickDetector(this, playerDataManager, punishmentManager);
        killAuraDetector = new KillAuraDetector(this, playerDataManager, punishmentManager);
        xrayDetector = new XrayDetector(this, playerDataManager, punishmentManager);

        getServer().getPluginManager().registerEvents(flyDetector, this);
        getServer().getPluginManager().registerEvents(speedDetector, this);
        getServer().getPluginManager().registerEvents(aimbotDetector, this);
        getServer().getPluginManager().registerEvents(autoClickDetector, this);
        getServer().getPluginManager().registerEvents(killAuraDetector, this);
        getServer().getPluginManager().registerEvents(xrayDetector, this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
    }

    private void registerCommands() {
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("anticheat").setExecutor(new AntiCheatCommand(this));
        getCommand("ac").setExecutor(new AntiCheatCommand(this));
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                playerDataManager.cleanupOfflinePlayers();
            }
        }.runTaskTimerAsynchronously(this, 6000L, 6000L);
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

    public void reload() {
        configManager.load();
        configManager.validateApiKey();

        http = new HttpHelper(configManager.getApiBaseUrl(), configManager.getApiKey(), getLogger());

        if (webSocketClient != null) {
            try {
                webSocketClient.closeIntentionally();
            } catch (Exception e) {
                getLogger().warning("关闭旧 WebSocket 连接时出错: " + e.getMessage());
            }
        }
        initWebSocketClient();

        cacheManager.forceRefresh();

        getLogger().info("[AntiCheat] 配置已完整重新加载");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public AntiCheatWebSocketClient getWebSocketClient() { return webSocketClient; }
    public boolean isWebSocketConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
    public String getApiBaseUrl() { return configManager.getApiBaseUrl(); }
    public HttpHelper getHttp() { return http; }
    public CacheManager getCacheManager() { return cacheManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }

    public FlyDetector getFlyDetector() { return flyDetector; }
    public SpeedDetector getSpeedDetector() { return speedDetector; }
    public AimbotDetector getAimbotDetector() { return aimbotDetector; }
    public AutoClickDetector getAutoClickDetector() { return autoClickDetector; }
    public KillAuraDetector getKillAuraDetector() { return killAuraDetector; }
    public XrayDetector getXrayDetector() { return xrayDetector; }

    public static class CheckResult {
        public final boolean known;
        public final boolean banned;
        public final String reason;
        public final long duration;

        CheckResult(boolean known, boolean banned, String reason, long duration) {
            this.known = known;
            this.banned = banned;
            this.reason = reason;
            this.duration = duration;
        }
    }

    public CheckResult checkBanAndGetReason(String uuid) {
        Map<String, Object> result = http.get("/api/punishment/check/" + uuid);
        if (result != null && result.containsKey("banned")) {
            boolean banned = Boolean.TRUE.equals(result.get("banned"));
            String reason = result.getOrDefault("reason", "作弊行为").toString();
            long duration = result.containsKey("duration") ? ((Number) result.get("duration")).longValue() : 0;
            if (banned) {
                cacheManager.addBanned(uuid, reason);
            } else {
                cacheManager.removeBanned(uuid);
            }
            return new CheckResult(true, banned, reason, duration);
        }
        getLogger().warning("[AntiCheat] 无法连接后端，使用本地缓存检查封禁状态: " + uuid);
        if (cacheManager.isBanned(uuid)) {
            return new CheckResult(false, true, cacheManager.getBanReason(uuid), 0);
        }
        return new CheckResult(false, false, "无法验证", 0);
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

                if (!cacheManager.isBanned(uuid)) {
                    cacheManager.addBanned(uuid, reason);
                }

                Map<String, Object> result = http.postWithRetry("/api/punishment/ban", data);

                if (result != null) {
                    getLogger().info("[AntiCheat] 玩家 " + playerName + " 已成功"
                            + banTypeDesc + "，原因: " + reason);
                } else {
                    getLogger().severe("[AntiCheat] " + banTypeDesc + "玩家 " + playerName
                            + " 最终失败，请手动" + banTypeDesc + "！");
                }
            }
        }.runTaskAsynchronously(this);
    }

    public boolean isWhitelisted(String uuid) {
        Map<String, Object> result = http.get("/api/whitelist/check/" + uuid);
        if (result != null && result.containsKey("whitelisted")) {
            boolean whitelisted = Boolean.TRUE.equals(result.get("whitelisted"));
            if (whitelisted) {
                cacheManager.addWhitelist(uuid);
            } else {
                cacheManager.removeWhitelist(uuid);
            }
            return whitelisted;
        }
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
        }.runTaskAsynchronously(this);
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
        }.runTaskAsynchronously(this);
    }

    public void resetKickCount(String uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                http.post("/api/player/kick/reset/" + uuid, null);
            }
        }.runTaskAsynchronously(this);
    }
}
