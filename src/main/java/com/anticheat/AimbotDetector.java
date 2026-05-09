package com.anticheat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class AimbotDetector extends AbstractDetector implements Listener {

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

        Location to = event.getTo();
        if (to == null) return;

        PlayerData data = getData(player);
        checkAimbot(player, to, data);
    }

    private void checkAimbot(Player player, Location to, PlayerData data) {
        float rawYawChange = Math.abs(to.getYaw() - data.lastYaw);
        if (rawYawChange > 180) rawYawChange = 360 - rawYawChange;

        float pitchChange = Math.abs(to.getPitch() - data.lastPitch);

        long currentTime = System.currentTimeMillis();
        long lastTime = data.lastAimTime;

        ConfigManager config = plugin.getConfigManager();
        float maxYaw = config.getMaxYawChange();
        float maxPitch = config.getMaxPitchChange();
        int aimThreshold = config.getAimViolationThreshold();

        if (rawYawChange > maxYaw || pitchChange > maxPitch) {
            int aimViolations = data.aimViolationCount + 1;
            data.aimViolationCount = aimViolations;

            if (aimViolations >= aimThreshold) {
                punishmentManager.handleCheat(player, "瞄准辅助", 2);
                data.aimViolationCount = 0;
            }

            data.lastAimTime = currentTime;
        } else if (currentTime - lastTime > 3000) {
            data.aimViolationCount = Math.max(0, data.aimViolationCount - 1);
        }
    }
}
