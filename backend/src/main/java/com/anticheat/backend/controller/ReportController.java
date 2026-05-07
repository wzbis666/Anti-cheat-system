package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.model.Report;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AuditLogService;
import com.anticheat.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/all")
    public ResponseEntity<List<Report>> getAll() {
        return ResponseEntity.ok(reportService.getAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPending() {
        return ResponseEntity.ok(reportService.getPending());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getById(@PathVariable Long id) {
        return reportService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count/pending")
    public ResponseEntity<Map<String, Object>> getPendingCount() {
        return ResponseEntity.ok(Map.of("count", reportService.getPendingCount()));
    }

    @PostMapping("/create")
    public ResponseEntity<Report> create(@RequestBody Map<String, String> request) {
        String reporterName = request.getOrDefault("reporterName", "Unknown");
        String reporterUuid = request.getOrDefault("reporterUuid", "");
        String reportedName = request.get("reportedName");
        String reportedUuid = request.getOrDefault("reportedUuid", "");
        String reason = request.getOrDefault("reason", "");
        String reportType = request.getOrDefault("reportType", "CHEATING");

        if (reportedName == null || reportedName.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Report report = reportService.create(reporterName, reporterUuid, reportedName,
            reportedUuid, reason, reportType);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/handle/{id}")
    public ResponseEntity<ApiResponse<Void>> handle(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String handledBy = request.getOrDefault("handledBy", "ADMIN");
        String status = request.getOrDefault("status", "RESOLVED");
        String result = request.getOrDefault("result", "");

        reportService.handle(id, handledBy, status, result);

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "HANDLE_REPORT", "REPORT",
                id, null, status + " - " + result);

        return ResponseEntity.ok(ApiResponse.ok(null, "举报已处理"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reportService.delete(id);
        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "DELETE_REPORT", "REPORT",
                id, null, "删除举报记录");
        return ResponseEntity.ok(ApiResponse.ok(null, "举报记录已删除"));
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
