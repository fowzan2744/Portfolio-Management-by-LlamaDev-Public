package com.backend.Portfolio_Backend.service;

import com.backend.Portfolio_Backend.model.Portfolio;
import com.backend.Portfolio_Backend.model.PortfolioAsset;
import com.backend.Portfolio_Backend.repository.PortfolioAssetRepository;
import com.backend.Portfolio_Backend.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository assetRepository;
    private final PortfolioAuditService auditService;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            PortfolioAssetRepository assetRepository,
            PortfolioAuditService auditService
    ) {
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.auditService = auditService;
    }

    public Portfolio getOrCreatePortfolio() {
        return portfolioRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> portfolioRepository.save(new Portfolio()));
    }

    public List<PortfolioAsset> getAssets() {
        Portfolio portfolio = getOrCreatePortfolio();
        return assetRepository.findByPortfolio(portfolio);
    }

    public PortfolioAsset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
    }

    public PortfolioAsset getAssetByTicker(String ticker) {
        Portfolio portfolio = getOrCreatePortfolio();
        return assetRepository.findByPortfolioAndTicker(portfolio, ticker)
                .orElseThrow(() -> new RuntimeException("Asset not found with ticker: " + ticker));
    }

    @Transactional
    public void addAsset(String ticker, int quantity, double avgBuyPrice) {
        Portfolio portfolio = getOrCreatePortfolio();

        PortfolioAsset asset = assetRepository
                .findByPortfolioAndTicker(portfolio, ticker)
                .orElseGet(() -> {
                    PortfolioAsset a = new PortfolioAsset();
                    a.setPortfolio(portfolio);
                    a.setTicker(ticker);
                    return a;
                });

        int currentQuantity = asset.getQuantity() == null ? 0 : asset.getQuantity();
        double currentAvgBuyPrice = asset.getAvgBuyPrice() == null ? avgBuyPrice : asset.getAvgBuyPrice();

        asset.setQuantity(currentQuantity + quantity);
        asset.setAvgBuyPrice((currentAvgBuyPrice * currentQuantity + avgBuyPrice * quantity) / (currentQuantity + quantity));

        assetRepository.save(asset);

        auditService.logAction(
                portfolio,
                "ADD",
                ticker,
                quantity,
                avgBuyPrice
        );
    }

    @Transactional
    public void removeAsset(String ticker, int quantity) {
        Portfolio portfolio = getOrCreatePortfolio();

        PortfolioAsset asset = assetRepository
                .findByPortfolioAndTicker(portfolio, ticker)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (asset.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient quantity");
        }

        asset.setQuantity(asset.getQuantity() - quantity);

        if (asset.getQuantity() == 0) {
            assetRepository.delete(asset);
        } else {
            assetRepository.save(asset);
        }

        auditService.logAction(
                portfolio,
                "REMOVE",
                ticker,
                quantity,
                asset.getAvgBuyPrice()
        );
    }
}
