package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.model.Appeal;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AppealService;
import com.anticheat.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appeal")
public class AppealController {

    @Autowired
    private AppealService appealService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Appeal>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(appealService.getAll()));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Appeal>>> getPending() {
        return ResponseEntity.ok(ApiResponse.ok(appealService.getPending()));
    }

    @GetMapping("/count/pending")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingCount() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", appealService.getPendingCount())));
    }

    @GetMapping("/player/{uuid}")
    public ResponseEntity<ApiResponse<List<Appeal>>> getByPlayer(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.ok(appealService.getByPlayerUuid(uuid)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Appeal>> create(@RequestBody Map<String, String> request) {
        String playerName = request.get("playerName");
        String playerUuid = request.get("playerUuid");
        Long punishmentId = request.containsKey("punishmentId") ?
                Long.parseLong(request.get("punishmentId")) : null;
        String reason = request.get("reason");

        if (playerName == null || playerUuid == null || reason == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("缺少必要参数"));
        }

        Appeal appeal = appealService.create(playerName, playerUuid, punishmentId, reason);
        return ResponseEntity.ok(ApiResponse.ok(appeal, "申诉已提交"));
    }

    @PostMapping("/handle/{id}")
    public ResponseEntity<ApiResponse<Appeal>> handle(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String handledBy = request.getOrDefault("handledBy", getCurrentUsername());
        String status = request.getOrDefault("status", "REJECTED");
        String adminResponse = request.getOrDefault("adminResponse", "");

        Appeal appeal = appealService.handle(id, handledBy, status, adminResponse);
        if (appeal == null) {
            return ResponseEntity.notFound().build();
        }

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "HANDLE_APPEAL", "APPEAL",
                id, appeal.getPlayerName(), status + " - " + adminResponse);

        return ResponseEntity.ok(ApiResponse.ok(appeal, "申诉已处理"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        appealService.delete(id);
        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "DELETE_APPEAL", "APPEAL",
                id, null, "删除申诉记录");
        return ResponseEntity.ok(ApiResponse.ok(null, "申诉已删除"));
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
