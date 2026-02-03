package com.backend.Portfolio_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "portfolio_asset",
        uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "ticker"})
)

public class PortfolioAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @JsonIgnore
    private Portfolio portfolio;

    @Column(nullable = false, length = 10)
    private String ticker;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "avg_buy_price", nullable = false)
    private Double avgBuyPrice;

    @Column(name = "current_price")
    private Double currentPrice = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.currentPrice == null) {
            this.currentPrice = this.avgBuyPrice;
        }
    }

    // Computed properties
    @Transient
    public Double getCurrentValue() {
        if (quantity == null || currentPrice == null) {
            return 0.0;
        }
        return quantity * currentPrice;
    }

    @Transient
    public Double getInvestedValue() {
        if (quantity == null || avgBuyPrice == null) {
            return 0.0;
        }
        return quantity * avgBuyPrice;
    }

    @Transient
    public Double getProfitLoss() {
        return getCurrentValue() - getInvestedValue();
    }

    @Transient
    public Double getProfitLossPercent() {
        Double invested = getInvestedValue();
        if (invested == 0) {
            return 0.0;
        }
        return (getProfitLoss() / invested) * 100;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
