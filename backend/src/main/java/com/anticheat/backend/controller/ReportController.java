package com.anticheat.backend.controller;

import com.anticheat.backend.model.Report;
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
    public ResponseEntity<Map<String, String>> handle(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String handledBy = request.getOrDefault("handledBy", "ADMIN");
        String status = request.getOrDefault("status", "RESOLVED");
        String result = request.getOrDefault("result", "");
        
        reportService.handle(id, handledBy, status, result);
        return ResponseEntity.ok(Map.of("message", "举报已处理"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.ok().build();
    }
}
