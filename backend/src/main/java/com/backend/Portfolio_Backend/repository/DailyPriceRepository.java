package com.backend.Portfolio_Backend.repository;

import com.backend.Portfolio_Backend.model.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyPriceRepository
        extends JpaRepository<DailyPrice, Long> {

    Optional<DailyPrice> findByTickerAndPriceDate(
            String ticker,
            LocalDate priceDate
    );

    List<DailyPrice> findByTickerAndPriceDateBetween(
            String ticker,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query(value = "SELECT * FROM daily_price WHERE ticker = :ticker AND price_date <= :date ORDER BY price_date DESC LIMIT 1", nativeQuery = true)
    Optional<DailyPrice> findLatestByTickerAndDate(@Param("ticker") String ticker, @Param("date") LocalDate date);
}
