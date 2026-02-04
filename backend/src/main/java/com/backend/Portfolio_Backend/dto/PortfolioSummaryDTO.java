package com.backend.Portfolio_Backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PortfolioSummaryDTO {
    private Double totalValue;
    private Double investedValue;
    private Double profitLoss;
    private Double profitLossPercent;
    private Double todayChange;
    private Double todayChangePercent;
    private String dataAvailableSince;

    public PortfolioSummaryDTO() {
    }

    public PortfolioSummaryDTO(
            Double totalValue,
            Double investedValue,
            Double profitLoss,
            Double profitLossPercent,
            Double todayChange,
            Double todayChangePercent,
            String dataAvailableSince
    ) {
        this.totalValue = totalValue;
        this.investedValue = investedValue;
        this.profitLoss = profitLoss;
        this.profitLossPercent = profitLossPercent;
        this.todayChange = todayChange;
        this.todayChangePercent = todayChangePercent;
        this.dataAvailableSince = dataAvailableSince;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    @JsonProperty("totalInvested")
    public Double getInvestedValue() {
        return investedValue;
    }

    public void setInvestedValue(Double investedValue) {
        this.investedValue = investedValue;
    }

    @JsonProperty("totalGain")
    public Double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(Double profitLoss) {
        this.profitLoss = profitLoss;
    }

    @JsonProperty("totalGainPercent")
    public Double getProfitLossPercent() {
        return profitLossPercent;
    }

    public void setProfitLossPercent(Double profitLossPercent) {
        this.profitLossPercent = profitLossPercent;
    }

    @JsonProperty("dailyChange")
    public Double getTodayChange() {
        return todayChange;
    }

    public void setTodayChange(Double todayChange) {
        this.todayChange = todayChange;
    }

    @JsonProperty("dailyChangePercent")
    public Double getTodayChangePercent() {
        return todayChangePercent;
    }

    public void setTodayChangePercent(Double todayChangePercent) {
        this.todayChangePercent = todayChangePercent;
    }

    public String getDataAvailableSince() {
        return dataAvailableSince;
    }

    public void setDataAvailableSince(String dataAvailableSince) {
        this.dataAvailableSince = dataAvailableSince;
    }
}
