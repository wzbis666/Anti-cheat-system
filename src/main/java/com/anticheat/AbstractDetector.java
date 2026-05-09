package com.anticheat;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public abstract class AbstractDetector {

    protected final AntiCheatPlugin plugin;
    protected final PlayerDataManager playerDataManager;
    protected final PunishmentManager punishmentManager;

    protected AbstractDetector(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                               PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.punishmentManager = punishmentManager;
    }

    public abstract String getName();

    protected abstract String getConfigKey();

    public boolean isEnabled() {
        return plugin.getConfigManager().isDetectionEnabled(getConfigKey());
    }

    protected boolean shouldSkipCheck(Player player) {
        return player.hasPermission("anticheat.bypass");
    }

    protected boolean shouldSkipGameMode(Player player) {
        GameMode gm = player.getGameMode();
        return gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR;
    }

    protected boolean isDebugEnabled() {
        return plugin.getConfigManager().isDebugEnabled();
    }

    protected PlayerData getData(Player player) {
        return playerDataManager.getData(player);
    }
}
