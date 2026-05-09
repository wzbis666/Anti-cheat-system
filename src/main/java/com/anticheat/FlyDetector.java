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

public class FlyDetector extends AbstractDetector implements Listener {

    public FlyDetector(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                       PunishmentManager punishmentManager) {
        super(plugin, playerDataManager, punishmentManager);
    }

    @Override
    public String getName() { return "飞行检测"; }

    @Override
    protected String getConfigKey() { return "fly"; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (shouldSkipCheck(player) || shouldSkipGameMode(player)) return;

        Location to = event.getTo();
        if (to == null) return;

        PlayerData data = getData(player);
        checkFlightCheat(player, event.getFrom(), to, data);
        checkGroundStatus(player, data);
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

    private void checkFlightCheat(Player player, Location from, Location to, PlayerData data) {
        if (player.getAllowFlight() || player.isFlying()) return;

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

        ConfigManager config = plugin.getConfigManager();

        if (currentAirTime < config.getMaxAirTicks()) {
            return;
        }

        double yDiff = to.getY() - from.getY();
        double lastY = data.lastYCoord;

        if (Math.abs(yDiff) < 0.1 && Math.abs(to.getY() - lastY) < 0.15) {
            int hovers = data.hoverCount + 1;
            data.hoverCount = hovers;

            if (isDebugEnabled()) {
                plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                        + " 悬浮检测: " + hovers + "/" + config.getMaxHoverCount());
            }

            if (hovers >= config.getMaxHoverCount()) {
                punishmentManager.handleCheat(player, "飞行作弊", 2);
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
                    punishmentManager.handleCheat(player, "飞行作弊", 2);
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

    private void checkGroundStatus(Player player, PlayerData data) {
        if (isReallyOnGround(player)) {
            data.lastGroundTime = System.currentTimeMillis();
            data.airTime = 0;
            data.jumpTime = System.currentTimeMillis();
        }
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
