package com.backend.Portfolio_Backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_audit_log")
public class PortfolioAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false)
    private String action; // ADD / REMOVE

    @Column(nullable = false, length = 10)
    private String ticker;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "avg_buy_price", nullable = false)
    private Double avgBuyPrice;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "previous_hash", length = 64)
    private String previousHash;

    @Column(name = "current_hash", nullable = false, length = 64)
    private String currentHash;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public void setCurrentHash(String currentHash) {
        this.currentHash = currentHash;
    }
}
