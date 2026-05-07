package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.model.Player;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AuditLogService;
import com.anticheat.backend.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

    private final PlayerService playerService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/all")
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Player> getPlayerByUuid(@PathVariable String uuid) {
        return playerService.findByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{playerName}")
    public ResponseEntity<Player> getPlayerByName(@PathVariable String playerName) {
        return playerService.findByPlayerName(playerName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/high-risk")
    public List<Player> getHighRiskPlayers(@RequestParam(defaultValue = "10") int threshold) {
        return playerService.getHighRiskPlayers(threshold);
    }

    @PostMapping("/add")
    public ResponseEntity<Player> addPlayer(@RequestBody Player player) {
        Player saved = playerService.getOrCreatePlayer(player.getPlayerName(), player.getUuid());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/update")
    public ResponseEntity<Player> updatePlayer(@RequestBody Player player) {
        Player updated = playerService.updatePlayer(player);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "DELETE_PLAYER", "PLAYER",
                id, null, "删除玩家及关联数据");
        return ResponseEntity.ok(ApiResponse.ok(null, "玩家已删除"));
    }

    @PostMapping("/kick/{uuid}")
    public ResponseEntity<Map<String, Object>> incrementKickCount(
            @PathVariable String uuid,
            @RequestBody(required = false) Map<String, String> request) {
        String playerName = request != null ? request.getOrDefault("playerName", "Unknown") : "Unknown";
        int kickCount = playerService.incrementKickCount(playerName, uuid);
        logger.info("玩家 {} 踢出次数更新: {}", playerName, kickCount);

        return ResponseEntity.ok(Map.of("uuid", uuid, "kickCount", kickCount));
    }

    @GetMapping("/kick/{uuid}")
    public ResponseEntity<Map<String, Object>> getKickCount(@PathVariable String uuid) {
        int kickCount = playerService.getKickCount(uuid);
        return ResponseEntity.ok(Map.of("uuid", uuid, "kickCount", kickCount));
    }

    @PostMapping("/kick/reset/{uuid}")
    public ResponseEntity<Map<String, Object>> resetKickCount(@PathVariable String uuid) {
        playerService.resetKickCount(uuid);
        return ResponseEntity.ok(Map.of("uuid", uuid, "kickCount", 0));
    }

    @PutMapping("/{id}/risk")
    public ResponseEntity<ApiResponse<Void>> updateRiskScore(@PathVariable Long id,
                                                              @RequestBody Map<String, Object> request) {
        try {
            int score = ((Number) request.get("score")).intValue();
            playerService.updateRiskScore(id, score);
            auditLogService.log(getCurrentUserId(), getCurrentUsername(), "UPDATE_RISK", "PLAYER",
                    id, null, "风险评分更新为: " + score);
            return ResponseEntity.ok(ApiResponse.ok(null, "风险评分已更新"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<ApiResponse<List<com.anticheat.backend.model.PlayerSession>>> getPlayerSessions(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(playerService.getPlayerSessions(id)));
    }

    @GetMapping("/{id}/ips")
    public ResponseEntity<ApiResponse<List<String>>> getPlayerIps(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(playerService.getPlayerDistinctIps(id)));
    }

    @PostMapping("/session/login")
    public ResponseEntity<ApiResponse<com.anticheat.backend.model.PlayerSession>> recordLogin(
            @RequestBody Map<String, String> request) {
        String playerName = request.get("playerName");
        String uuid = request.get("uuid");
        String ipAddress = request.get("ipAddress");
        String serverName = request.get("serverName");
        return ResponseEntity.ok(ApiResponse.ok(
                playerService.recordLogin(playerName, uuid, ipAddress, serverName)));
    }

    @PostMapping("/session/logout/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> recordLogout(@PathVariable Long sessionId) {
        playerService.recordLogout(sessionId);
        return ResponseEntity.ok(ApiResponse.ok(null, "登出已记录"));
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
