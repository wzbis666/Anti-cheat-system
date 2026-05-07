package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.dto.PageResponse;
import com.anticheat.backend.model.AuditLog;
import com.anticheat.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<AuditLog>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> result = auditLogService.getAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));
        PageResponse<AuditLog> pageResp = new PageResponse<>(
                result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
        return ResponseEntity.ok(ApiResponse.ok(pageResp));
    }

    @GetMapping("/type/{actionType}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getByType(@PathVariable String actionType) {
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getByActionType(actionType)));
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getByAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getByAdminId(adminId)));
    }
}
