package com.backend.Portfolio_Backend.dto;

public class AssetDetailDTO {
    private Long id;
    private String ticker;
    private Integer quantity;
    private Double avgBuyPrice;
    private Double currentPrice;
    private Double currentValue;
    private Double investedValue;
    private Double profitLoss;
    private Double profitLossPercent;

    public AssetDetailDTO() {
    }

    public AssetDetailDTO(
            Long id,
            String ticker,
            Integer quantity,
            Double avgBuyPrice,
            Double currentPrice,
            Double currentValue,
            Double investedValue,
            Double profitLoss,
            Double profitLossPercent
    ) {
        this.id = id;
        this.ticker = ticker;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
        this.currentPrice = currentPrice;
        this.currentValue = currentValue;
        this.investedValue = investedValue;
        this.profitLoss = profitLoss;
        this.profitLossPercent = profitLossPercent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(Double avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public Double getInvestedValue() {
        return investedValue;
    }

    public void setInvestedValue(Double investedValue) {
        this.investedValue = investedValue;
    }

    public Double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(Double profitLoss) {
        this.profitLoss = profitLoss;
    }

    public Double getProfitLossPercent() {
        return profitLossPercent;
    }

    public void setProfitLossPercent(Double profitLossPercent) {
        this.profitLossPercent = profitLossPercent;
    }
}
