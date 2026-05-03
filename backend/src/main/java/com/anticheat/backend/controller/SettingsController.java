package com.anticheat.backend.controller;

import com.anticheat.backend.service.SystemSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SystemSettingsService settingsService;

    @Autowired
    public SettingsController(SystemSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public Map<String, Object> getAllSettings() {
        return settingsService.getAllSettings();
    }

    @GetMapping("/{key}")
    public ResponseEntity<Object> getSetting(@PathVariable String key) {
        Object value = settingsService.getSetting(key, null);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody Map<String, Object> settings) {
        settingsService.updateSettings(settings);
        return ResponseEntity.ok(Map.of("message", "设置已更新"));
    }

    @PutMapping("/{key}")
    public ResponseEntity<Map<String, String>> updateSetting(
            @PathVariable String key,
            @RequestBody Map<String, Object> body) {

        Object value = body.get("value");
        String description = (String) body.getOrDefault("description", null);

        if (value == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "缺少 value 字段"));
        }

        settingsService.setSetting(key, String.valueOf(value), description);
        return ResponseEntity.ok(Map.of("message", "设置已更新", "key", key, "value", String.valueOf(value)));
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initDefaultSettings() {
        settingsService.initDefaultSettings();
        return ResponseEntity.ok(Map.of("message", "默认设置已初始化"));
    }
}
