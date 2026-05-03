package com.anticheat.backend.controller;

import com.anticheat.backend.ai.AiService;
import com.anticheat.backend.ai.dto.AiAnalysisRequest;
import com.anticheat.backend.ai.dto.AiAnalysisResponse;
import com.anticheat.backend.model.CheatRecord;
import com.anticheat.backend.model.Player;
import com.anticheat.backend.model.Punishment;
import com.anticheat.backend.model.Report;
import com.anticheat.backend.service.CheatRecordService;
import com.anticheat.backend.service.PlayerService;
import com.anticheat.backend.service.PunishmentService;
import com.anticheat.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private CheatRecordService cheatRecordService;

    @Autowired
    private PunishmentService punishmentService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("available", aiService.isAvailable());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/analyze/cheat")
    public ResponseEntity<AiAnalysisResponse> analyzeCheat(@RequestBody Map<String, String> params) {
        String playerUuid = params.get("playerUuid");
        if (playerUuid == null || playerUuid.isEmpty()) {
            return ResponseEntity.badRequest().body(AiAnalysisResponse.fail("playerUuid不能为空"));
        }

        AiAnalysisRequest request = new AiAnalysisRequest();
        request.setType("cheat_analysis");
        request.setPlayerUuid(playerUuid);
        request.setPlayerName(params.getOrDefault("playerName", ""));

        List<CheatRecord> cheatRecords = cheatRecordService.getCheatRecordsByPlayerUuid(playerUuid);
        request.setCheatRecords(cheatRecords.stream().limit(20).map(this::cheatToMap).collect(Collectors.toList()));

        List<Punishment> punishments = punishmentService.getPunishmentsByPlayerUuid(playerUuid);
        request.setPunishmentRecords(punishments.stream().limit(10).map(this::punishmentToMap).collect(Collectors.toList()));

        AiAnalysisResponse response = aiService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/report")
    public ResponseEntity<AiAnalysisResponse> analyzeReport(@RequestBody Map<String, Object> params) {
        Long reportId = null;
        Object idObj = params.get("reportId");
        if (idObj != null) {
            try { reportId = Long.valueOf(idObj.toString()); } catch (NumberFormatException ignored) {}
        }

        AiAnalysisRequest request = new AiAnalysisRequest();
        request.setType("report_analysis");

        Map<String, Object> reportData = new HashMap<>(params);

        if (reportId != null) {
            Optional<Report> reportOpt = reportService.getById(reportId);
            if (reportOpt.isPresent()) {
                Report report = reportOpt.get();
                reportData.put("reporterName", report.getReporterName());
                reportData.put("reportedName", report.getReportedName());
                reportData.put("type", report.getReportType());
                reportData.put("reason", report.getReason());

                if (report.getReportedName() != null) {
                    Optional<Player> reportedPlayer = playerService.findByPlayerName(report.getReportedName());
                    if (reportedPlayer.isPresent()) {
                        List<CheatRecord> cheatRecords = cheatRecordService.getCheatRecordsByPlayerUuid(reportedPlayer.get().getUuid());
                        request.setCheatRecords(cheatRecords.stream().limit(10).map(this::cheatToMap).collect(Collectors.toList()));
                    }
                }
            }
        }

        request.setReportData(reportData);
        AiAnalysisResponse response = aiService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/dashboard")
    public ResponseEntity<AiAnalysisResponse> analyzeDashboard() {
        AiAnalysisRequest request = new AiAnalysisRequest();
        request.setType("dashboard_analysis");

        Map<String, Object> dashboardData = new HashMap<>();
        try {
            long totalPlayers = playerService.getTotalPlayers();
            long totalCheats = cheatRecordService.getTotalCheats();
            List<Player> highRisk = playerService.getHighRiskPlayers(10);
            long activeBans = punishmentService.getActivePunishments().size();
            long pendingReports = reportService.getPendingCount();

            dashboardData.put("totalPlayers", totalPlayers);
            dashboardData.put("totalCheats", totalCheats);
            dashboardData.put("highRiskPlayers", highRisk.size());
            dashboardData.put("mediumRiskPlayers", 0);
            dashboardData.put("lowRiskPlayers", Math.max(0, totalPlayers - highRisk.size()));
            dashboardData.put("activeBans", activeBans);
            dashboardData.put("pendingReports", pendingReports);

            List<CheatRecord> allCheats = cheatRecordService.getAllCheatRecords();
            Map<String, Long> typeDist = allCheats.stream()
                    .limit(50)
                    .collect(Collectors.groupingBy(CheatRecord::getCheatType, Collectors.counting()));
            dashboardData.put("cheatTypeDistribution", typeDist);
        } catch (Exception e) {
            dashboardData.put("error", "数据查询失败");
        }

        request.setDashboardData(dashboardData);
        AiAnalysisResponse response = aiService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/ban-evaluation")
    public ResponseEntity<AiAnalysisResponse> evaluateBan(@RequestBody Map<String, String> params) {
        String playerUuid = params.get("playerUuid");
        if (playerUuid == null || playerUuid.isEmpty()) {
            return ResponseEntity.badRequest().body(AiAnalysisResponse.fail("playerUuid不能为空"));
        }

        AiAnalysisRequest request = new AiAnalysisRequest();
        request.setType("ban_evaluation");
        request.setPlayerUuid(playerUuid);
        request.setPlayerName(params.getOrDefault("playerName", ""));

        List<CheatRecord> cheatRecords = cheatRecordService.getCheatRecordsByPlayerUuid(playerUuid);
        request.setCheatRecords(cheatRecords.stream().limit(20).map(this::cheatToMap).collect(Collectors.toList()));

        List<Punishment> punishments = punishmentService.getPunishmentsByPlayerUuid(playerUuid);
        request.setPunishmentRecords(punishments.stream().limit(10).map(this::punishmentToMap).collect(Collectors.toList()));

        AiAnalysisResponse response = aiService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<AiAnalysisResponse> chat(@RequestBody Map<String, Object> params) {
        String message = (String) params.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AiAnalysisResponse.fail("消息不能为空"));
        }

        String sessionId = (String) params.getOrDefault("sessionId", "default");
        String playerName = (String) params.get("playerName");

        AiAnalysisRequest request = new AiAnalysisRequest();
        request.setType("chat");
        request.setSessionId(sessionId);
        request.setUserMessage(message);
        request.setPlayerName(playerName);

        String ragContext = buildRagContext(message, playerName);
        request.setRagContext(ragContext);

        AiAnalysisResponse response = aiService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String sessionId) {
        aiService.clearSession(sessionId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
        String message = (String) params.get("message");
        String sessionId = (String) params.getOrDefault("sessionId", "default");
        String playerName = (String) params.get("playerName");

        AiAnalysisRequest request = new AiAnalysisRequest();
        request.setType("chat");
        request.setSessionId(sessionId);
        request.setUserMessage(message != null ? message : "");
        request.setPlayerName(playerName);

        String ragContext = buildRagContext(message, playerName);
        request.setRagContext(ragContext);

        return aiService.streamChat(request);
    }

    private String buildRagContext(String message, String playerName) {
        StringBuilder sb = new StringBuilder();

        try {
            long totalPlayers = playerService.getTotalPlayers();
            long totalCheats = cheatRecordService.getTotalCheats();
            long activeBans = punishmentService.getActivePunishments().size();
            long pendingReports = reportService.getPendingCount();
            List<Player> highRisk = playerService.getHighRiskPlayers(10);

            sb.append("系统实时数据：\n");
            sb.append("- 总玩家数: ").append(totalPlayers).append("\n");
            sb.append("- 总检测次数: ").append(totalCheats).append("\n");
            sb.append("- 活跃封禁: ").append(activeBans).append("\n");
            sb.append("- 待处理举报: ").append(pendingReports).append("\n");
            sb.append("- 高风险玩家数: ").append(highRisk.size()).append("\n");

            if (!highRisk.isEmpty()) {
                sb.append("- 高风险玩家列表: ");
                sb.append(highRisk.stream().limit(10)
                        .map(p -> p.getPlayerName() + "(风险:" + p.getRiskScore() + ")")
                        .collect(Collectors.joining(", ")));
                sb.append("\n");
            }

            List<CheatRecord> recentCheats = cheatRecordService.getAllCheatRecords();
            if (!recentCheats.isEmpty()) {
                Map<String, Long> typeDist = recentCheats.stream()
                        .limit(50)
                        .collect(Collectors.groupingBy(CheatRecord::getCheatType, Collectors.counting()));
                sb.append("- 近期作弊类型分布: ").append(typeDist).append("\n");
            }
        } catch (Exception e) {
            sb.append("- 数据查询异常\n");
        }

        if (playerName != null && !playerName.isEmpty()) {
            try {
                Optional<Player> playerOpt = playerService.findByPlayerName(playerName);
                if (playerOpt.isPresent()) {
                    Player p = playerOpt.get();
                    sb.append("\n当前玩家数据：\n");
                    sb.append("- 名称: ").append(p.getPlayerName()).append("\n");
                    sb.append("- 风险评分: ").append(p.getRiskScore()).append("\n");
                    sb.append("- 踢出次数: ").append(p.getKickCount()).append("\n");

                    List<CheatRecord> playerCheats = cheatRecordService.getCheatRecordsByPlayerUuid(p.getUuid());
                    if (!playerCheats.isEmpty()) {
                        sb.append("- 检测记录数: ").append(playerCheats.size()).append("\n");
                        Map<String, Long> playerTypeDist = playerCheats.stream()
                                .collect(Collectors.groupingBy(CheatRecord::getCheatType, Collectors.counting()));
                        sb.append("- 作弊类型: ").append(playerTypeDist).append("\n");
                    }
                }
            } catch (Exception ignored) {}
        }

        return sb.toString();
    }

    private Map<String, Object> cheatToMap(CheatRecord c) {
        Map<String, Object> m = new HashMap<>();
        m.put("cheatType", c.getCheatType());
        m.put("severity", c.getSeverity());
        m.put("detectionTime", String.valueOf(c.getDetectionTime()));
        m.put("details", c.getDetails());
        return m;
    }

    private Map<String, Object> punishmentToMap(Punishment p) {
        Map<String, Object> m = new HashMap<>();
        m.put("punishmentType", p.getPunishmentType());
        m.put("reason", p.getReason());
        m.put("active", p.isActive());
        m.put("punishmentTime", String.valueOf(p.getPunishmentTime()));
        return m;
    }
}
