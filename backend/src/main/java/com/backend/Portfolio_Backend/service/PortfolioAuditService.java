package com.backend.Portfolio_Backend.service;


import com.backend.Portfolio_Backend.model.Portfolio;
import com.backend.Portfolio_Backend.model.PortfolioAuditLog;
import com.backend.Portfolio_Backend.repository.PortfolioAuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class PortfolioAuditService {

    private final PortfolioAuditLogRepository auditRepository;
    private final HashService hashService;

    public PortfolioAuditService(
            PortfolioAuditLogRepository auditRepository,
            HashService hashService
    ) {
        this.auditRepository = auditRepository;
        this.hashService = hashService;
    }

    public void logAction(
            Portfolio portfolio,
            String action,
            String ticker,
            int quantity,
            double avgBuyPrice
    ) {
        String previousHash = auditRepository
                .findTopByPortfolioOrderByIdDesc(portfolio)
                .map(PortfolioAuditLog::getCurrentHash)
                .orElse("GENESIS");

        String dataToHash = portfolio.getId()
                + action
                + ticker
                + quantity
                + avgBuyPrice
                + previousHash;

        String currentHash = hashService.sha256(dataToHash);

        PortfolioAuditLog log = new PortfolioAuditLog();
        log.setPortfolio(portfolio);
        log.setAction(action);
        log.setTicker(ticker);
        log.setQuantity(quantity);
        log.setAvgBuyPrice(avgBuyPrice);
        log.setPreviousHash(previousHash);
        log.setCurrentHash(currentHash);

        auditRepository.save(log);
    }
}
