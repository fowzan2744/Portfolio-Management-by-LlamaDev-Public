package com.backend.Portfolio_Backend.dto;

/**
 * Rebalancing action recommendation from AI
 */
public class RebalancingActionDTO {
    private String action; // "BUY", "SELL", "HOLD", "REDUCE", "INCREASE"
    private String symbol;
    private String reason;
    private Double suggestedAllocation; // Target allocation percentage

    // Constructors
    public RebalancingActionDTO() {}

    public RebalancingActionDTO(String action, String symbol, String reason, Double suggestedAllocation) {
        this.action = action;
        this.symbol = symbol;
        this.reason = reason;
        this.suggestedAllocation = suggestedAllocation;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getSuggestedAllocation() {
        return suggestedAllocation;
    }

    public void setSuggestedAllocation(Double suggestedAllocation) {
        this.suggestedAllocation = suggestedAllocation;
    }
}
