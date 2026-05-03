package com.anticheat;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class AntiCheatListener implements Listener {

    private final AntiCheatPlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public AntiCheatListener(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    private static class PlayerData {
        volatile long lastMoveTime;
        Location lastLocation;
        volatile int clickCount;
        volatile long lastClickTime;
        volatile int leftClickCount;
        volatile long lastLeftClickTime;
        volatile float lastYaw;
        volatile float lastPitch;
        volatile long lastAimTime;
        volatile int aimViolationCount;
        final List<Long> attackTimestamps = new CopyOnWriteArrayList<>();
        final Set<UUID> attackedEntities = new CopyOnWriteArraySet<>();
        volatile int totalBlocksBroken;
        volatile int rareOresBroken;
        volatile long miningStartTime;
        volatile int airTime;
        volatile long lastGroundTime;
        volatile double lastYCoord;
        volatile int hoverCount;
        volatile long jumpTime;
        volatile int warningCount;
        volatile String lastCheatType;
        volatile long lastCheatTime;
    }

    private PlayerData getData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, k -> new PlayerData());
    }

    private double getMaxWalkSpeed() {
        return plugin.getConfig().getDouble("thresholds.max-walk-speed", 5.0);
    }

    private double getMaxSprintSpeed() {
        return plugin.getConfig().getDouble("thresholds.max-sprint-speed", 7.0);
    }

    private double getMaxFlySpeed() {
        return plugin.getConfig().getDouble("thresholds.max-fly-speed", 12.0);
    }

    private int getMaxClicksPerSecond() {
        return plugin.getConfig().getInt("thresholds.max-clicks-per-second", 15);
    }

    private float getMaxYawChange() {
        return (float) plugin.getConfig().getDouble("thresholds.max-yaw-change", 160.0);
    }

    private float getMaxPitchChange() {
        return (float) plugin.getConfig().getDouble("thresholds.max-pitch-change", 90.0);
    }

    private int getAimViolationThreshold() {
        return plugin.getConfig().getInt("thresholds.aim-violation-threshold", 3);
    }

    private int getKillAuraTargets() {
        return plugin.getConfig().getInt("thresholds.killaura-targets", 3);
    }

    private long getKillAuraWindow() {
        return plugin.getConfig().getLong("thresholds.killaura-window", 1000);
    }

    private double getXrayRareOreRatio() {
        return plugin.getConfig().getDouble("thresholds.xray-rare-ore-ratio", 0.15);
    }

    private int getXrayMinBlocks() {
        return plugin.getConfig().getInt("thresholds.xray-min-blocks", 50);
    }

    private int getMaxAirTicks() {
        return plugin.getConfig().getInt("thresholds.max-air-ticks", 10);
    }

    private int getMaxHoverCount() {
        return plugin.getConfig().getInt("thresholds.max-hover-count", 5);
    }

    private int getWarningThreshold() {
        return plugin.getConfig().getInt("punishment.warning-threshold", 2);
    }

    private int getTempBanThreshold() {
        return plugin.getConfig().getInt("punishment.temp-ban-threshold", 4);
    }

    private int getPermBanThreshold() {
        return plugin.getConfig().getInt("punishment.perm-ban-threshold", 6);
    }

    private long getTempBanDuration() {
        return plugin.getConfig().getLong("punishment.temp-ban-duration", 86400000);
    }

    private long getCheatCooldown() {
        return plugin.getConfig().getLong("punishment.cooldown", 3000);
    }

    private boolean isDetectionEnabled(String key) {
        return plugin.getConfig().getBoolean("detection." + key, true);
    }

    private boolean isDebugEnabled() {
        return plugin.getConfig().getBoolean("debug", false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (player.hasPermission("anticheat.bypass")) {
            return;
        }

        GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        PlayerData data = getData(playerId);

        if (isDetectionEnabled("fly")) {
            checkFlightCheat(player, from, to, data);
        }

        if (isDetectionEnabled("speed")) {
            checkSpeedCheat(player, to, data);
        }

        if (isDetectionEnabled("aimbot")) {
            checkAimbot(player, from, to, data);
        }

        checkGroundStatus(player, data);

        data.lastMoveTime = System.currentTimeMillis();
        data.lastLocation = to.clone();
        data.lastYaw = to.getYaw();
        data.lastPitch = to.getPitch();
        data.lastYCoord = to.getY();
    }

    private void checkFlightCheat(Player player, Location from, Location to, PlayerData data) {
        UUID playerId = player.getUniqueId();

        if (player.getAllowFlight() || player.isFlying()) {
            return;
        }

        if (isInWater(player) || isClimbing(player)) {
            data.hoverCount = 0;
            data.airTime = 0;
            return;
        }

        boolean isOnGround = isReallyOnGround(player);

        if (isOnGround) {
            data.hoverCount = 0;
            data.airTime = 0;
            data.jumpTime = System.currentTimeMillis();
            return;
        }

        int currentAirTime = data.airTime + 1;
        data.airTime = currentAirTime;

        if (currentAirTime < 5) {
            return;
        }

        double yDiff = to.getY() - from.getY();
        double lastY = data.lastYCoord;

        if (Math.abs(yDiff) < 0.1 && Math.abs(to.getY() - lastY) < 0.15) {
            int hovers = data.hoverCount + 1;
            data.hoverCount = hovers;

            if (isDebugEnabled()) {
                plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                        + " 悬浮检测: " + hovers + "/" + getMaxHoverCount());
            }

            if (hovers >= getMaxHoverCount()) {
                handleCheat(player, "飞行作弊", 2);
                data.hoverCount = 0;
            }
        } else if (yDiff > 0.8) {
            long lastJump = data.jumpTime;
            long now = System.currentTimeMillis();

            if (now - lastJump > 800) {
                if (!player.hasPotionEffect(PotionEffectType.JUMP) &&
                    !player.hasPotionEffect(PotionEffectType.LEVITATION)) {
                    if (isDebugEnabled()) {
                        plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                                + " 异常上升: " + yDiff);
                    }
                    handleCheat(player, "飞行作弊", 2);
                }
            }
            data.hoverCount = 0;
        } else if (yDiff < -0.8) {
            data.hoverCount = 0;
        } else {
            if (data.hoverCount > 0) {
                data.hoverCount--;
            }
        }
    }

    private boolean isReallyOnGround(Player player) {
        if (player.isOnGround()) {
            return true;
        }

        Location loc = player.getLocation();
        for (int y = 0; y <= 2; y++) {
            Block below = loc.clone().subtract(0, y + 1, 0).getBlock();
            if (below.getType().isSolid()) {
                return true;
            }
        }
        return false;
    }

    private void checkSpeedCheat(Player player, Location to, PlayerData data) {
        UUID playerId = player.getUniqueId();

        if (data.lastLocation == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - data.lastMoveTime;

        if (timeDiff < 50 || timeDiff > 1000) {
            return;
        }

        Location lastLoc = data.lastLocation;
        double distance = Math.sqrt(
            Math.pow(to.getX() - lastLoc.getX(), 2) +
            Math.pow(to.getZ() - lastLoc.getZ(), 2)
        );

        double speed = distance / (timeDiff / 1000.0);

        double speedMultiplier = getSpeedMultiplier(player);

        double maxAllowedSpeed;
        if (player.isFlying()) {
            maxAllowedSpeed = getMaxFlySpeed() * speedMultiplier;
        } else if (player.isSprinting()) {
            maxAllowedSpeed = getMaxSprintSpeed() * speedMultiplier;
        } else {
            maxAllowedSpeed = getMaxWalkSpeed() * speedMultiplier;
        }

        if (speed > maxAllowedSpeed * 1.5) {
            if (isDebugEnabled()) {
                plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                        + " 速度: " + String.format("%.2f", speed)
                        + " m/s, 最大允许: " + String.format("%.2f", maxAllowedSpeed));
            }
            handleCheat(player, "速度作弊", 1);
        }
    }

    private void checkAimbot(Player player, Location from, Location to, PlayerData data) {
        float rawYawChange = Math.abs(to.getYaw() - data.lastYaw);
        if (rawYawChange > 180) rawYawChange = 360 - rawYawChange;

        float pitchChange = Math.abs(to.getPitch() - data.lastPitch);

        long currentTime = System.currentTimeMillis();
        long lastTime = data.lastAimTime;

        float maxYaw = getMaxYawChange();
        float maxPitch = getMaxPitchChange();
        int aimThreshold = getAimViolationThreshold();

        if (rawYawChange > maxYaw || pitchChange > maxPitch) {
            int aimViolations = data.aimViolationCount + 1;
            data.aimViolationCount = aimViolations;

            if (aimViolations >= aimThreshold) {
                handleCheat(player, "瞄准辅助", 2);
                data.aimViolationCount = 0;
            }

            data.lastAimTime = currentTime;
        } else if (currentTime - lastTime > 3000) {
            data.aimViolationCount = Math.max(0, data.aimViolationCount - 1);
        }
    }

    private void checkGroundStatus(Player player, PlayerData data) {
        if (isReallyOnGround(player)) {
            data.lastGroundTime = System.currentTimeMillis();
            data.airTime = 0;
            data.jumpTime = System.currentTimeMillis();
        }
    }

    private double getSpeedMultiplier(Player player) {
        double multiplier = 1.0;

        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            multiplier += 0.2 * (amplifier + 1);
        }

        if (player.hasPotionEffect(PotionEffectType.SLOW)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SLOW).getAmplifier();
            multiplier -= 0.15 * (amplifier + 1);
        }

        return Math.max(0.1, multiplier);
    }

    private boolean isInWater(Player player) {
        return player.getLocation().getBlock().isLiquid();
    }

    private boolean isClimbing(Player player) {
        Material type = player.getLocation().getBlock().getType();
        return type.toString().contains("LADDER") || type.toString().contains("VINE");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!isDetectionEnabled("fly_permission")) return;

        Player player = event.getPlayer();

        if (player.hasPermission("anticheat.bypass") || player.hasPermission("anticheat.fly")) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (event.isFlying() && !player.getAllowFlight()) {
            event.setCancelled(true);
            handleCheat(player, "飞行权限作弊", 3);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isDetectionEnabled("autoclick")) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (player.hasPermission("anticheat.bypass")) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            checkRightClick(player, playerId);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (!isDetectionEnabled("autoclick")) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (player.hasPermission("anticheat.bypass")) {
            return;
        }

        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            checkLeftClick(player, playerId);
        }
    }

    private void checkRightClick(Player player, UUID playerId) {
        PlayerData data = getData(playerId);
        long currentTime = System.currentTimeMillis();

        long timeDiff = currentTime - data.lastClickTime;
        if (timeDiff >= 1000) {
            data.clickCount = 0;
        }

        data.clickCount++;
        data.lastClickTime = currentTime;

        if (data.clickCount > getMaxClicksPerSecond()) {
            if (isDebugEnabled()) {
                plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                        + " 右键点击频率: " + data.clickCount + "/秒");
            }
            handleCheat(player, "自动点击作弊", 1);
            data.clickCount = 0;
        }
    }

    private void checkLeftClick(Player player, UUID playerId) {
        PlayerData data = getData(playerId);
        long currentTime = System.currentTimeMillis();

        long timeDiff = currentTime - data.lastLeftClickTime;
        if (timeDiff >= 1000) {
            data.leftClickCount = 0;
        }

        data.leftClickCount++;
        data.lastLeftClickTime = currentTime;

        if (data.leftClickCount > getMaxClicksPerSecond()) {
            if (isDebugEnabled()) {
                plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                        + " 左键点击频率: " + data.leftClickCount + "/秒");
            }
            handleCheat(player, "自动点击作弊", 1);
            data.leftClickCount = 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isDetectionEnabled("killaura")) return;

        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();

        if (player.hasPermission("anticheat.bypass")) {
            return;
        }

        PlayerData data = getData(playerId);
        long currentTime = System.currentTimeMillis();
        long window = getKillAuraWindow();

        data.attackTimestamps.add(currentTime);
        data.attackTimestamps.removeIf(t -> currentTime - t > window);

        if (data.attackTimestamps.isEmpty()) {
            data.attackedEntities.clear();
        }

        data.attackedEntities.add(event.getEntity().getUniqueId());

        int attackCount = data.attackTimestamps.size();
        int targetCount = data.attackedEntities.size();
        int killAuraTargets = getKillAuraTargets();

        if (isDebugEnabled()) {
            plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                    + " 杀戮光环检测: 攻击次数=" + attackCount + ", 目标数=" + targetCount);
        }

        if (attackCount >= getKillAuraTargets() && targetCount >= killAuraTargets) {
            handleCheat(player, "杀戮光环", 3);
            data.attackedEntities.clear();
            data.attackTimestamps.clear();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isDetectionEnabled("xray")) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (player.hasPermission("anticheat.bypass")) {
            return;
        }

        PlayerData data = getData(playerId);
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (data.miningStartTime == 0) {
            data.miningStartTime = System.currentTimeMillis();
        }

        data.totalBlocksBroken++;

        if (isRareOre(blockType)) {
            data.rareOresBroken++;
        }

        int total = data.totalBlocksBroken;
        int rare = data.rareOresBroken;

        if (total >= getXrayMinBlocks()) {
            double ratio = (double) rare / total;

            if (ratio > getXrayRareOreRatio()) {
                handleCheat(player, "透视作弊", 1);

                data.totalBlocksBroken = 0;
                data.rareOresBroken = 0;
                data.miningStartTime = System.currentTimeMillis();
            }
        }
    }

    private boolean isRareOre(Material material) {
        return material == Material.DIAMOND_ORE ||
               material == Material.DEEPSLATE_DIAMOND_ORE ||
               material == Material.EMERALD_ORE ||
               material == Material.DEEPSLATE_EMERALD_ORE ||
               material == Material.ANCIENT_DEBRIS;
    }

    private void handleCheat(Player player, String cheatType, int severity) {
        if (plugin.getCacheManager().isWhitelisted(player.getUniqueId().toString())) {
            return;
        }

        UUID playerId = player.getUniqueId();
        PlayerData data = getData(playerId);
        long currentTime = System.currentTimeMillis();

        if (currentTime - data.lastCheatTime < getCheatCooldown()) {
            return;
        }

        data.warningCount++;
        int warnings = data.warningCount;
        data.lastCheatType = cheatType;
        data.lastCheatTime = currentTime;

        plugin.getLogger().warning(String.format(
            "[AntiCheat] 检测到玩家 %s 疑似 %s，警告次数: %d",
            player.getName(), cheatType, warnings
        ));

        AntiCheatWebSocketClient webSocketClient = plugin.getWebSocketClient();
        if (webSocketClient != null && webSocketClient.isOpen()) {
            String details = String.format("警告次数: %d, 检测时间: %d", warnings, currentTime);
            webSocketClient.sendCheatData(player.getName(), playerId.toString(), cheatType, severity, details);
        }

        int permThreshold = getPermBanThreshold();
        int tempThreshold = getTempBanThreshold();
        int warnThreshold = getWarningThreshold();

        if (warnings >= permThreshold) {
            plugin.getLogger().warning("[AntiCheat] ========== 永久封禁 ==========");
            plugin.getLogger().warning("[AntiCheat] 玩家: " + player.getName() + ", UUID: " + playerId.toString());
            plugin.getLogger().warning("[AntiCheat] 作弊类型: " + cheatType);

            String banReason = cheatType + " (累计" + warnings + "次)";
            plugin.banPlayer(player.getName(), playerId.toString(), "PERMANENT", 0, banReason);
            plugin.incrementKickCount(player.getName(), playerId.toString());

            playerDataMap.remove(playerId);

            player.kickPlayer(
                "§c§l[AntiCheat] §f你已被永久封禁!\n" +
                "§7原因: §f" + banReason + "\n" +
                "§7如有疑问请联系管理员"
            );

            plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() + " 已被永久封禁");
            plugin.getLogger().warning("[AntiCheat] ========== 封禁完成 ==========");
        } else if (warnings >= tempThreshold) {
            plugin.getLogger().warning("[AntiCheat] ========== 临时封禁 ==========");
            plugin.getLogger().warning("[AntiCheat] 玩家: " + player.getName() + ", UUID: " + playerId.toString());
            plugin.getLogger().warning("[AntiCheat] 作弊类型: " + cheatType);

            long durationMs = getTempBanDuration();
            long durationHours = durationMs / 3600000;
            String banReason = cheatType + " (累计" + warnings + "次，临时封禁" + durationHours + "小时)";
            plugin.banPlayer(player.getName(), playerId.toString(), "TEMPORARY", durationMs, banReason);

            player.kickPlayer(
                "§c§l[AntiCheat] §f你已被临时封禁!\n" +
                "§7原因: §f" + banReason + "\n" +
                "§7时长: §f" + durationHours + " 小时\n" +
                "§7再次作弊将被永久封禁!\n" +
                "§7如有疑问请联系管理员"
            );

            plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() + " 已被临时封禁 " + durationHours + " 小时");
            plugin.getLogger().warning("[AntiCheat] ========== 封禁完成 ==========");
        } else {
            int remainingForTemp = tempThreshold - warnings;
            String msg = "§c§l[AntiCheat] §e警告! §f检测到疑似 §c" + cheatType + " §f行为";
            String countMsg = "§c§l[AntiCheat] §f这是第 §e" + warnings + " §f次警告";
            String hint;
            if (warnings >= warnThreshold) {
                hint = "§c§l[AntiCheat] §f再检测到 §e" + remainingForTemp + " §f次将触发§c临时封禁";
            } else {
                hint = "§c§l[AntiCheat] §f再检测到 §e" + remainingForTemp + " §f次将触发§e惩罚";
            }
            player.sendMessage(msg);
            player.sendMessage(countMsg);
            player.sendMessage(hint);
            player.sendTitle("§c§l警告!", "§f检测到疑似作弊行为", 10, 70, 20);
        }
    }

    public void cleanupOfflinePlayers() {
        for (UUID playerId : new HashSet<>(playerDataMap.keySet())) {
            if (org.bukkit.Bukkit.getPlayer(playerId) == null) {
                playerDataMap.remove(playerId);
            }
        }
    }

    public void clearPlayerData(UUID playerId) {
        playerDataMap.remove(playerId);
    }

    public int getWarningCount(UUID playerId) {
        PlayerData data = playerDataMap.get(playerId);
        return data != null ? data.warningCount : 0;
    }

    public String getLastCheatType(UUID playerId) {
        PlayerData data = playerDataMap.get(playerId);
        return data != null ? data.lastCheatType : null;
    }

    public int getTrackedPlayerCount() {
        return playerDataMap.size();
    }
}
