package com.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerData getData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, k -> new PlayerData());
    }

    public PlayerData getData(Player player) {
        return getData(player.getUniqueId());
    }

    public void removeData(UUID playerId) {
        playerDataMap.remove(playerId);
    }

    public void cleanupOfflinePlayers() {
        for (UUID playerId : new HashSet<>(playerDataMap.keySet())) {
            if (Bukkit.getPlayer(playerId) == null) {
                playerDataMap.remove(playerId);
            }
        }
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

    public void updateMovementState(Player player, Location to) {
        PlayerData data = getData(player.getUniqueId());
        data.lastMoveTime = System.currentTimeMillis();
        data.lastLocation = to.clone();
        data.lastYaw = to.getYaw();
        data.lastPitch = to.getPitch();
        data.lastYCoord = to.getY();
    }
}
