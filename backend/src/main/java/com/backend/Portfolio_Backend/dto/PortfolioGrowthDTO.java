package com.backend.Portfolio_Backend.dto;

public class PortfolioGrowthDTO {
    private String time;
    private Double value;

    public PortfolioGrowthDTO() {
    }

    public PortfolioGrowthDTO(String time, Double value) {
        this.time = time;
        this.value = value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
