package com.anticheat;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class CacheManager {

    private final AntiCheatPlugin plugin;
    private final Logger logger;

    private final Set<String> bannedUuids = ConcurrentHashMap.newKeySet();
    private final Map<String, String> banReasons = new ConcurrentHashMap<>();
    private final Set<String> whitelistedUuids = ConcurrentHashMap.newKeySet();

    private volatile long lastBanCacheRefresh = 0;
    private volatile long lastWhitelistCacheRefresh = 0;
    private static final long CACHE_REFRESH_INTERVAL = 60000;

    public CacheManager(AntiCheatPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void startRefreshTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                refreshBanCache();
                refreshWhitelistCache();
            }
        }.runTaskTimerAsynchronously(plugin, 600L, 600L);
    }

    public boolean isBanned(String uuid) {
        return bannedUuids.contains(uuid);
    }

    public String getBanReason(String uuid) {
        return banReasons.getOrDefault(uuid, "作弊行为");
    }

    public void addBanned(String uuid, String reason) {
        bannedUuids.add(uuid);
        banReasons.put(uuid, reason);
    }

    public void removeBanned(String uuid) {
        bannedUuids.remove(uuid);
        banReasons.remove(uuid);
    }

    public boolean isWhitelisted(String uuid) {
        return whitelistedUuids.contains(uuid);
    }

    public void addWhitelist(String uuid) {
        whitelistedUuids.add(uuid);
    }

    public void removeWhitelist(String uuid) {
        whitelistedUuids.remove(uuid);
    }

    public int getBannedCount() {
        return bannedUuids.size();
    }

    public int getWhitelistedCount() {
        return whitelistedUuids.size();
    }

    private void refreshBanCache() {
        try {
            List<Map<String, Object>> result = plugin.getHttp().getList("/api/punishment/active");
            if (result != null) {
                bannedUuids.clear();
                banReasons.clear();

                for (Map<String, Object> item : result) {
                    Object uuidObj = item.get("uuid");
                    if (uuidObj != null) {
                        String uuid = uuidObj.toString();
                        bannedUuids.add(uuid);
                        Object reasonObj = item.get("reason");
                        banReasons.put(uuid, reasonObj != null ? reasonObj.toString() : "作弊行为");
                    }
                }

                lastBanCacheRefresh = System.currentTimeMillis();
                logger.fine("[CacheManager] 封禁缓存已刷新，共 " + bannedUuids.size() + " 条记录");
            }
        } catch (Exception e) {
            logger.fine("[CacheManager] 刷新封禁缓存失败: " + e.getMessage());
        }
    }

    private void refreshWhitelistCache() {
        try {
            List<Map<String, Object>> result = plugin.getHttp().getList("/api/whitelist/active");
            if (result != null) {
                whitelistedUuids.clear();

                for (Map<String, Object> item : result) {
                    Object uuidObj = item.get("uuid");
                    if (uuidObj != null) {
                        whitelistedUuids.add(uuidObj.toString());
                    }
                }

                lastWhitelistCacheRefresh = System.currentTimeMillis();
                logger.fine("[CacheManager] 白名单缓存已刷新，共 " + whitelistedUuids.size() + " 条记录");
            }
        } catch (Exception e) {
            logger.fine("[CacheManager] 刷新白名单缓存失败: " + e.getMessage());
        }
    }

    public boolean isCacheFresh() {
        long now = System.currentTimeMillis();
        return (now - lastBanCacheRefresh < CACHE_REFRESH_INTERVAL * 3)
                && (now - lastWhitelistCacheRefresh < CACHE_REFRESH_INTERVAL * 3);
    }

    public void forceRefresh() {
        refreshBanCache();
        refreshWhitelistCache();
    }
}
