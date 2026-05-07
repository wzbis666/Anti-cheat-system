package com.anticheat.backend.handler;

import com.anticheat.backend.model.NotificationRule;
import com.anticheat.backend.repository.NotificationRuleRepository;
import com.anticheat.backend.service.CheatRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.anticheat.backend.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CheatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CheatWebSocketHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_CONNECTIONS = 100;
    private static final int MAX_MESSAGE_SIZE = 65536;

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    @Value("${api.key:}")
    private String validApiKey;

    private final JwtUtils jwtUtils;

    @Autowired
    private CheatRecordService cheatRecordService;

    @Autowired
    private NotificationRuleRepository notificationRuleRepository;

    public CheatWebSocketHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (connectionCount.get() >= MAX_CONNECTIONS) {
            logger.warn("WebSocket连接拒绝: 超过最大连接数 {}, remote={}", MAX_CONNECTIONS, session.getRemoteAddress());
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        if (!isAuthenticated(session)) {
            logger.warn("WebSocket连接拒绝: 认证失败, remote={}", session.getRemoteAddress());
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        sessions.add(session);
        connectionCount.incrementAndGet();
        logger.info("WebSocket连接建立: {}, 当前连接数: {}", session.getRemoteAddress(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        connectionCount.decrementAndGet();
        logger.info("WebSocket连接关闭: {}, 状态: {}, 当前连接数: {}", session.getRemoteAddress(), status, sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayloadLength() > MAX_MESSAGE_SIZE) {
            logger.warn("WebSocket消息过大，丢弃: {} bytes, remote={}", message.getPayloadLength(), session.getRemoteAddress());
            return;
        }

        String payload = message.getPayload();
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        try {
            Map<String, Object> data = mapper.readValue(payload, Map.class);
            String type = data.get("type") instanceof String ? (String) data.get("type") : "";

            if ("CHEAT_DETECTED".equals(type)) {
                String playerName = getStringOrNull(data, "playerName");
                String uuid = getStringOrNull(data, "uuid");
                String cheatType = getStringOrNull(data, "cheatType");
                int severity = data.get("severity") instanceof Number ? ((Number) data.get("severity")).intValue() : 1;
                String details = getStringOrNull(data, "details");

                if (uuid != null && cheatType != null) {
                    cheatRecordService.createCheatRecord(playerName, uuid, cheatType, severity, details);
                    logger.info("WebSocket作弊记录已保存: 玩家={}, 类型={}, 严重程度={}",
                            playerName, cheatType, severity);
                } else {
                    logger.warn("WebSocket作弊数据不完整，跳过保存: uuid={}, type={}", uuid, cheatType);
                }
            } else {
                logger.debug("WebSocket收到消息: type={}", type);
            }

            broadcastCheatData(data);
            checkNotificationRules(data);
        } catch (Exception e) {
            logger.warn("WebSocket消息处理失败: {}", e.getMessage());
        }
    }

    private void checkNotificationRules(Map<String, Object> cheatData) {
        List<NotificationRule> enabledRules = notificationRuleRepository.findByEnabledTrue();
        for (NotificationRule rule : enabledRules) {
            try {
                if (evaluateRule(rule, cheatData)) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "rule_triggered");
                    alert.put("ruleName", rule.getRuleName());
                    alert.put("ruleType", rule.getRuleType());
                    alert.put("message", "规则触发: " + rule.getRuleName());
                    alert.put("timestamp", System.currentTimeMillis());
                    broadcastCheatData(alert);
                    logger.info("通知规则触发: {}", rule.getRuleName());
                }
            } catch (Exception e) {
                logger.warn("检查通知规则失败: {} - {}", rule.getRuleName(), e.getMessage());
            }
        }
    }

    private boolean evaluateRule(NotificationRule rule, Map<String, Object> data) {
        String key = rule.getConditionKey();
        String operator = rule.getConditionOperator();
        String expectedValue = rule.getConditionValue();

        Object actualValue = data.get(key);
        if (actualValue == null && !key.contains(".")) {
            actualValue = data;
            for (String part : key.split("\\.")) {
                if (actualValue instanceof Map) {
                    actualValue = ((Map<?, ?>) actualValue).get(part);
                }
            }
        }
        if (actualValue == null) return false;

        String actualStr = String.valueOf(actualValue);

        switch (operator) {
            case "EQUALS":
                return expectedValue.equalsIgnoreCase(actualStr);
            case "CONTAINS":
                return actualStr.toLowerCase().contains(expectedValue.toLowerCase());
            case "GREATER_THAN":
                try {
                    return Double.parseDouble(actualStr) > Double.parseDouble(expectedValue);
                } catch (NumberFormatException e) {
                    return false;
                }
            case "LESS_THAN":
                try {
                    return Double.parseDouble(actualStr) < Double.parseDouble(expectedValue);
                } catch (NumberFormatException e) {
                    return false;
                }
            case "GREATER_EQUAL":
                try {
                    return Double.parseDouble(actualStr) >= Double.parseDouble(expectedValue);
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                return false;
        }
    }

    private String getStringOrNull(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof String ? (String) val : null;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket传输错误: {}", exception.getMessage());
        if (sessions.remove(session)) {
            connectionCount.decrementAndGet();
        }
    }

    public void broadcastCheatData(Map<String, Object> cheatData) {
        try {
            String json = mapper.writeValueAsString(cheatData);
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        logger.warn("发送WebSocket消息失败: {}", e.getMessage());
                        if (sessions.remove(session)) {
                            connectionCount.decrementAndGet();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("广播作弊数据失败", e);
        }
    }

    private boolean isAuthenticated(WebSocketSession session) {
        String apiKey = getApiKeyFromHeaders(session);
        if (apiKey == null) {
            apiKey = getApiKeyFromQuery(session);
        }
        if (isApiKeyValid(apiKey)) {
            return true;
        }

        String token = getTokenFromHeaders(session);
        if (token == null) {
            token = getTokenFromQuery(session);
        }
        return token != null && jwtUtils.validateToken(token);
    }

    private String getApiKeyFromHeaders(WebSocketSession session) {
        List<String> apiKeyHeaders = session.getHandshakeHeaders().get("X-Api-Key");
        return apiKeyHeaders != null && !apiKeyHeaders.isEmpty() ? apiKeyHeaders.get(0) : null;
    }

    private String getApiKeyFromQuery(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && "apiKey".equals(kv[0])) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    private boolean isApiKeyValid(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) return false;
        if (validApiKey == null || validApiKey.isEmpty()) return false;
        try {
            return MessageDigest.isEqual(apiKey.getBytes("UTF-8"), validApiKey.getBytes("UTF-8"));
        } catch (Exception e) {
            return false;
        }
    }

    private String getTokenFromHeaders(WebSocketSession session) {
        List<String> authHeaders = session.getHandshakeHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String auth = authHeaders.get(0);
            if (auth.startsWith("Bearer ")) return auth.substring(7);
        }
        return null;
    }

    private String getTokenFromQuery(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && "token".equals(kv[0])) return kv[1];
            }
        }
        return null;
    }
}
