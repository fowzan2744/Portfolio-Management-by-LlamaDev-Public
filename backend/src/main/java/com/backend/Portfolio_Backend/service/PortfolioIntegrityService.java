package com.backend.Portfolio_Backend.service;

import com.backend.Portfolio_Backend.model.Portfolio;
import com.backend.Portfolio_Backend.model.PortfolioAuditLog;
import com.backend.Portfolio_Backend.repository.PortfolioAuditLogRepository;
import com.backend.Portfolio_Backend.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioIntegrityService {

    private final PortfolioAuditLogRepository auditRepository;
    private final PortfolioRepository portfolioRepository;
    private final HashService hashService;

    public PortfolioIntegrityService(
            PortfolioAuditLogRepository auditRepository,
            PortfolioRepository portfolioRepository,
            HashService hashService
    ) {
        this.auditRepository = auditRepository;
        this.portfolioRepository = portfolioRepository;
        this.hashService = hashService;
    }

    public boolean verifyIntegrity() {
        Portfolio portfolio = portfolioRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        List<PortfolioAuditLog> logs =
                auditRepository.findByPortfolioOrderByIdAsc(portfolio);

        String previousHash = "GENESIS";

        for (PortfolioAuditLog log : logs) {
            String recalculatedHash = hashService.sha256(
                    portfolio.getId()
                            + log.getAction()
                            + log.getTicker()
                            + log.getQuantity()
                            + log.getAvgBuyPrice()
                            + previousHash
            );

            if (!recalculatedHash.equals(log.getCurrentHash())) {
                return false;
            }

            previousHash = log.getCurrentHash();
        }

        return true;
    }
}
