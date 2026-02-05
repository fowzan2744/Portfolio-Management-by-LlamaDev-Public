package com.backend.Portfolio_Backend.dto;

/**
 * Request DTO for AI portfolio analysis
 * Contains user preferences for personalized insights
 */
public class AIInsightsRequestDTO {
    private String riskProfile; // "Conservative", "Moderate", "Aggressive"
    private String investmentHorizon; // "Short", "Medium", "Long"
    private String notes; // Optional user notes/questions

    // Constructors
    public AIInsightsRequestDTO() {}

    public AIInsightsRequestDTO(String riskProfile, String investmentHorizon, String notes) {
        this.riskProfile = riskProfile;
        this.investmentHorizon = investmentHorizon;
        this.notes = notes;
    }

    // Getters and Setters
    public String getRiskProfile() {
        return riskProfile;
    }

    public void setRiskProfile(String riskProfile) {
        this.riskProfile = riskProfile;
    }

    public String getInvestmentHorizon() {
        return investmentHorizon;
    }

    public void setInvestmentHorizon(String investmentHorizon) {
        this.investmentHorizon = investmentHorizon;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
