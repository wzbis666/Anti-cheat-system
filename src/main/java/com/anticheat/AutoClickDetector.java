package com.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class AutoClickDetector extends AbstractDetector implements Listener {

    public AutoClickDetector(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                             PunishmentManager punishmentManager) {
        super(plugin, playerDataManager, punishmentManager);
    }

    @Override
    public String getName() { return "自动点击检测"; }

    @Override
    protected String getConfigKey() { return "autoclick"; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (shouldSkipCheck(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            checkClick(player, player.getUniqueId(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (shouldSkipCheck(player)) return;

        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            checkClick(player, player.getUniqueId(), false);
        }
    }

    private void checkClick(Player player, UUID playerId, boolean isRightClick) {
        PlayerData data = playerDataManager.getData(playerId);
        long currentTime = System.currentTimeMillis();
        int maxCPS = plugin.getConfigManager().getMaxClicksPerSecond();

        if (isRightClick) {
            long timeDiff = currentTime - data.lastClickTime;
            if (timeDiff >= 1000) {
                data.clickCount = 0;
            }
            data.clickCount++;
            data.lastClickTime = currentTime;

            if (data.clickCount > maxCPS) {
                if (isDebugEnabled()) {
                    plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                            + " 右键点击频率: " + data.clickCount + "/秒");
                }
                punishmentManager.handleCheat(player, "自动点击作弊", 1);
                data.clickCount = 0;
            }
        } else {
            long timeDiff = currentTime - data.lastLeftClickTime;
            if (timeDiff >= 1000) {
                data.leftClickCount = 0;
            }
            data.leftClickCount++;
            data.lastLeftClickTime = currentTime;

            if (data.leftClickCount > maxCPS) {
                if (isDebugEnabled()) {
                    plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                            + " 左键点击频率: " + data.leftClickCount + "/秒");
                }
                punishmentManager.handleCheat(player, "自动点击作弊", 1);
                data.leftClickCount = 0;
            }
        }
    }
}
