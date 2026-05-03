package com.anticheat.backend.controller;

import com.anticheat.backend.model.Whitelist;
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
        return ResponseEntity.ok(Map.of(
            "uuid", uuid,
            "whitelisted", isWhitelisted
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<Whitelist> add(@RequestBody Map<String, String> request) {
        String playerName = request.getOrDefault("playerName", "Unknown");
        String uuid = request.get("uuid");
        String reason = request.getOrDefault("reason", "");
        String addedBy = request.getOrDefault("addedBy", "ADMIN");
        
        if (uuid == null || uuid.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Whitelist whitelist = whitelistService.add(playerName, uuid, reason, addedBy);
        return ResponseEntity.ok(whitelist);
    }

    @PostMapping("/remove/{uuid}")
    public ResponseEntity<Map<String, String>> remove(@PathVariable String uuid) {
        whitelistService.remove(uuid);
        return ResponseEntity.ok(Map.of("message", "已移除白名单"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        whitelistService.delete(id);
        return ResponseEntity.ok().build();
    }
}
