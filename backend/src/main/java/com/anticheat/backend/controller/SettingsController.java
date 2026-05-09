package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.handler.CheatWebSocketHandler;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AuditLogService;
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
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CheatWebSocketHandler webSocketHandler;

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
    public ResponseEntity<ApiResponse<Void>> updateSettings(@RequestBody Map<String, Object> settings) {
        settingsService.updateSettings(settings);

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "SETTINGS_UPDATE", "SETTINGS",
                null, null, "批量更新 " + settings.size() + " 项设置");

        return ResponseEntity.ok(ApiResponse.ok(null, "设置已更新"));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateSetting(
            @PathVariable String key,
            @RequestBody Map<String, Object> body) {

        Object value = body.get("value");
        String description = (String) body.getOrDefault("description", null);

        if (value == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("缺少 value 字段"));
        }

        settingsService.setSetting(key, String.valueOf(value), description);

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "SETTINGS_UPDATE", "SETTINGS",
                null, key, "更新设置为: " + value);

        return ResponseEntity.ok(ApiResponse.ok(Map.of("key", key, "value", String.valueOf(value)), "设置已更新"));
    }

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<Void>> initDefaultSettings() {
        settingsService.initDefaultSettings();
        return ResponseEntity.ok(ApiResponse.ok(null, "默认设置已初始化"));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Void>> syncToPlugin() {
        try {
            Map<String, Object> syncMessage = Map.of(
                "type", "CONFIG_SYNC",
                "timestamp", System.currentTimeMillis()
            );
            webSocketHandler.broadcastCheatData(syncMessage);
            return ResponseEntity.ok(ApiResponse.ok(null, "同步指令已发送"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.ok(null, "同步指令已发送（无插件连接）"));
        }
    }

    @GetMapping("/plugin")
    public Map<String, Object> getPluginSettings() {
        return settingsService.getAllSettings();
    }

    @PutMapping("/plugin")
    public ResponseEntity<ApiResponse<Void>> updatePluginSettings(@RequestBody Map<String, Object> settings) {
        settingsService.updateSettings(settings);

        try {
            Map<String, Object> updateMessage = Map.of(
                "type", "CONFIG_UPDATE",
                "config", settings,
                "timestamp", System.currentTimeMillis()
            );
            webSocketHandler.broadcastCheatData(updateMessage);
        } catch (Exception ignored) {}

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "PLUGIN_CONFIG_UPDATE", "SETTINGS",
                null, null, "批量更新插件配置 " + settings.size() + " 项");

        return ResponseEntity.ok(ApiResponse.ok(null, "插件配置已更新并同步"));
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
