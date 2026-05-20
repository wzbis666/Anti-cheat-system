package com.anticheat;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PunishmentManager {

    private final AntiCheatPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final CacheManager cacheManager;

    private final ConcurrentHashMap<UUID, Long> banningPlayers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ReentrantLock> playerLocks = new ConcurrentHashMap<>();

    private static final long DETECTION_COOLDOWN_MS = 1000;

    public PunishmentManager(AntiCheatPlugin plugin, PlayerDataManager playerDataManager,
                             CacheManager cacheManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.cacheManager = cacheManager;
    }

    public void handleCheat(Player player, String cheatType, int severity) {
        if (cacheManager.isWhitelisted(player.getUniqueId().toString())) {
            return;
        }

        UUID playerId = player.getUniqueId();
        ReentrantLock lock = playerLocks.computeIfAbsent(playerId, k -> new ReentrantLock());
        
        if (!lock.tryLock()) {
            plugin.getLogger().info("[AntiCheat] 玩家 " + player.getName() + " 已有检测处理中，跳过");
            return;
        }

        try {
            PlayerData data = playerDataManager.getData(playerId);
            long currentTime = System.currentTimeMillis();

            if (currentTime - data.lastCheatTime < DETECTION_COOLDOWN_MS) {
                plugin.getLogger().info("[AntiCheat] 玩家 " + player.getName() + " 仍在检测冷却中 (" 
                        + (DETECTION_COOLDOWN_MS - (currentTime - data.lastCheatTime)) + "ms)");
                return;
            }

            data.lastCheatTime = currentTime;

            plugin.getLogger().warning(String.format(
                    "[AntiCheat] 检测到玩家 %s 疑似 %s",
                    player.getName(), cheatType));

            if (!data.hasReceivedWarning) {
                executeWarning(player, cheatType);
                data.hasReceivedWarning = true;
                plugin.getLogger().info("[AntiCheat] 玩家 " + player.getName() + " 首次作弊，已发送警告");
            } else {
                executeBan(player, playerId, cheatType);
                plugin.getLogger().info("[AntiCheat] 玩家 " + player.getName() + " 再次作弊，执行封禁");
            }
        } finally {
            lock.unlock();
        }
    }

    private void executeWarning(Player player, String cheatType) {
        plugin.getLogger().warning("[AntiCheat] ========== 警告 ==========");
        plugin.getLogger().warning("[AntiCheat] 玩家: " + player.getName());
        plugin.getLogger().warning("[AntiCheat] 作弊类型: " + cheatType);

        player.sendMessage("§c§l[AntiCheat] §e警告! §f检测到疑似 §c" + cheatType + " §f行为");
        player.sendMessage("§7[AntiCheat] §f停止违规行为，否则将被封禁");
        player.sendTitle("§c§l警告!", "§f检测到疑似作弊行为", 5, 40, 15);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);

        plugin.getLogger().warning("[AntiCheat] 已向玩家 " + player.getName() + " 发送警告");
        plugin.getLogger().warning("[AntiCheat] ========== 警告完成 ==========");

        reportCheatData(player, player.getUniqueId(), cheatType, 1, "警告");
    }

    private void executeBan(Player player, UUID playerId, String cheatType) {
        String uuidStr = playerId.toString();

        if (cacheManager.isBanned(uuidStr)) {
            plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() + " 已经被封禁，跳过重复封禁");
            return;
        }

        if (banningPlayers.putIfAbsent(playerId, System.currentTimeMillis()) != null) {
            plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() + " 正在封禁中，跳过重复封禁");
            return;
        }

        cacheManager.addBanned(uuidStr, cheatType + " (警告后再次检测到)");

        plugin.getLogger().warning("[AntiCheat] ========== 封禁玩家 ==========");
        plugin.getLogger().warning("[AntiCheat] 玩家: " + player.getName() + ", UUID: " + playerId);
        plugin.getLogger().warning("[AntiCheat] 作弊类型: " + cheatType);

        String banReason = cheatType + " (警告后再次检测到)";
        plugin.banPlayer(player.getName(), uuidStr, "PERMANENT", 0, banReason);
        plugin.incrementKickCount(player.getName(), uuidStr);

        playerDataManager.removeData(playerId);

        player.sendTitle("§c§l已封禁!", "§f检测到作弊行为", 5, 30, 10);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
        
        player.kickPlayer(
                "§c§l[AntiCheat] §f你已被封禁!\n"
                        + "§7原因: §f" + banReason + "\n"
                        + "§7检测冷却: 1秒\n"
                        + "§7如有疑问请联系管理员");

        reportCheatData(player, playerId, cheatType, 3, "警告后再次检测到，已封禁");

        plugin.getLogger().warning("[AntiCheat] 玩家 " + player.getName() + " 已被封禁");
        plugin.getLogger().warning("[AntiCheat] ========== 封禁完成 ==========");

        banningPlayers.remove(playerId);
    }

    private void reportCheatData(Player player, UUID playerId, String cheatType,
                                  int severity, String details) {
        AntiCheatWebSocketClient webSocketClient = plugin.getWebSocketClient();

        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.sendCheatData(player.getName(), playerId.toString(),
                    cheatType, severity, details);
        } else {
            sendCheatViaRest(player.getName(), playerId.toString(), cheatType, severity, details);
        }
    }

    private void sendCheatViaRest(String playerName, String uuid, String cheatType,
                                   int severity, String details) {
        try {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("playerName", playerName);
            data.put("uuid", uuid);
            data.put("cheatType", cheatType);
            data.put("severity", severity);
            data.put("details", details);
            data.put("detectionTime", System.currentTimeMillis());
            plugin.getHttp().post("/api/cheat/add", data);
        } catch (Exception e) {
            plugin.getLogger().warning("[AntiCheat] REST API 回退发送作弊记录失败: " + e.getMessage());
        }
    }
}