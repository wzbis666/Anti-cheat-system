package com.anticheat.backend.controller;

import com.anticheat.backend.model.Punishment;
import com.anticheat.backend.service.PunishmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/punishment")
public class PunishmentController {

    @Autowired
    private PunishmentService punishmentService;

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
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("banned", isBanned);
        response.put("punishment", activeBan);
        
        if (activeBan != null) {
            response.put("reason", activeBan.getReason());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ban")
    public ResponseEntity<Punishment> banPlayer(@RequestBody Map<String, Object> request) {
        String playerName = (String) request.get("playerName");
        String uuid = (String) request.getOrDefault("uuid", request.get("playerUuid"));
        String punishmentType = (String) request.getOrDefault("punishmentType", "PERMANENT");
        long duration = request.containsKey("duration") ? 
            ((Number) request.get("duration")).longValue() : 0;
        String reason = (String) request.getOrDefault("reason", "作弊行为");

        Punishment punishment = punishmentService.banPlayer(
            playerName, uuid, punishmentType, duration, reason
        );
        return ResponseEntity.ok(punishment);
    }

    @PostMapping("/unban/{id}")
    public ResponseEntity<Punishment> unbanPlayer(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String unbannedBy = request != null ? request.getOrDefault("unbannedBy", "ADMIN") : "ADMIN";
        Punishment punishment = punishmentService.unbanPlayer(id, unbannedBy);
        if (punishment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(punishment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePunishment(@PathVariable Long id) {
        punishmentService.deletePunishment(id);
        return ResponseEntity.ok().build();
    }
}
