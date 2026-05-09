package com.anticheat;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class CacheManager {

    private final AntiCheatPlugin plugin;
    private final Logger logger;

    private volatile ConcurrentHashMap<String, String> banCache = new ConcurrentHashMap<>();
    private volatile Set<String> whitelistedUuids = ConcurrentHashMap.newKeySet();

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
        return banCache.containsKey(uuid);
    }

    public String getBanReason(String uuid) {
        return banCache.getOrDefault(uuid, "作弊行为");
    }

    public void addBanned(String uuid, String reason) {
        banCache.put(uuid, reason != null ? reason : "作弊行为");
    }

    public void removeBanned(String uuid) {
        banCache.remove(uuid);
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
        return banCache.size();
    }

    public int getWhitelistedCount() {
        return whitelistedUuids.size();
    }

    private void refreshBanCache() {
        try {
            List<Map<String, Object>> result = plugin.getHttp().getList("/api/punishment/active");
            if (result != null) {
                ConcurrentHashMap<String, String> newCache = new ConcurrentHashMap<>();
                for (Map<String, Object> item : result) {
                    Object uuidObj = item.get("uuid");
                    if (uuidObj != null) {
                        String uuid = uuidObj.toString();
                        Object reasonObj = item.get("reason");
                        newCache.put(uuid, reasonObj != null ? reasonObj.toString() : "作弊行为");
                    }
                }
                banCache = newCache;
                lastBanCacheRefresh = System.currentTimeMillis();
                logger.fine("[CacheManager] 封禁缓存已刷新，共 " + banCache.size() + " 条记录");
            }
        } catch (Exception e) {
            logger.warning("[CacheManager] 刷新封禁缓存失败: " + e.getMessage());
        }
    }

    private void refreshWhitelistCache() {
        try {
            List<Map<String, Object>> result = plugin.getHttp().getList("/api/whitelist/active");
            if (result != null) {
                Set<String> newSet = ConcurrentHashMap.newKeySet();
                for (Map<String, Object> item : result) {
                    Object uuidObj = item.get("uuid");
                    if (uuidObj != null) {
                        newSet.add(uuidObj.toString());
                    }
                }
                whitelistedUuids = newSet;
                lastWhitelistCacheRefresh = System.currentTimeMillis();
                logger.fine("[CacheManager] 白名单缓存已刷新，共 "
                        + whitelistedUuids.size() + " 条记录");
            }
        } catch (Exception e) {
            logger.warning("[CacheManager] 刷新白名单缓存失败: " + e.getMessage());
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
