package com.anticheat;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class XrayDetector extends AbstractDetector implements Listener {

    public XrayDetector(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                        PunishmentManager punishmentManager) {
        super(plugin, playerDataManager, punishmentManager);
    }

    @Override
    public String getName() { return "透视检测"; }

    @Override
    protected String getConfigKey() { return "xray"; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (shouldSkipCheck(player)) return;

        PlayerData data = getData(player);
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (data.miningStartTime == 0) {
            data.miningStartTime = System.currentTimeMillis();
        }

        data.totalBlocksBroken++;

        if (isRareOre(blockType)) {
            data.rareOresBroken++;
        }

        ConfigManager config = plugin.getConfigManager();
        int total = data.totalBlocksBroken;
        int rare = data.rareOresBroken;

        if (total >= config.getXrayMinBlocks()) {
            double ratio = (double) rare / total;

            if (ratio > config.getXrayRareOreRatio()) {
                punishmentManager.handleCheat(player, "透视作弊", 1);

                data.totalBlocksBroken = 0;
                data.rareOresBroken = 0;
                data.miningStartTime = System.currentTimeMillis();
            }
        }
    }

    private boolean isRareOre(Material material) {
        return material == Material.DIAMOND_ORE
                || material == Material.DEEPSLATE_DIAMOND_ORE
                || material == Material.EMERALD_ORE
                || material == Material.DEEPSLATE_EMERALD_ORE
                || material == Material.ANCIENT_DEBRIS;
    }
}
