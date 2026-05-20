package com.anticheat;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AimbotDetector extends AbstractDetector implements Listener {

    private final ConcurrentHashMap<UUID, Long> lastAttackTime = new ConcurrentHashMap<>();

    private static final long ATTACK_WINDOW_MS = 2000;
    private static final long VIOLATION_DECAY_MS = 5000;
    private static final float NORMAL_MAX_YAW_CHANGE = 120f;
    private static final float NORMAL_MAX_PITCH_CHANGE = 90f;
    private static final float SNAP_THRESHOLD = 80f;
    private static final float HIGH_VELOCITY_THRESHOLD = 2.0f;

    public AimbotDetector(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                          PunishmentManager punishmentManager) {
        super(plugin, playerDataManager, punishmentManager);
    }

    @Override
    public String getName() { return "自瞄检测"; }

    @Override
    protected String getConfigKey() { return "aimbot"; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (shouldSkipCheck(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        PlayerData data = getData(player);
        checkAimbot(player, from, to, data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAttack(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        lastAttackTime.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    private void checkAimbot(Player player, Location from, Location to, PlayerData data) {
        float rawYawChange = Math.abs(to.getYaw() - data.lastYaw);
        if (rawYawChange > 180) rawYawChange = 360 - rawYawChange;

        float pitchChange = Math.abs(to.getPitch() - data.lastPitch);

        long currentTime = System.currentTimeMillis();
        long timeSinceLastAim = currentTime - data.lastAimTime;

        data.lastYaw = to.getYaw();
        data.lastPitch = to.getPitch();

        if (!shouldCheckAimbot(player, data, currentTime)) {
            return;
        }

        boolean isSnap = rawYawChange > SNAP_THRESHOLD && timeSinceLastAim < 200;

        if (isSnap) {
            Entity target = getLookingAtEntity(player);
            if (target != null) {
                data.aimViolationCount++;
                
                if (data.aimViolationCount >= 5) {
                    punishmentManager.handleCheat(player, "瞄准辅助", 2);
                    data.aimViolationCount = 0;
                    plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() + " 因快速瞄准被检测");
                }
                data.lastAimTime = currentTime;
            }
        } else if (rawYawChange > NORMAL_MAX_YAW_CHANGE || pitchChange > NORMAL_MAX_PITCH_CHANGE) {
            if (isLikelyLegitimate(player, rawYawChange, pitchChange)) {
                return;
            }
            
            data.aimViolationCount++;
            
            if (data.aimViolationCount >= 8) {
                punishmentManager.handleCheat(player, "瞄准辅助", 2);
                data.aimViolationCount = 0;
                plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() + " 因异常角度变化被检测");
            }
            data.lastAimTime = currentTime;
        } else if (currentTime - data.lastAimTime > VIOLATION_DECAY_MS) {
            data.aimViolationCount = Math.max(0, data.aimViolationCount - 1);
        }
    }

    private boolean shouldCheckAimbot(Player player, PlayerData data, long currentTime) {
        try {
            java.lang.reflect.Method getGameModeMethod = org.bukkit.entity.Player.class.getMethod("getGameMode");
            Object gameMode = getGameModeMethod.invoke(player);
            if (gameMode.toString().equals("SPECTATOR")) {
                return false;
            }
        } catch (Exception e) {
            // 旧版本 Bukkit，不支持旁观者模式检测
        }

        if (player.isDead()) return false;

        Vector velocity = player.getVelocity();
        double speed = velocity.length();
        if (speed > HIGH_VELOCITY_THRESHOLD) {
            return false;
        }

        Long lastAttack = lastAttackTime.get(player.getUniqueId());
        if (lastAttack != null && currentTime - lastAttack < ATTACK_WINDOW_MS) {
            return true;
        }

        return false;
    }

    private boolean isLikelyLegitimate(Player player, float yawChange, float pitchChange) {
        if (yawChange > 180 && player.isSprinting()) {
            return true;
        }

        if (yawChange > 200 && player.isInsideVehicle()) {
            return true;
        }

        if (pitchChange > 60 && pitchChange < 120) {
            return true;
        }

        return false;
    }

    private Entity getLookingAtEntity(Player player) {
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity.equals(player)) continue;

            Location entityLoc = entity.getLocation().add(0, entity.getHeight() / 2, 0);
            Location eyeLoc = player.getEyeLocation();

            Vector direction = eyeLoc.getDirection().normalize();
            Vector toEntity = entityLoc.subtract(eyeLoc).toVector().normalize();

            double dot = direction.dot(toEntity);
            if (dot > 0.95) {
                return entity;
            }
        }
        return null;
    }
}