package com.anticheat.backend.controller;

import com.anticheat.backend.model.Player;
import com.anticheat.backend.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);
    
    private final PlayerService playerService;

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
        Optional<Player> player = playerService.findByUuid(uuid);
        return player.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{playerName}")
    public ResponseEntity<Player> getPlayerByName(@PathVariable String playerName) {
        Optional<Player> player = playerService.findByPlayerName(playerName);
        return player.map(ResponseEntity::ok)
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
    public ResponseEntity<Map<String, String>> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.ok(Map.of("message", "玩家已删除"));
    }

    @PostMapping("/kick/{uuid}")
    public ResponseEntity<Map<String, Object>> incrementKickCount(
            @PathVariable String uuid,
            @RequestBody(required = false) Map<String, String> request) {
        String playerName = request != null ? request.getOrDefault("playerName", "Unknown") : "Unknown";
        
        int kickCount = playerService.incrementKickCount(playerName, uuid);
        logger.info("玩家 {} 踢出次数更新: {}", playerName, kickCount);
        
        return ResponseEntity.ok(Map.of(
            "uuid", uuid,
            "kickCount", kickCount
        ));
    }

    @GetMapping("/kick/{uuid}")
    public ResponseEntity<Map<String, Object>> getKickCount(@PathVariable String uuid) {
        int kickCount = playerService.getKickCount(uuid);
        return ResponseEntity.ok(Map.of(
            "uuid", uuid,
            "kickCount", kickCount
        ));
    }

    @PostMapping("/kick/reset/{uuid}")
    public ResponseEntity<Map<String, Object>> resetKickCount(@PathVariable String uuid) {
        playerService.resetKickCount(uuid);
        return ResponseEntity.ok(Map.of(
            "uuid", uuid,
            "kickCount", 0
        ));
    }

    @PutMapping("/{id}/risk")
    public ResponseEntity<Map<String, Object>> updateRiskScore(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            int score = ((Number) request.get("score")).intValue();
            playerService.updateRiskScore(id, score);
            return ResponseEntity.ok(Map.of("success", true, "message", "风险评分已更新"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
