package com.anticheat.backend.ai.dto;

import java.util.Map;

public class AiAnalysisResponse {

    private boolean success;
    private String analysis;
    private String verdict;
    private double confidence;
    private String suggestedAction;
    private String reasoning;
    private String model;
    private String error;

    public static AiAnalysisResponse ok(String analysis, String model) {
        AiAnalysisResponse r = new AiAnalysisResponse();
        r.setSuccess(true);
        r.setAnalysis(analysis);
        r.setModel(model);
        return r;
    }

    public static AiAnalysisResponse ok(String analysis, String verdict, double confidence, String suggestedAction, String reasoning, String model) {
        AiAnalysisResponse r = new AiAnalysisResponse();
        r.setSuccess(true);
        r.setAnalysis(analysis);
        r.setVerdict(verdict);
        r.setConfidence(confidence);
        r.setSuggestedAction(suggestedAction);
        r.setReasoning(reasoning);
        r.setModel(model);
        return r;
    }

    public static AiAnalysisResponse fail(String error) {
        AiAnalysisResponse r = new AiAnalysisResponse();
        r.setSuccess(false);
        r.setError(error);
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
