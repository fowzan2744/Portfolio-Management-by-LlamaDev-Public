package com.backend.Portfolio_Backend.repository;
import com.backend.Portfolio_Backend.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
