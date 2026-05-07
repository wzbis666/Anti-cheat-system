package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.dto.BanRequest;
import com.anticheat.backend.model.Punishment;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AuditLogService;
import com.anticheat.backend.service.PunishmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/punishment")
public class PunishmentController {

    @Autowired
    private PunishmentService punishmentService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/all")
    public ResponseEntity<List<Punishment>> getAllPunishments() {
        return ResponseEntity.ok(punishmentService.getAllPunishments());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Punishment>> getActivePunishments() {
        return ResponseEntity.ok(punishmentService.getActivePunishments());
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<Punishment>> getPunishmentsByPlayerId(@PathVariable Long playerId) {
        return ResponseEntity.ok(punishmentService.getPunishmentsByPlayerId(playerId));
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<List<Punishment>> getPunishmentsByUuid(@PathVariable String uuid) {
        return ResponseEntity.ok(punishmentService.getPunishmentsByPlayerUuid(uuid));
    }

    @GetMapping("/check/{uuid}")
    public ResponseEntity<Map<String, Object>> checkBanStatus(@PathVariable String uuid) {
        boolean isBanned = punishmentService.isPlayerBanned(uuid);
        Punishment activeBan = punishmentService.getActiveBan(uuid);

        Map<String, Object> response = new HashMap<>();
        response.put("banned", isBanned);
        response.put("punishment", activeBan);

        if (activeBan != null) {
            response.put("reason", activeBan.getReason());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/ban")
    public ResponseEntity<ApiResponse<Punishment>> banPlayer(@Valid @RequestBody BanRequest request) {
        Punishment punishment = punishmentService.banPlayer(
            request.getPlayerName(), request.getUuid(),
            request.getPunishmentType(), request.getDuration(), request.getReason()
        );

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "BAN", "PLAYER",
                punishment.getPlayer().getId(), request.getPlayerName(),
                request.getPunishmentType() + " - " + request.getReason());

        return ResponseEntity.ok(ApiResponse.ok(punishment, "封禁成功"));
    }

    @PostMapping("/unban/{id}")
    public ResponseEntity<ApiResponse<Punishment>> unbanPlayer(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String unbannedBy = request != null ? request.getOrDefault("unbannedBy", "ADMIN") : "ADMIN";
        Punishment punishment = punishmentService.unbanPlayer(id, unbannedBy);
        if (punishment == null) {
            return ResponseEntity.notFound().build();
        }

        String playerName = punishment.getPlayer() != null ? punishment.getPlayer().getPlayerName() : "Unknown";
        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "UNBAN", "PLAYER",
                punishment.getPlayer() != null ? punishment.getPlayer().getId() : null,
                playerName, "解封处罚ID: " + id);

        return ResponseEntity.ok(ApiResponse.ok(punishment, "解封成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePunishment(@PathVariable Long id) {
        punishmentService.deletePunishment(id);
        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "DELETE_PUNISHMENT", "PUNISHMENT",
                id, null, "删除处罚记录");
        return ResponseEntity.ok(ApiResponse.ok(null, "处罚记录已删除"));
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
