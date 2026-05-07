package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.service.CheatRecordService;
import com.anticheat.backend.service.PlayerService;
import com.anticheat.backend.service.PunishmentService;
import com.anticheat.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final PlayerService playerService;
    private final CheatRecordService cheatRecordService;
    private final PunishmentService punishmentService;
    private final ReportService reportService;

    private static final int HIGH_RISK_THRESHOLD = 10;
    private static final int MEDIUM_RISK_MIN = 5;

    @Autowired
    public StatsController(PlayerService playerService,
                          CheatRecordService cheatRecordService,
                          PunishmentService punishmentService,
                          ReportService reportService) {
        this.playerService = playerService;
        this.cheatRecordService = cheatRecordService;
        this.punishmentService = punishmentService;
        this.reportService = reportService;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalPlayers = playerService.getTotalPlayers();
        stats.put("totalPlayers", totalPlayers);

        long totalCheats = cheatRecordService.getTotalCheats();
        stats.put("totalCheats", totalCheats);

        long highRiskPlayers = playerService.getHighRiskPlayerCount(HIGH_RISK_THRESHOLD);
        stats.put("highRiskPlayers", highRiskPlayers);

        long mediumRiskPlayers = playerService.getHighRiskPlayerCount(MEDIUM_RISK_MIN) - highRiskPlayers;
        stats.put("mediumRiskPlayers", Math.max(0, mediumRiskPlayers));

        long lowRiskPlayers = totalPlayers - highRiskPlayers - mediumRiskPlayers;
        stats.put("lowRiskPlayers", Math.max(0, lowRiskPlayers));

        long activeBans = punishmentService.getActivePunishments().size();
        stats.put("activeBans", activeBans);

        long pendingReports = reportService.getPendingCount();
        stats.put("pendingReports", pendingReports);

        return ApiResponse.ok(stats);
    }

    @GetMapping("/cheat-types")
    public ApiResponse<Map<String, Integer>> getCheatTypeStats() {
        return ApiResponse.ok(cheatRecordService.getCheatTypeStatistics());
    }

    @GetMapping("/recent")
    public ApiResponse<Map<String, Object>> getRecentStats(@RequestParam(defaultValue = "24") int hours) {
        long startTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000L);
        long recentCheats = cheatRecordService.getRecentCheatsCount(startTime);

        // Get hourly breakdown for trend chart
        List<Map<String, Object>> hourlyData = cheatRecordService.getHourlyBreakdown(startTime);

        Map<String, Object> stats = new HashMap<>();
        stats.put("recentCheats", recentCheats);
        stats.put("hours", hours);
        stats.put("hourlyData", hourlyData);

        return ApiResponse.ok(stats);
    }
}
