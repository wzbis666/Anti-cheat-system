package com.anticheat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedDetector extends AbstractDetector implements Listener {

    public SpeedDetector(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                         PunishmentManager punishmentManager) {
        super(plugin, playerDataManager, punishmentManager);
    }

    @Override
    public String getName() { return "速度检测"; }

    @Override
    protected String getConfigKey() { return "speed"; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (shouldSkipCheck(player) || shouldSkipGameMode(player)) return;

        Location to = event.getTo();
        if (to == null) return;

        PlayerData data = getData(player);
        checkSpeedCheat(player, to, data);
    }

    private void checkSpeedCheat(Player player, Location to, PlayerData data) {
        if (data.lastLocation == null) return;

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - data.lastMoveTime;

        if (timeDiff < 50 || timeDiff > 1000) return;

        Location lastLoc = data.lastLocation;
        double distance = Math.sqrt(
                Math.pow(to.getX() - lastLoc.getX(), 2) +
                        Math.pow(to.getZ() - lastLoc.getZ(), 2)
        );

        double speed = distance / (timeDiff / 1000.0);
        double speedMultiplier = getSpeedMultiplier(player);

        ConfigManager config = plugin.getConfigManager();
        double maxAllowedSpeed;
        if (player.isFlying()) {
            maxAllowedSpeed = config.getMaxFlySpeed() * speedMultiplier;
        } else if (player.isSprinting()) {
            maxAllowedSpeed = config.getMaxSprintSpeed() * speedMultiplier;
        } else {
            maxAllowedSpeed = config.getMaxWalkSpeed() * speedMultiplier;
        }

        if (speed > maxAllowedSpeed * 1.5) {
            if (isDebugEnabled()) {
                plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                        + " 速度: " + String.format("%.2f", speed)
                        + " m/s, 最大允许: " + String.format("%.2f", maxAllowedSpeed));
            }
            punishmentManager.handleCheat(player, "速度作弊", 1);
        }
    }

    private double getSpeedMultiplier(Player player) {
        double multiplier = 1.0;

        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            PotionEffect effect = player.getPotionEffect(PotionEffectType.SPEED);
            if (effect != null) {
                multiplier += 0.2 * (effect.getAmplifier() + 1);
            }
        }

        if (player.hasPotionEffect(PotionEffectType.SLOW)) {
            PotionEffect effect = player.getPotionEffect(PotionEffectType.SLOW);
            if (effect != null) {
                multiplier -= 0.15 * (effect.getAmplifier() + 1);
            }
        }

        return Math.max(0.1, multiplier);
    }
}
