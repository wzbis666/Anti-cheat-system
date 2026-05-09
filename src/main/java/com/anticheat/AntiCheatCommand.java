package com.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AntiCheatCommand implements CommandExecutor {

    private final AntiCheatPlugin plugin;

    public AntiCheatCommand(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("anticheat.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令");
            return true;
        }

        if (args.length == 0) {
            sendStatus(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "status":
                sendStatus(sender);
                break;
            case "clear":
                handleClear(sender, args);
                break;
            case "cache":
                handleCache(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "未知子命令: " + args[0]);
                sender.sendMessage(ChatColor.YELLOW + "用法: /" + label
                        + " [reload|status|clear <player>|cache]");
                break;
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "[AntiCheat] 配置已完整重新加载（含WebSocket重连）");
        plugin.getLogger().info("[AntiCheat] 配置已由 " + sender.getName() + " 重新加载");
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== AntiCheat 状态 ==========");
        sender.sendMessage(ChatColor.YELLOW + "WebSocket: "
                + (plugin.isWebSocketConnected() ? ChatColor.GREEN + "已连接" : ChatColor.RED + "未连接"));
        sender.sendMessage(ChatColor.YELLOW + "后端地址: " + ChatColor.WHITE + plugin.getApiBaseUrl());
        sender.sendMessage(ChatColor.YELLOW + "缓存状态: "
                + (plugin.getCacheManager().isCacheFresh() ? ChatColor.GREEN + "新鲜" : ChatColor.RED + "过期"));
        sender.sendMessage(ChatColor.YELLOW + "封禁缓存: " + ChatColor.WHITE
                + plugin.getCacheManager().getBannedCount() + " 条");
        sender.sendMessage(ChatColor.YELLOW + "白名单缓存: " + ChatColor.WHITE
                + plugin.getCacheManager().getWhitelistedCount() + " 条");
        sender.sendMessage(ChatColor.YELLOW + "追踪玩家数: " + ChatColor.WHITE
                + plugin.getPlayerDataManager().getTrackedPlayerCount());
        sender.sendMessage(ChatColor.YELLOW + "调试模式: "
                + (plugin.getConfigManager().isDebugEnabled() ? ChatColor.GREEN + "开启" : ChatColor.GRAY + "关闭"));
        sender.sendMessage(ChatColor.YELLOW + "降级策略: " + ChatColor.WHITE
                + plugin.getConfigManager().getFallbackStrategy());

        ConfigManager config = plugin.getConfigManager();
        boolean anyDetection = config.isAnyDetectionEnabled();
        sender.sendMessage(ChatColor.YELLOW + "检测状态: "
                + (anyDetection ? ChatColor.GREEN + "运行中" : ChatColor.RED + "全部关闭"));

        sender.sendMessage(ChatColor.GOLD + "--- 检测器详情 ---");
        sendDetectorStatus(sender, "飞行", plugin.getFlyDetector());
        sendDetectorStatus(sender, "速度", plugin.getSpeedDetector());
        sendDetectorStatus(sender, "自瞄", plugin.getAimbotDetector());
        sendDetectorStatus(sender, "自动点击", plugin.getAutoClickDetector());
        sendDetectorStatus(sender, "杀戮光环", plugin.getKillAuraDetector());
        sendDetectorStatus(sender, "透视", plugin.getXrayDetector());

        sender.sendMessage(ChatColor.GOLD + "====================================");
    }

    private void sendDetectorStatus(CommandSender sender, String name, AbstractDetector detector) {
        sender.sendMessage(ChatColor.YELLOW + "  " + name + ": "
                + (detector.isEnabled() ? ChatColor.GREEN + "启用" : ChatColor.RED + "禁用"));
    }

    private void handleClear(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /anticheat clear <玩家名称>");
            return;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线");
            return;
        }

        plugin.getPlayerDataManager().removeData(target.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + "[AntiCheat] 已清除玩家 "
                + target.getName() + " 的检测数据");
        plugin.getLogger().info("[AntiCheat] " + sender.getName()
                + " 清除了 " + target.getName() + " 的检测数据");
    }

    private void handleCache(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "[AntiCheat] 正在刷新缓存...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getCacheManager().forceRefresh();
            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage(ChatColor.GREEN + "[AntiCheat] 缓存已刷新");
                sender.sendMessage(ChatColor.YELLOW + "封禁缓存: "
                        + plugin.getCacheManager().getBannedCount() + " 条");
                sender.sendMessage(ChatColor.YELLOW + "白名单缓存: "
                        + plugin.getCacheManager().getWhitelistedCount() + " 条");
            });
        });
    }
}
