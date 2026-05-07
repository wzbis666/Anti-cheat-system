package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.model.Whitelist;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AuditLogService;
import com.anticheat.backend.service.WhitelistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whitelist")
public class WhitelistController {

    @Autowired
    private WhitelistService whitelistService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/all")
    public ResponseEntity<List<Whitelist>> getAll() {
        return ResponseEntity.ok(whitelistService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Whitelist>> getActive() {
        return ResponseEntity.ok(whitelistService.getActive());
    }

    @GetMapping("/check/{uuid}")
    public ResponseEntity<Map<String, Object>> check(@PathVariable String uuid) {
        boolean isWhitelisted = whitelistService.isWhitelisted(uuid);
        return ResponseEntity.ok(Map.of("uuid", uuid, "whitelisted", isWhitelisted));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Whitelist>> add(@RequestBody Map<String, String> request) {
        String playerName = request.getOrDefault("playerName", "Unknown");
        String uuid = request.get("uuid");
        String reason = request.getOrDefault("reason", "");
        String addedBy = request.getOrDefault("addedBy", "ADMIN");

        if (uuid == null || uuid.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Whitelist whitelist = whitelistService.add(playerName, uuid, reason, addedBy);

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "WHITELIST_ADD", "WHITELIST",
                whitelist.getId(), playerName, "添加到白名单: " + reason);

        return ResponseEntity.ok(ApiResponse.ok(whitelist, "已添加到白名单"));
    }

    @PostMapping("/remove/{uuid}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable String uuid) {
        whitelistService.remove(uuid);
        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "WHITELIST_REMOVE", "WHITELIST",
                null, uuid, "从白名单移除");
        return ResponseEntity.ok(ApiResponse.ok(null, "已移除白名单"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        whitelistService.delete(id);
        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "WHITELIST_DELETE", "WHITELIST",
                id, null, "删除白名单记录");
        return ResponseEntity.ok(ApiResponse.ok(null, "白名单记录已删除"));
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
