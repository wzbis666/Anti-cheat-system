package com.anticheat.backend.controller;

import com.anticheat.backend.dto.ApiResponse;
import com.anticheat.backend.model.NotificationRule;
import com.anticheat.backend.repository.NotificationRuleRepository;
import com.anticheat.backend.security.JwtUtils;
import com.anticheat.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notification-rules")
public class NotificationRuleController {

    @Autowired
    private NotificationRuleRepository ruleRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<NotificationRule>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(ruleRepository.findAll()));
    }

    @GetMapping("/enabled")
    public ResponseEntity<ApiResponse<List<NotificationRule>>> getEnabled() {
        return ResponseEntity.ok(ApiResponse.ok(ruleRepository.findByEnabledTrue()));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<NotificationRule>> create(@RequestBody Map<String, Object> request) {
        NotificationRule rule = new NotificationRule();
        rule.setRuleName((String) request.get("ruleName"));
        rule.setRuleType((String) request.get("ruleType"));
        rule.setConditionKey((String) request.get("conditionKey"));
        rule.setConditionOperator((String) request.get("conditionOperator"));
        rule.setConditionValue(String.valueOf(request.get("conditionValue")));
        rule.setDescription((String) request.get("description"));
        rule.setEnabled(request.containsKey("enabled") ? (Boolean) request.get("enabled") : true);

        NotificationRule saved = ruleRepository.save(rule);

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "NOTIFICATION_RULE_CREATE",
                "NOTIFICATION_RULE", saved.getId(), saved.getRuleName(), "创建通知规则");

        return ResponseEntity.ok(ApiResponse.ok(saved, "通知规则已创建"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationRule>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Optional<NotificationRule> optional = ruleRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        NotificationRule rule = optional.get();
        if (request.containsKey("ruleName")) rule.setRuleName((String) request.get("ruleName"));
        if (request.containsKey("ruleType")) rule.setRuleType((String) request.get("ruleType"));
        if (request.containsKey("conditionKey")) rule.setConditionKey((String) request.get("conditionKey"));
        if (request.containsKey("conditionOperator")) rule.setConditionOperator((String) request.get("conditionOperator"));
        if (request.containsKey("conditionValue")) rule.setConditionValue(String.valueOf(request.get("conditionValue")));
        if (request.containsKey("description")) rule.setDescription((String) request.get("description"));
        if (request.containsKey("enabled")) rule.setEnabled((Boolean) request.get("enabled"));

        NotificationRule saved = ruleRepository.save(rule);

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "NOTIFICATION_RULE_UPDATE",
                "NOTIFICATION_RULE", saved.getId(), saved.getRuleName(), "更新通知规则");

        return ResponseEntity.ok(ApiResponse.ok(saved, "通知规则已更新"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ruleRepository.deleteById(id);

        auditLogService.log(getCurrentUserId(), getCurrentUsername(), "NOTIFICATION_RULE_DELETE",
                "NOTIFICATION_RULE", id, null, "删除通知规则");

        return ResponseEntity.ok(ApiResponse.ok(null, "通知规则已删除"));
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
