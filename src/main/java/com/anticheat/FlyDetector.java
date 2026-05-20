package com.anticheat;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class FlyDetector extends AbstractDetector implements Listener {

    private static final long JUMP_GRACE_PERIOD = 400;
    private static final long GLIDE_GRACE_PERIOD = 600;
    private static final double INSTANT_FLY_THRESHOLD = 1.0;
    private static final double SUSPICIOUS_RISE_THRESHOLD = 0.5;
    private static final double HOVER_Y_THRESHOLD = 0.05;
    private static final int MIN_HOVER_TICKS = 3;
    private static final int MAX_FALL_DISTANCE = 4;

    public FlyDetector(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                       PunishmentManager punishmentManager) {
        super(plugin, playerDataManager, punishmentManager);
    }

    @Override
    public String getName() { return "飞行检测"; }

    @Override
    protected String getConfigKey() { return "fly"; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (shouldSkipCheck(player) || shouldSkipGameMode(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        PlayerData data = getData(player);
        
        if (isInWater(player) || isClimbing(player)) {
            resetFlightData(data);
            return;
        }

        if (player.getAllowFlight() || player.isFlying()) {
            return;
        }

        checkInstantFlight(player, from, to, data);
        checkSuspiciousMovement(player, from, to, data);
        updateFlightData(player, from, to, data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!plugin.getConfigManager().isDetectionFlyPermission()) return;

        Player player = event.getPlayer();
        if (shouldSkipCheck(player) || player.hasPermission("anticheat.fly")) return;
        if (shouldSkipGameMode(player)) return;

        if (event.isFlying() && !player.getAllowFlight()) {
            event.setCancelled(true);
            punishmentManager.handleCheat(player, "飞行权限作弊", 3);
        }
    }

    private void checkInstantFlight(Player player, Location from, Location to, PlayerData data) {
        long now = System.currentTimeMillis();
        
        if (now - data.jumpTime < JUMP_GRACE_PERIOD) {
            return;
        }

        if (now - data.lastGroundTime < GLIDE_GRACE_PERIOD) {
            return;
        }

        double yDiff = to.getY() - from.getY();
        
        if (yDiff > INSTANT_FLY_THRESHOLD) {
            if (!hasFlightEffects(player)) {
                plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() 
                        + " 瞬间上升检测: " + yDiff + " blocks");
                punishmentManager.handleCheat(player, "飞行作弊", 3);
            }
        }
    }

    private void checkSuspiciousMovement(Player player, Location from, Location to, PlayerData data) {
        long now = System.currentTimeMillis();
        
        if (now - data.jumpTime < JUMP_GRACE_PERIOD) {
            return;
        }

        double yDiff = to.getY() - from.getY();
        double xzDiff = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));

        boolean isOnGround = isReallyOnGround(player);
        
        if (!isOnGround && !player.isFlying()) {
            if (yDiff > SUSPICIOUS_RISE_THRESHOLD && xzDiff < 0.3) {
                if (!hasFlightEffects(player)) {
                    data.hoverCount++;
                    
                    if (data.hoverCount >= MIN_HOVER_TICKS) {
                        plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() 
                                + " 悬浮上升检测: " + yDiff + " blocks/tick");
                        punishmentManager.handleCheat(player, "飞行作弊", 2);
                        data.hoverCount = 0;
                    }
                }
            } else if (Math.abs(yDiff) < HOVER_Y_THRESHOLD && xzDiff > 0.1) {
                data.hoverCount++;
                
                if (data.hoverCount >= MIN_HOVER_TICKS * 2) {
                    plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() 
                            + " 水平悬浮检测");
                    punishmentManager.handleCheat(player, "飞行作弊", 2);
                    data.hoverCount = 0;
                }
            } else if (yDiff < -MAX_FALL_DISTANCE) {
                Vector velocity = player.getVelocity();
                if (velocity.getY() > -0.5) {
                    plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() 
                            + " 异常下落检测");
                    punishmentManager.handleCheat(player, "飞行作弊", 2);
                }
                data.hoverCount = 0;
            } else {
                data.hoverCount = Math.max(0, data.hoverCount - 1);
            }
        } else {
            data.hoverCount = 0;
        }
    }

    private void updateFlightData(Player player, Location from, Location to, PlayerData data) {
        boolean isOnGround = isReallyOnGround(player);
        
        if (isOnGround) {
            data.lastGroundTime = System.currentTimeMillis();
            data.jumpTime = System.currentTimeMillis();
            data.hoverCount = 0;
            data.airTime = 0;
        } else {
            data.airTime++;
        }
        
        data.lastYCoord = to.getY();
    }

    private void resetFlightData(PlayerData data) {
        data.hoverCount = 0;
        data.airTime = 0;
        data.jumpTime = System.currentTimeMillis();
        data.lastGroundTime = System.currentTimeMillis();
    }

    private boolean hasFlightEffects(Player player) {
        return player.hasPotionEffect(PotionEffectType.JUMP) ||
               player.hasPotionEffect(PotionEffectType.LEVITATION);
    }

    private boolean isReallyOnGround(Player player) {
        if (player.isOnGround()) return true;

        Location loc = player.getLocation();
        for (int y = 0; y <= 2; y++) {
            Block below = loc.clone().subtract(0, y + 1, 0).getBlock();
            if (below.getType().isSolid()) return true;
        }
        return false;
    }

    private boolean isInWater(Player player) {
        return player.getLocation().getBlock().isLiquid();
    }

    private boolean isClimbing(Player player) {
        Material type = player.getLocation().getBlock().getType();
        return type.toString().contains("LADDER") || type.toString().contains("VINE");
    }
}