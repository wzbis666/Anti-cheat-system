package com.anticheat.backend.ai.prompt;

import com.anticheat.backend.ai.dto.AiAnalysisRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class PromptTemplate {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            你是一个专业的Minecraft反作弊系统AI助手。你的职责是：
            1. 分析玩家行为数据，判断是否存在作弊行为
            2. 分析举报内容，给出处理建议
            3. 回答管理员关于反作弊系统的问题
            4. 提供数据分析和趋势解读
            5. 评估封禁决策的合理性
            
            回答要求：
            - 基于提供的数据客观分析，不要臆测
            - 给出明确的判断和建议
            - 使用中文回答
            - 如果数据不足以判断，请说明
            - 回答要简洁专业，重点突出
            """;

    public static String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public static String buildCheatAnalysisPrompt(AiAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下玩家的作弊检测数据，判断作弊可能性并给出建议：\n\n");
        sb.append("玩家：").append(request.getPlayerName()).append("\n");
        sb.append("UUID：").append(request.getPlayerUuid()).append("\n\n");

        if (request.getCheatRecords() != null && !request.getCheatRecords().isEmpty()) {
            sb.append("作弊检测记录（最近").append(request.getCheatRecords().size()).append("条）：\n");
            for (Map<String, Object> record : request.getCheatRecords()) {
                sb.append("- 类型: ").append(record.get("cheatType"))
                  .append(", 严重度: ").append(record.get("severity"))
                  .append(", 时间: ").append(record.get("detectionTime"))
                  .append(", 详情: ").append(record.get("details")).append("\n");
            }
        }

        if (request.getPunishmentRecords() != null && !request.getPunishmentRecords().isEmpty()) {
            sb.append("\n历史封禁记录：\n");
            for (Map<String, Object> record : request.getPunishmentRecords()) {
                sb.append("- 类型: ").append(record.get("punishmentType"))
                  .append(", 原因: ").append(record.get("reason"))
                  .append(", 状态: ").append(record.get("active") != null && (Boolean) record.get("active") ? "生效中" : "已过期").append("\n");
            }
        }

        sb.append("\n请以JSON格式返回分析结果，包含以下字段：\n");
        sb.append("- verdict: 判定结果（CLEAN/SUSPICIOUS/LIKELY_CHEATING/CONFIRMED_CHEATING）\n");
        sb.append("- confidence: 置信度（0.0-1.0）\n");
        sb.append("- suggestedAction: 建议操作（NONE/WARN/KICK/TEMP_BAN/PERM_BAN）\n");
        sb.append("- reasoning: 推理过程（中文描述）\n");
        sb.append("- analysis: 综合分析（中文描述）\n");

        return sb.toString();
    }

    public static String buildReportAnalysisPrompt(AiAnalysisRequest request) {
        Map<String, Object> report = request.getReportData();
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下玩家举报，给出处理建议：\n\n");

        if (report != null) {
            sb.append("举报人：").append(report.get("reporterName")).append("\n");
            sb.append("被举报人：").append(report.get("reportedName")).append("\n");
            sb.append("举报类型：").append(report.get("type")).append("\n");
            sb.append("举报原因：").append(report.get("reason")).append("\n\n");

            if (report.get("evidence") != null) {
                sb.append("系统检测证据：\n").append(formatJson(report.get("evidence"))).append("\n\n");
            }
        }

        if (request.getCheatRecords() != null && !request.getCheatRecords().isEmpty()) {
            sb.append("被举报玩家的检测记录：\n");
            for (Map<String, Object> record : request.getCheatRecords()) {
                sb.append("- ").append(record.get("cheatType")).append(" (严重度: ").append(record.get("severity")).append(")\n");
            }
        }

        sb.append("\n请以JSON格式返回分析结果，包含以下字段：\n");
        sb.append("- verdict: 判定结果（CONFIRM_VIOLATION/SUSPICIOUS/FALSE_POSITIVE/INSUFFICIENT_EVIDENCE）\n");
        sb.append("- confidence: 置信度（0.0-1.0）\n");
        sb.append("- suggestedAction: 建议操作（CONFIRM/WARN/REJECT/NEED_MORE_EVIDENCE）\n");
        sb.append("- reasoning: 推理过程（中文描述）\n");
        sb.append("- analysis: 综合分析（中文描述）\n");

        return sb.toString();
    }

    public static String buildDashboardAnalysisPrompt(AiAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下反作弊系统的仪表盘数据，给出洞察和建议：\n\n");

        if (request.getDashboardData() != null) {
            Map<String, Object> data = request.getDashboardData();
            sb.append("系统概览：\n");
            sb.append("- 总玩家数: ").append(data.get("totalPlayers")).append("\n");
            sb.append("- 总检测次数: ").append(data.get("totalCheats")).append("\n");
            sb.append("- 高风险玩家: ").append(data.get("highRiskPlayers")).append("\n");
            sb.append("- 中风险玩家: ").append(data.get("mediumRiskPlayers")).append("\n");
            sb.append("- 低风险玩家: ").append(data.get("lowRiskPlayers")).append("\n");
            sb.append("- 活跃封禁: ").append(data.get("activeBans")).append("\n");
            sb.append("- 待处理举报: ").append(data.get("pendingReports")).append("\n\n");

            if (data.get("cheatTypeDistribution") != null) {
                sb.append("作弊类型分布：\n").append(formatJson(data.get("cheatTypeDistribution"))).append("\n\n");
            }
            if (data.get("recentTrend") != null) {
                sb.append("近期趋势（24小时）：\n").append(formatJson(data.get("recentTrend"))).append("\n\n");
            }
        }

        sb.append("请分析：1) 当前系统安全状况 2) 主要威胁类型 3) 是否有异常趋势 4) 优化建议\n");
        sb.append("请以JSON格式返回，包含：analysis（综合分析）, threats（主要威胁列表）, suggestions（建议列表）, riskLevel（整体风险等级LOW/MEDIUM/HIGH）\n");

        return sb.toString();
    }

    public static String buildBanEvaluationPrompt(AiAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请评估以下封禁决策是否合理：\n\n");
        sb.append("玩家：").append(request.getPlayerName()).append("\n");
        sb.append("UUID：").append(request.getPlayerUuid()).append("\n\n");

        if (request.getCheatRecords() != null && !request.getCheatRecords().isEmpty()) {
            sb.append("作弊检测记录：\n");
            for (Map<String, Object> record : request.getCheatRecords()) {
                sb.append("- ").append(record.get("cheatType"))
                  .append(" (严重度: ").append(record.get("severity")).append(")")
                  .append(" 时间: ").append(record.get("detectionTime")).append("\n");
            }
        }

        if (request.getPunishmentRecords() != null && !request.getPunishmentRecords().isEmpty()) {
            sb.append("\n历史封禁：\n");
            for (Map<String, Object> record : request.getPunishmentRecords()) {
                sb.append("- ").append(record.get("punishmentType"))
                  .append(" 原因: ").append(record.get("reason"))
                  .append(" 状态: ").append(record.get("active") != null && (Boolean) record.get("active") ? "生效中" : "已过期").append("\n");
            }
        }

        sb.append("\n请以JSON格式返回：\n");
        sb.append("- verdict: 评估结果（APPROPRIATE/TOO_HARSH/TOO_LENIENT/NEED_MORE_EVIDENCE）\n");
        sb.append("- confidence: 置信度（0.0-1.0）\n");
        sb.append("- suggestedAction: 建议操作（PERM_BAN/TEMP_BAN_7D/TEMP_BAN_30D/KICK/WARN/UNBAN）\n");
        sb.append("- reasoning: 推理过程\n");
        sb.append("- analysis: 综合分析（含误判风险评估）\n");

        return sb.toString();
    }

    public static String buildChatPrompt(String userMessage, List<Map<String, String>> chatHistory, String ragContext) {
        StringBuilder sb = new StringBuilder();
        if (ragContext != null && !ragContext.isEmpty()) {
            sb.append("以下是当前系统的实时数据，请基于这些数据回答问题：\n").append(ragContext).append("\n\n");
        }
        if (chatHistory != null && !chatHistory.isEmpty()) {
            sb.append("之前的对话：\n");
            for (Map<String, String> msg : chatHistory) {
                String role = msg.get("role");
                String content = msg.get("content");
                sb.append(role.equals("user") ? "管理员" : "AI").append("：").append(content).append("\n");
            }
            sb.append("\n");
        }
        sb.append("管理员问题：").append(userMessage);
        return sb.toString();
    }

    private static String formatJson(Object obj) {
        try { return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj); }
        catch (Exception e) { return String.valueOf(obj); }
    }
}
