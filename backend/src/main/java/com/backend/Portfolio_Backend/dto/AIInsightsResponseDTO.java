package com.backend.Portfolio_Backend.dto;

import java.util.List;

/**
 * Response DTO for AI-generated portfolio insights
 */
public class AIInsightsResponseDTO {
    private String summary; // Brief portfolio overview
    private List<String> risks; // Identified risks
    private List<String> opportunities; // Investment opportunities
    private List<RebalancingActionDTO> rebalancingActions; // Recommended actions
    private String sentimentAnalysis; // News sentiment overview
    private List<String> nextSteps; // Actionable next steps
    private boolean isAiGenerated; // True if from Gemini, false if fallback

    // Constructors
    public AIInsightsResponseDTO() {}

    public AIInsightsResponseDTO(String summary, List<String> risks, List<String> opportunities,
                                 List<RebalancingActionDTO> rebalancingActions, String sentimentAnalysis,
                                 List<String> nextSteps, boolean isAiGenerated) {
        this.summary = summary;
        this.risks = risks;
        this.opportunities = opportunities;
        this.rebalancingActions = rebalancingActions;
        this.sentimentAnalysis = sentimentAnalysis;
        this.nextSteps = nextSteps;
        this.isAiGenerated = isAiGenerated;
    }

    // Getters and Setters
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public List<String> getOpportunities() {
        return opportunities;
    }

    public void setOpportunities(List<String> opportunities) {
        this.opportunities = opportunities;
    }

    public List<RebalancingActionDTO> getRebalancingActions() {
        return rebalancingActions;
    }

    public void setRebalancingActions(List<RebalancingActionDTO> rebalancingActions) {
        this.rebalancingActions = rebalancingActions;
    }

    public String getSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public void setSentimentAnalysis(String sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }

    public List<String> getNextSteps() {
        return nextSteps;
    }

    public void setNextSteps(List<String> nextSteps) {
        this.nextSteps = nextSteps;
    }

    public boolean isAiGenerated() {
        return isAiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        isAiGenerated = aiGenerated;
    }
}
