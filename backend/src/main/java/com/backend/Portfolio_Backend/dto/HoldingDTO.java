package com.backend.Portfolio_Backend.dto;

public class HoldingDTO {
    private String id;
    private String symbol;
    private String name;
    private Integer shares;
    private Double avgCost;
    private Double currentPrice;
    private Double value;
    private Double allocation;
    private Double dailyChange;
    private Double dailyChangePercent;

    public HoldingDTO() {
    }

    public HoldingDTO(String id, String symbol, String name, Integer shares, Double avgCost, 
                     Double currentPrice, Double value, Double allocation, 
                     Double dailyChange, Double dailyChangePercent) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.shares = shares;
        this.avgCost = avgCost;
        this.currentPrice = currentPrice;
        this.value = value;
        this.allocation = allocation;
        this.dailyChange = dailyChange;
        this.dailyChangePercent = dailyChangePercent;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getShares() {
        return shares;
    }

    public void setShares(Integer shares) {
        this.shares = shares;
    }

    public Double getAvgCost() {
        return avgCost;
    }

    public void setAvgCost(Double avgCost) {
        this.avgCost = avgCost;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getAllocation() {
        return allocation;
    }

    public void setAllocation(Double allocation) {
        this.allocation = allocation;
    }

    public Double getDailyChange() {
        return dailyChange;
    }

    public void setDailyChange(Double dailyChange) {
        this.dailyChange = dailyChange;
    }

    public Double getDailyChangePercent() {
        return dailyChangePercent;
    }

    public void setDailyChangePercent(Double dailyChangePercent) {
        this.dailyChangePercent = dailyChangePercent;
    }
}
