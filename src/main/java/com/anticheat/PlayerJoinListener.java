package com.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final AntiCheatPlugin plugin;

    public PlayerJoinListener(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("anticheat.bypass")) {
            plugin.getLogger().info("[AntiCheat] 玩家 " + player.getName()
                    + " 拥有绕过权限，跳过封禁检查");
            return;
        }

        final String uuid = player.getUniqueId().toString();
        final String playerName = player.getName();

        plugin.getLogger().info("[AntiCheat] 玩家 " + playerName + " 加入服务器，UUID: "
                + uuid + "，正在检查封禁状态...");

        player.sendMessage("§e[AntiCheat] §f正在验证账户状态，请稍候...");

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    AntiCheatPlugin.CheckResult checkResult = plugin.checkBanAndGetReason(uuid);
                    plugin.getLogger().info("[AntiCheat] 玩家 " + playerName
                            + " 封禁状态检查结果: known=" + checkResult.known
                            + ", banned=" + checkResult.banned);

                    if (!checkResult.known && !checkResult.banned) {
                        handleBackendUnavailable(player, playerName, uuid);
                        return;
                    }

                    if (checkResult.banned) {
                        handleBannedPlayer(player, playerName, checkResult.reason);
                    } else {
                        handleCleanPlayer(player);
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("[AntiCheat] 检查玩家 " + playerName
                            + " 封禁状态时出错: " + e.getMessage());
                    e.printStackTrace();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.isOnline()) {
                                player.kickPlayer(
                                        "§c§l[AntiCheat] §f验证账户状态时发生错误\n"
                                                + "§7请稍后重试或联系管理员");
                            }
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void handleBackendUnavailable(Player player, String playerName, String uuid) {
        String strategy = plugin.getConfigManager().getFallbackStrategy();

        switch (strategy) {
            case "deny":
                plugin.getLogger().warning("[AntiCheat] 后端不可用，拒绝策略: 踢出玩家 " + playerName);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            player.kickPlayer("§c§l[AntiCheat] §f无法验证账户状态，请稍后重试");
                        }
                    }
                }.runTask(plugin);
                break;

            case "whitelist_only":
                if (plugin.getCacheManager().isWhitelisted(uuid)) {
                    plugin.getLogger().info("[AntiCheat] 后端不可用，白名单玩家 "
                            + playerName + " 放行");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.isOnline()) {
                                player.sendMessage("§e[AntiCheat] §f白名单验证通过，祝您游戏愉快!");
                            }
                        }
                    }.runTask(plugin);
                } else {
                    plugin.getLogger().warning("[AntiCheat] 后端不可用，非白名单玩家 "
                            + playerName + " 被拒绝");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.isOnline()) {
                                player.kickPlayer(
                                        "§c§l[AntiCheat] §f无法验证账户状态，仅白名单玩家可入");
                            }
                        }
                    }.runTask(plugin);
                }
                break;

            case "allow":
            default:
                plugin.getLogger().warning("[AntiCheat] 无法验证玩家 " + playerName
                        + " 的封禁状态（后端不可用），降级放行");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            player.sendMessage("§e[AntiCheat] §f无法验证账户状态，已临时放行，如有问题请联系管理员");
                        }
                    }
                }.runTask(plugin);
                break;
        }
    }

    private void handleBannedPlayer(Player player, String playerName, String reason) {
        plugin.getLogger().warning("[AntiCheat] 玩家 " + playerName
                + " 已被封禁，正在踢出... 原因: " + reason);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.kickPlayer(
                            "§c§l[AntiCheat] §f你已被封禁!\n"
                                    + "§7原因: §f" + reason + "\n"
                                    + "§7如有疑问请联系管理员");
                    plugin.getLogger().info("[AntiCheat] 玩家 " + playerName
                            + " 已被踢出服务器（封禁）");
                } else {
                    plugin.getLogger().info("[AntiCheat] 玩家 " + playerName
                            + " 已离线，无需踢出");
                }
            }
        }.runTask(plugin);
    }

    private void handleCleanPlayer(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.sendMessage("§a[AntiCheat] §f账户验证通过，祝您游戏愉快!");
                }
            }
        }.runTask(plugin);
    }
}
