package com.anticheat;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class ConfigManager {

    private final AntiCheatPlugin plugin;

    private String wsServerHost;
    private int wsServerPort;
    private String apiBaseUrl;
    private String apiKey;

    private double maxWalkSpeed;
    private double maxSprintSpeed;
    private double maxFlySpeed;
    private int maxClicksPerSecond;
    private float maxYawChange;
    private float maxPitchChange;
    private int aimViolationThreshold;
    private int killAuraTargets;
    private long killAuraWindow;
    private double xrayRareOreRatio;
    private int xrayMinBlocks;
    private int maxAirTicks;
    private int maxHoverCount;
    private int warningThreshold;
    private int tempBanThreshold;
    private int permBanThreshold;
    private long tempBanDuration;
    private long cheatCooldown;
    private boolean debug;

    private boolean detectionFly;
    private boolean detectionSpeed;
    private boolean detectionAutoclick;
    private boolean detectionFlyPermission;
    private boolean detectionAimbot;
    private boolean detectionKillaura;
    private boolean detectionXray;

    private String messageWarning;
    private String messageWarningCount;
    private String messageKick;

    private String fallbackStrategy;
    private boolean syncFromServer;

    public ConfigManager(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        wsServerHost = plugin.getConfig().getString("websocket.host", "localhost");
        wsServerPort = plugin.getConfig().getInt("websocket.port", 8080);
        apiBaseUrl = "http://" + wsServerHost + ":" + wsServerPort;
        apiKey = plugin.getConfig().getString("api.key", "");
        syncFromServer = plugin.getConfig().getBoolean("sync.enabled", false);

        loadLocalConfig();

        if (syncFromServer) {
            loadFromServer();
        }
    }

    private void loadLocalConfig() {
        maxWalkSpeed = plugin.getConfig().getDouble("thresholds.max-walk-speed", 5.0);
        maxSprintSpeed = plugin.getConfig().getDouble("thresholds.max-sprint-speed", 7.0);
        maxFlySpeed = plugin.getConfig().getDouble("thresholds.max-fly-speed", 12.0);
        maxClicksPerSecond = plugin.getConfig().getInt("thresholds.max-clicks-per-second", 15);
        maxYawChange = (float) plugin.getConfig().getDouble("thresholds.max-yaw-change", 160.0);
        maxPitchChange = (float) plugin.getConfig().getDouble("thresholds.max-pitch-change", 90.0);
        aimViolationThreshold = plugin.getConfig().getInt("thresholds.aim-violation-threshold", 3);
        killAuraTargets = plugin.getConfig().getInt("thresholds.killaura-targets", 3);
        killAuraWindow = plugin.getConfig().getLong("thresholds.killaura-window", 1000);
        xrayRareOreRatio = plugin.getConfig().getDouble("thresholds.xray-rare-ore-ratio", 0.15);
        xrayMinBlocks = plugin.getConfig().getInt("thresholds.xray-min-blocks", 50);
        maxAirTicks = plugin.getConfig().getInt("thresholds.max-air-ticks", 10);
        maxHoverCount = plugin.getConfig().getInt("thresholds.max-hover-count", 5);
        warningThreshold = plugin.getConfig().getInt("punishment.warning-threshold", 1);
        tempBanThreshold = plugin.getConfig().getInt("punishment.temp-ban-threshold", 2);
        permBanThreshold = plugin.getConfig().getInt("punishment.perm-ban-threshold", 3);
        tempBanDuration = plugin.getConfig().getLong("punishment.temp-ban-duration", 3600000);
        cheatCooldown = plugin.getConfig().getLong("punishment.cooldown", 3000);
        debug = plugin.getConfig().getBoolean("debug", false);

        detectionFly = plugin.getConfig().getBoolean("detection.fly", true);
        detectionSpeed = plugin.getConfig().getBoolean("detection.speed", true);
        detectionAutoclick = plugin.getConfig().getBoolean("detection.autoclick", true);
        detectionFlyPermission = plugin.getConfig().getBoolean("detection.fly_permission", true);
        detectionAimbot = plugin.getConfig().getBoolean("detection.aimbot", true);
        detectionKillaura = plugin.getConfig().getBoolean("detection.killaura", true);
        detectionXray = plugin.getConfig().getBoolean("detection.xray", true);

        messageWarning = plugin.getConfig().getString("messages.warning",
                "§c§l[AntiCheat] §e警告! §f检测到疑似 §c%cheatType% §f行为");
        messageWarningCount = plugin.getConfig().getString("messages.warning-count",
                "§c§l[AntiCheat] §f这是第 §e%count% §f次警告");
        messageKick = plugin.getConfig().getString("messages.kick",
                "§c[AntiCheat] §f检测到多次作弊行为，你已被踢出服务器");

        fallbackStrategy = plugin.getConfig().getString("fallback.strategy", "allow");
    }

    public void loadFromServer() {
        try {
            plugin.getLogger().info("[AntiCheat] 正在从服务器同步配置...");
            Map<String, Object> serverConfig = plugin.getHttp().get("/api/settings/plugin");

            if (serverConfig != null) {
                updateFromServerConfig(serverConfig);
                plugin.getLogger().info("[AntiCheat] 配置同步完成");
            } else {
                plugin.getLogger().warning("[AntiCheat] 无法从服务器获取配置，使用本地配置");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[AntiCheat] 同步配置失败: " + e.getMessage());
        }
    }

    public void updateFromServerConfig(Map<String, Object> config) {
        if (config.containsKey("detect.fly")) {
            detectionFly = Boolean.TRUE.equals(config.get("detect.fly"));
        }
        if (config.containsKey("detect.speed")) {
            detectionSpeed = Boolean.TRUE.equals(config.get("detect.speed"));
        }
        if (config.containsKey("detect.autoclick")) {
            detectionAutoclick = Boolean.TRUE.equals(config.get("detect.autoclick"));
        }
        if (config.containsKey("detect.aimbot")) {
            detectionAimbot = Boolean.TRUE.equals(config.get("detect.aimbot"));
        }
        if (config.containsKey("detect.killaura")) {
            detectionKillaura = Boolean.TRUE.equals(config.get("detect.killaura"));
        }
        if (config.containsKey("detect.xray")) {
            detectionXray = Boolean.TRUE.equals(config.get("detect.xray"));
        }

        if (config.containsKey("threshold.autoclick")) {
            maxClicksPerSecond = toInt(config.get("threshold.autoclick"), 15);
        }
        if (config.containsKey("threshold.speed")) {
            maxWalkSpeed = toDouble(config.get("threshold.speed"), 5.0);
        }
        if (config.containsKey("threshold.violation")) {
            warningThreshold = toInt(config.get("threshold.violation"), 1);
        }

        if (config.containsKey("cheat.fly.warn_threshold")) {
            warningThreshold = toInt(config.get("cheat.fly.warn_threshold"), 1);
        }
        if (config.containsKey("cheat.fly.kick_threshold")) {
            tempBanThreshold = toInt(config.get("cheat.fly.kick_threshold"), 2);
        }
        if (config.containsKey("cheat.fly.temp_ban_threshold")) {
            permBanThreshold = toInt(config.get("cheat.fly.temp_ban_threshold"), 3);
        }

        plugin.getLogger().info("[AntiCheat] 配置已更新: 飞行检测=" + detectionFly
                + ", 速度检测=" + detectionSpeed
                + ", 自动点击检测=" + detectionAutoclick);
    }

    private int toInt(Object value, int defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double toDouble(Object value, double defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean validateApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            plugin.getLogger().severe("===================================================");
            plugin.getLogger().severe("[AntiCheat] 严重安全警告: api.key 未配置！");
            plugin.getLogger().severe("[AntiCheat] 插件将禁用后端通信功能。");
            plugin.getLogger().severe("[AntiCheat] 请在 config.yml 中设置 api.key 并与后端保持一致。");
            plugin.getLogger().severe("===================================================");
            return false;
        }
        if (apiKey.equals("anticheat-plugin-secret-api-key-change-in-production")
                || apiKey.equals("your-api-key")
                || apiKey.equals("changeme")) {
            plugin.getLogger().severe("===================================================");
            plugin.getLogger().severe("[AntiCheat] 严重安全警告: 您正在使用默认或弱 API 密钥！");
            plugin.getLogger().severe("[AntiCheat] 此密钥为公开默认值，不具备任何安全性。");
            plugin.getLogger().severe("[AntiCheat] 请执行以下步骤：");
            plugin.getLogger().severe("[AntiCheat] 1. 生成一个强随机密钥（至少 32 字符）");
            plugin.getLogger().severe("[AntiCheat] 2. 修改 config.yml 中的 api.key");
            plugin.getLogger().severe("[AntiCheat] 3. 确保后端 application.yml 中的 api.key 与此一致");
            plugin.getLogger().severe("[AntiCheat] 4. 重启服务器");
            plugin.getLogger().severe("[AntiCheat] 插件将继续加载，但后端通信将被禁用。");
            plugin.getLogger().severe("===================================================");
            return false;
        }
        if (apiKey.length() < 16) {
            plugin.getLogger().warning("===================================================");
            plugin.getLogger().warning("[AntiCheat] 警告: API 密钥长度不足（" + apiKey.length() + " 字符）");
            plugin.getLogger().warning("[AntiCheat] 建议使用至少 32 字符的强随机密钥。");
            plugin.getLogger().warning("===================================================");
        }
        return true;
    }

    public boolean isDetectionEnabled(String key) {
        switch (key) {
            case "fly": return detectionFly;
            case "speed": return detectionSpeed;
            case "autoclick": return detectionAutoclick;
            case "fly_permission": return detectionFlyPermission;
            case "aimbot": return detectionAimbot;
            case "killaura": return detectionKillaura;
            case "xray": return detectionXray;
            default: return true;
        }
    }

    public boolean isAnyDetectionEnabled() {
        return detectionFly || detectionSpeed || detectionAutoclick
                || detectionFlyPermission || detectionAimbot || detectionKillaura || detectionXray;
    }

    public boolean isSyncFromServer() { return syncFromServer; }

    public String getWsServerHost() { return wsServerHost; }
    public int getWsServerPort() { return wsServerPort; }
    public String getApiBaseUrl() { return apiBaseUrl; }
    public String getApiKey() { return apiKey; }
    public double getMaxWalkSpeed() { return maxWalkSpeed; }
    public double getMaxSprintSpeed() { return maxSprintSpeed; }
    public double getMaxFlySpeed() { return maxFlySpeed; }
    public int getMaxClicksPerSecond() { return maxClicksPerSecond; }
    public float getMaxYawChange() { return maxYawChange; }
    public float getMaxPitchChange() { return maxPitchChange; }
    public int getAimViolationThreshold() { return aimViolationThreshold; }
    public int getKillAuraTargets() { return killAuraTargets; }
    public long getKillAuraWindow() { return killAuraWindow; }
    public double getXrayRareOreRatio() { return xrayRareOreRatio; }
    public int getXrayMinBlocks() { return xrayMinBlocks; }
    public int getMaxAirTicks() { return maxAirTicks; }
    public int getMaxHoverCount() { return maxHoverCount; }
    public int getWarningThreshold() { return warningThreshold; }
    public int getTempBanThreshold() { return tempBanThreshold; }
    public int getPermBanThreshold() { return permBanThreshold; }
    public long getTempBanDuration() { return tempBanDuration; }
    public long getCheatCooldown() { return cheatCooldown; }
    public boolean isDebugEnabled() { return debug; }
    public boolean isDetectionFly() { return detectionFly; }
    public boolean isDetectionSpeed() { return detectionSpeed; }
    public boolean isDetectionAutoclick() { return detectionAutoclick; }
    public boolean isDetectionFlyPermission() { return detectionFlyPermission; }
    public boolean isDetectionAimbot() { return detectionAimbot; }
    public boolean isDetectionKillaura() { return detectionKillaura; }
    public boolean isDetectionXray() { return detectionXray; }
    public String getMessageWarning() { return messageWarning; }
    public String getMessageWarningCount() { return messageWarningCount; }
    public String getMessageKick() { return messageKick; }
    public String getFallbackStrategy() { return fallbackStrategy; }

    public void setDetectionFly(boolean value) { detectionFly = value; }
    public void setDetectionSpeed(boolean value) { detectionSpeed = value; }
    public void setDetectionAutoclick(boolean value) { detectionAutoclick = value; }
    public void setDetectionAimbot(boolean value) { detectionAimbot = value; }
    public void setDetectionKillaura(boolean value) { detectionKillaura = value; }
    public void setDetectionXray(boolean value) { detectionXray = value; }
    public void setMaxClicksPerSecond(int value) { maxClicksPerSecond = value; }
    public void setMaxWalkSpeed(double value) { maxWalkSpeed = value; }
    public void setWarningThreshold(int value) { warningThreshold = value; }
    public void setTempBanThreshold(int value) { tempBanThreshold = value; }
    public void setPermBanThreshold(int value) { permBanThreshold = value; }
}
