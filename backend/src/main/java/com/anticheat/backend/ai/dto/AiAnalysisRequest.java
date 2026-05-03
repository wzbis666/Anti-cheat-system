package com.anticheat.backend.ai.dto;

import java.util.List;
import java.util.Map;

public class AiAnalysisRequest {

    private String type;
    private String sessionId;
    private String playerName;
    private String playerUuid;
    private List<Map<String, Object>> cheatRecords;
    private List<Map<String, Object>> punishmentRecords;
    private Map<String, Object> reportData;
    private String userMessage;
    private List<Map<String, Object>> chatHistory;
    private String ragContext;
    private Map<String, Object> dashboardData;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }

    public List<Map<String, Object>> getCheatRecords() { return cheatRecords; }
    public void setCheatRecords(List<Map<String, Object>> cheatRecords) { this.cheatRecords = cheatRecords; }

    public List<Map<String, Object>> getPunishmentRecords() { return punishmentRecords; }
    public void setPunishmentRecords(List<Map<String, Object>> punishmentRecords) { this.punishmentRecords = punishmentRecords; }

    public Map<String, Object> getReportData() { return reportData; }
    public void setReportData(Map<String, Object> reportData) { this.reportData = reportData; }

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }

    public List<Map<String, Object>> getChatHistory() { return chatHistory; }
    public void setChatHistory(List<Map<String, Object>> chatHistory) { this.chatHistory = chatHistory; }

    public String getRagContext() { return ragContext; }
    public void setRagContext(String ragContext) { this.ragContext = ragContext; }

    public Map<String, Object> getDashboardData() { return dashboardData; }
    public void setDashboardData(Map<String, Object> dashboardData) { this.dashboardData = dashboardData; }
}
