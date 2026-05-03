package com.anticheat.backend.ai;

import com.anticheat.backend.ai.dto.AiAnalysisRequest;
import com.anticheat.backend.ai.dto.AiAnalysisResponse;
import com.anticheat.backend.ai.prompt.PromptTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;
    private static final long SESSION_TTL_MS = 30 * 60 * 1000;
    private static final int MAX_SESSION_MESSAGES = 20;

    @Autowired
    private AiConfig aiConfig;

    private RestTemplate restTemplate;

    @Autowired
    public void setAiConfig(AiConfig aiConfig) {
        this.aiConfig = aiConfig;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(aiConfig.getTimeout() * 1000);
        factory.setReadTimeout(aiConfig.getTimeout() * 1000);
        this.restTemplate = new RestTemplate(factory);
    }

    private final Map<String, AiAnalysisResponse> analysisCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, String>>> sessionHistory = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionTimestamps = new ConcurrentHashMap<>();
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();

    public AiAnalysisResponse analyze(AiAnalysisRequest request) {
        if (!aiConfig.isEnabled()) {
            return AiAnalysisResponse.fail("AI功能未启用，请在配置中设置 ai.enabled=true 并提供有效的 API Key");
        }

        String cacheKey = buildCacheKey(request);
        AiAnalysisResponse cached = getFromCache(cacheKey);
        if (cached != null) {
            logger.debug("AI分析命中缓存: {}", cacheKey);
            return cached;
        }

        try {
            String userPrompt;
            switch (request.getType()) {
                case "cheat_analysis":
                    userPrompt = PromptTemplate.buildCheatAnalysisPrompt(request);
                    break;
                case "report_analysis":
                    userPrompt = PromptTemplate.buildReportAnalysisPrompt(request);
                    break;
                case "dashboard_analysis":
                    userPrompt = PromptTemplate.buildDashboardAnalysisPrompt(request);
                    break;
                case "ban_evaluation":
                    userPrompt = PromptTemplate.buildBanEvaluationPrompt(request);
                    break;
                case "chat":
                    String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
                    List<Map<String, String>> history = getOrCreateSession(sessionId);
                    userPrompt = PromptTemplate.buildChatPrompt(
                            request.getUserMessage(),
                            history,
                            request.getRagContext() != null ? request.getRagContext() : ""
                    );
                    String aiResponse = callLlmWithHistory(userPrompt, history);
                    addToSession(sessionId, "user", request.getUserMessage());
                    addToSession(sessionId, "assistant", aiResponse);
                    AiAnalysisResponse chatResp = AiAnalysisResponse.ok(aiResponse, aiConfig.getModel());
                    return chatResp;
                default:
                    return AiAnalysisResponse.fail("未知的分析类型: " + request.getType());
            }

            String aiResponse = callLlm(userPrompt);
            AiAnalysisResponse response = parseAnalysisResponse(aiResponse, request.getType());
            putToCache(cacheKey, response);
            return response;

        } catch (Exception e) {
            logger.error("AI分析异常", e);
            return AiAnalysisResponse.fail("AI分析失败: " + e.getMessage());
        }
    }

    private String callLlm(String userPrompt) {
        return callLlmWithHistory(userPrompt, null);
    }

    private String callLlmWithHistory(String userPrompt, List<Map<String, String>> history) {
        String url = aiConfig.getBaseUrl() + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getApiKey());

        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", PromptTemplate.getSystemPrompt());
        messages.add(systemMsg);

        if (history != null && !history.isEmpty()) {
            for (Map<String, String> msg : history) {
                Map<String, Object> hMsg = new HashMap<>();
                hMsg.put("role", msg.get("role"));
                hMsg.put("content", msg.get("content"));
                messages.add(hMsg);
            }
        }

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiConfig.getModel());
        body.put("messages", messages);
        body.put("max_tokens", aiConfig.getMaxTokens());
        body.put("temperature", aiConfig.getTemperature());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        logger.debug("AI请求: model={}, prompt长度={}, history={}", aiConfig.getModel(), userPrompt.length(), history != null ? history.size() : 0);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    return choices.get(0).get("message").get("content").asText();
                }
            } catch (Exception e) {
                logger.error("解析AI响应失败", e);
            }
        }

        throw new RuntimeException("AI服务返回异常: " + response.getStatusCode());
    }

    private AiAnalysisResponse parseAnalysisResponse(String aiResponse, String type) {
        if ("chat".equals(type)) {
            return AiAnalysisResponse.ok(aiResponse, aiConfig.getModel());
        }

        try {
            String jsonStr = aiResponse;
            if (aiResponse.contains("```json")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```json") + 7);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            } else if (aiResponse.contains("```")) {
                jsonStr = aiResponse.substring(aiResponse.indexOf("```") + 3);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            }
            jsonStr = jsonStr.trim();

            JsonNode node = mapper.readTree(jsonStr);

            return AiAnalysisResponse.ok(
                    getTextSafe(node, "analysis"),
                    getTextSafe(node, "verdict"),
                    getDoubleSafe(node, "confidence", 0.5),
                    getTextSafe(node, "suggestedAction"),
                    getTextSafe(node, "reasoning"),
                    aiConfig.getModel()
            );
        } catch (Exception e) {
            logger.warn("解析AI结构化响应失败，返回原始文本", e);
            return AiAnalysisResponse.ok(aiResponse, aiConfig.getModel());
        }
    }

    private String getTextSafe(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : "";
    }

    private double getDoubleSafe(JsonNode node, String field, double defaultValue) {
        return node.has(field) ? node.get(field).asDouble(defaultValue) : defaultValue;
    }

    private String buildCacheKey(AiAnalysisRequest request) {
        return request.getType() + ":" +
                (request.getPlayerUuid() != null ? request.getPlayerUuid() : "") + ":" +
                (request.getReportData() != null ? request.getReportData().hashCode() : 0) + ":" +
                (request.getUserMessage() != null ? request.getUserMessage().hashCode() : 0);
    }

    private AiAnalysisResponse getFromCache(String key) {
        cleanExpiredCache();
        Long ts = cacheTimestamps.get(key);
        if (ts != null && System.currentTimeMillis() - ts < CACHE_TTL_MS) {
            return analysisCache.get(key);
        }
        analysisCache.remove(key);
        cacheTimestamps.remove(key);
        return null;
    }

    private void putToCache(String key, AiAnalysisResponse response) {
        analysisCache.put(key, response);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }

    private void cleanExpiredCache() {
        long now = System.currentTimeMillis();
        cacheTimestamps.entrySet().removeIf(e -> {
            if (now - e.getValue() > CACHE_TTL_MS) {
                analysisCache.remove(e.getKey());
                return true;
            }
            return false;
        });
    }

    public void invalidateCacheForPlayer(String playerUuid) {
        analysisCache.keySet().removeIf(k -> k.contains(playerUuid));
        cacheTimestamps.keySet().removeIf(k -> k.contains(playerUuid));
    }

    private List<Map<String, String>> getOrCreateSession(String sessionId) {
        cleanExpiredSessions();
        if (!sessionHistory.containsKey(sessionId)) {
            sessionHistory.put(sessionId, new ArrayList<>());
        }
        sessionTimestamps.put(sessionId, System.currentTimeMillis());
        return sessionHistory.get(sessionId);
    }

    private void addToSession(String sessionId, String role, String content) {
        List<Map<String, String>> history = sessionHistory.get(sessionId);
        if (history != null) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", role);
            msg.put("content", content);
            history.add(msg);
            while (history.size() > MAX_SESSION_MESSAGES) {
                history.remove(0);
            }
        }
        sessionTimestamps.put(sessionId, System.currentTimeMillis());
    }

    private void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        sessionTimestamps.entrySet().removeIf(e -> {
            if (now - e.getValue() > SESSION_TTL_MS) {
                sessionHistory.remove(e.getKey());
                return true;
            }
            return false;
        });
    }

    public void clearSession(String sessionId) {
        sessionHistory.remove(sessionId);
        sessionTimestamps.remove(sessionId);
    }

    public boolean isAvailable() {
        return aiConfig.isEnabled() && aiConfig.getApiKey() != null && !aiConfig.getApiKey().isEmpty();
    }

    public SseEmitter streamChat(AiAnalysisRequest request) {
        SseEmitter emitter = new SseEmitter(120000L);

        streamExecutor.execute(() -> {
            StringBuilder fullResponse = new StringBuilder();
            try {
                String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
                List<Map<String, String>> history = getOrCreateSession(sessionId);
                String userPrompt = PromptTemplate.buildChatPrompt(
                        request.getUserMessage(),
                        history,
                        request.getRagContext() != null ? request.getRagContext() : ""
                );

                String urlStr = aiConfig.getBaseUrl() + "/chat/completions";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + aiConfig.getApiKey());
                conn.setConnectTimeout(aiConfig.getTimeout() * 1000);
                conn.setReadTimeout(aiConfig.getTimeout() * 1000);

                List<Map<String, Object>> messages = new ArrayList<>();
                Map<String, Object> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", PromptTemplate.getSystemPrompt());
                messages.add(systemMsg);

                for (Map<String, String> msg : history) {
                    Map<String, Object> hMsg = new HashMap<>();
                    hMsg.put("role", msg.get("role"));
                    hMsg.put("content", msg.get("content"));
                    messages.add(hMsg);
                }

                Map<String, Object> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", userPrompt);
                messages.add(userMsg);

                Map<String, Object> body = new HashMap<>();
                body.put("model", aiConfig.getModel());
                body.put("messages", messages);
                body.put("max_tokens", aiConfig.getMaxTokens());
                body.put("temperature", aiConfig.getTemperature());
                body.put("stream", true);

                String jsonBody = mapper.writeValueAsString(body);
                conn.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();
                            if ("[DONE]".equals(data)) break;
                            try {
                                JsonNode chunk = mapper.readTree(data);
                                JsonNode delta = chunk.at("/choices/0/delta/content");
                                if (!delta.isMissingNode() && !delta.isNull()) {
                                    String content = delta.asText();
                                    fullResponse.append(content);
                                    emitter.send(SseEmitter.event().name("message").data(content));
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }

                addToSession(sessionId, "user", request.getUserMessage());
                addToSession(sessionId, "assistant", fullResponse.toString());
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();

            } catch (Exception e) {
                logger.error("流式AI聊天异常", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage() != null ? e.getMessage() : "未知错误"));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    public void saveAnalysisRecord(String type, String playerUuid, String playerName, AiAnalysisResponse response) {
        logger.debug("AI分析记录暂存: type={}, player={}, analysis={}", type, playerName,
                response != null && response.getAnalysis() != null ? response.getAnalysis().substring(0, Math.min(50, response.getAnalysis().length())) : "null");
    }
}
