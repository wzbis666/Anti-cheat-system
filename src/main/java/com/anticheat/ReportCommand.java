package com.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportCommand implements CommandExecutor {

    private final AntiCheatPlugin plugin;
    private static final long REPORT_COOLDOWN_MS = 30000;
    private static final int MAX_REASON_LENGTH = 200;
    private final Map<UUID, Long> lastReportTime = new HashMap<>();

    public ReportCommand(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令");
            return true;
        }

        Player reporter = (Player) sender;

        if (args.length < 2) {
            reporter.sendMessage(ChatColor.RED + "用法: /report <玩家名称> <原因>");
            return true;
        }

        UUID reporterId = reporter.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastTime = lastReportTime.get(reporterId);
        if (lastTime != null && now - lastTime < REPORT_COOLDOWN_MS) {
            long remaining = (REPORT_COOLDOWN_MS - (now - lastTime)) / 1000;
            reporter.sendMessage(ChatColor.RED + "请等待 " + remaining + " 秒后再次举报");
            return true;
        }

        String reportedName = args[0];

        Player reported = Bukkit.getPlayer(reportedName);
        if (reported == null) {
            reporter.sendMessage(ChatColor.RED + "玩家 " + reportedName + " 不在线或不存在");
            return true;
        }

        if (reported.getUniqueId().equals(reporterId)) {
            reporter.sendMessage(ChatColor.RED + "你不能举报自己");
            return true;
        }

        if (reported.hasPermission("anticheat.bypass")) {
            reporter.sendMessage(ChatColor.RED + "无法举报该玩家");
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        if (reason.length() > MAX_REASON_LENGTH) {
            reporter.sendMessage(ChatColor.RED + "举报原因过长，最多 " + MAX_REASON_LENGTH + " 个字符");
            return true;
        }

        if (reason.length() < 2) {
            reporter.sendMessage(ChatColor.RED + "举报原因过短，请提供更详细的描述");
            return true;
        }

        lastReportTime.put(reporterId, now);

        String reportedUuid = reported.getUniqueId().toString();
        submitReport(reporter.getName(), reporter.getUniqueId().toString(),
                    reportedName, reportedUuid, reason);

        reporter.sendMessage(ChatColor.GREEN + "举报已提交，我们会尽快处理！");
        plugin.getLogger().info("玩家 " + reporter.getName() + " 举报了 " + reportedName + ": " + reason);

        notifyAdmins(reporter.getName(), reportedName, reason);

        return true;
    }

    private void notifyAdmins(String reporterName, String reportedName, String reason) {
        String notification = ChatColor.GOLD + "[AntiCheat] " + ChatColor.WHITE + reporterName
                + " 举报了 " + ChatColor.RED + reportedName
                + ChatColor.WHITE + ": " + reason;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("anticheat.notify") && !player.getName().equals(reporterName)) {
                player.sendMessage(notification);
            }
        }
    }

    private void submitReport(String reporterName, String reporterUuid,
                              String reportedName, String reportedUuid, String reason) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, Object> data = new HashMap<>();
            data.put("reporterName", reporterName);
            data.put("reporterUuid", reporterUuid);
            data.put("reportedName", reportedName);
            data.put("reportedUuid", reportedUuid);
            data.put("reason", reason);

            plugin.getHttp().post("/api/report/create", data);
        });
    }
}
