package com.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class KillAuraDetector extends AbstractDetector implements Listener {

    public KillAuraDetector(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                            PunishmentManager punishmentManager) {
        super(plugin, playerDataManager, punishmentManager);
    }

    @Override
    public String getName() { return "杀戮光环检测"; }

    @Override
    protected String getConfigKey() { return "killaura"; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;

        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        if (shouldSkipCheck(player)) return;

        PlayerData data = getData(player);
        long currentTime = System.currentTimeMillis();

        ConfigManager config = plugin.getConfigManager();
        long window = config.getKillAuraWindow();
        int killAuraTargets = config.getKillAuraTargets();

        data.attackTimestamps.add(currentTime);
        data.attackTimestamps.removeIf(t -> currentTime - t > window);

        if (data.attackTimestamps.isEmpty()) {
            data.attackedEntities.clear();
        }

        data.attackedEntities.add(event.getEntity().getUniqueId());

        int attackCount = data.attackTimestamps.size();
        int targetCount = data.attackedEntities.size();

        if (isDebugEnabled()) {
            plugin.getLogger().info("[AntiCheat Debug] " + player.getName()
                    + " 杀戮光环检测: 攻击次数=" + attackCount + ", 目标数=" + targetCount);
        }

        if (attackCount >= killAuraTargets && targetCount >= killAuraTargets) {
            punishmentManager.handleCheat(player, "杀戮光环", 3);
            data.attackedEntities.clear();
            data.attackTimestamps.clear();
        }
    }
}
