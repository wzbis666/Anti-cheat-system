package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.model.CheatRecord;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AuditLogService;
import com.anticheat.backend.service.CheatRecordService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cheat")
public class CheatRecordController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "cheatType", "severity", "detectionTime", "details"
    );

    private final CheatRecordService cheatRecordService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    public CheatRecordController(CheatRecordService cheatRecordService) {
        this.cheatRecordService = cheatRecordService;
    }

    @GetMapping("/all")
    public List<CheatRecord> getAllCheatRecords() {
        return cheatRecordService.getAllCheatRecords();
    }

    @GetMapping("/page")
    public Page<CheatRecord> getCheatRecordsByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "detectionTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "detectionTime";
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return cheatRecordService.getCheatRecordsByPage(pageable);
    }

    @GetMapping("/type/{cheatType}")
    public Page<CheatRecord> getCheatRecordsByType(
            @PathVariable String cheatType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("detectionTime").descending());
        return cheatRecordService.getCheatRecordsByType(cheatType, pageable);
    }

    @GetMapping("/player/{uuid}")
    public List<CheatRecord> getCheatRecordsByPlayerUuid(@PathVariable String uuid) {
        return cheatRecordService.getCheatRecordsByPlayerUuid(uuid);
    }

    @PostMapping("/add")
    public ResponseEntity<CheatRecord> addCheatRecord(@RequestBody Map<String, Object> request) {
        String playerName = (String) request.get("playerName");
        String uuid = (String) request.get("uuid");
        String cheatType = (String) request.get("cheatType");
        Integer severity = request.get("severity") != null ? Integer.valueOf(request.get("severity").toString()) : 1;
        String details = (String) request.get("details");
        String detectionMethod = (String) request.getOrDefault("detectionMethod", "AUTO_DETECT");
        String evidence = (String) request.get("evidence");
        String serverName = (String) request.get("serverName");

        CheatRecord saved = cheatRecordService.createCheatRecord(playerName, uuid, cheatType, severity,
                details, detectionMethod, evidence, serverName);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCheatRecord(@PathVariable Long id) {
        cheatRecordService.deleteCheatRecord(id);
        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "DELETE_CHEAT", "CHEAT",
                id, null, "删除作弊记录");
        return ResponseEntity.ok(ApiResponse.ok(null, "作弊记录已删除"));
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=cheat_records.csv");
        response.setCharacterEncoding("UTF-8");

        List<CheatRecord> records = cheatRecordService.getAllCheatRecords();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        PrintWriter writer = response.getWriter();
        writer.write('﻿');
        writer.println("ID,玩家名,UUID,作弊类型,严重等级,检测时间,检测方式,服务器,详情");

        for (CheatRecord r : records) {
            writer.printf("%d,%s,%s,%s,%d,%s,%s,%s,%s%n",
                    r.getId(),
                    r.getPlayer() != null ? escapeCsv(r.getPlayer().getPlayerName()) : "",
                    r.getPlayer() != null ? r.getPlayer().getUuid() : "",
                    escapeCsv(r.getCheatType()),
                    r.getSeverity(),
                    sdf.format(new Date(r.getDetectionTime())),
                    escapeCsv(r.getDetectionMethod() != null ? r.getDetectionMethod() : ""),
                    escapeCsv(r.getServerName() != null ? r.getServerName() : ""),
                    escapeCsv(r.getDetails() != null ? r.getDetails() : ""));
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String getCurrentUsername() {
        try {
            String token = getToken();
            return token != null ? jwtUtils.getUsernameFromToken(token) : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    private Long getCurrentUserId() {
        try {
            String token = getToken();
            return token != null ? jwtUtils.getUserIdFromToken(token) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getToken() {
        try {
            jakarta.servlet.http.HttpServletRequest request =
                    ((org.springframework.web.context.request.ServletRequestAttributes)
                            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                            .getRequest();
            String bearer = request.getHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) {
                return bearer.substring(7);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
