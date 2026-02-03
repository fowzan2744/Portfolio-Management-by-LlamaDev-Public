package com.backend.Portfolio_Backend.repository;

import com.backend.Portfolio_Backend.model.Portfolio;
import com.backend.Portfolio_Backend.model.PortfolioAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioAuditLogRepository extends JpaRepository<PortfolioAuditLog, Long> {

    List<PortfolioAuditLog> findByPortfolioOrderByIdAsc(Portfolio portfolio);

    Optional<PortfolioAuditLog> findTopByPortfolioOrderByIdDesc(Portfolio portfolio);
}
