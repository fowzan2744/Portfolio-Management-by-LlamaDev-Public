package com.backend.Portfolio_Backend.repository;

import com.backend.Portfolio_Backend.model.Portfolio;
import com.backend.Portfolio_Backend.model.PortfolioAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PortfolioAssetRepository extends JpaRepository<PortfolioAsset, Long> {

    List<PortfolioAsset> findByPortfolio(Portfolio portfolio);

    @Query("SELECT DISTINCT p.ticker FROM PortfolioAsset p")
    List<String> findDistinctTickers();
    Optional<PortfolioAsset> findByPortfolioAndTicker(Portfolio portfolio, String ticker);
}
