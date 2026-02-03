package com.backend.Portfolio_Backend.repository;

import com.backend.Portfolio_Backend.model.IntradayPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IntradayPriceRepository
        extends JpaRepository<IntradayPrice, Long> {

    Optional<IntradayPrice> findByTickerAndTimestamp(
            String ticker,
            LocalDateTime timestamp
    );
    List<IntradayPrice> findByTickerAndTimestampBetween(
            String ticker,
            LocalDateTime start,
            LocalDateTime end
    );

    void deleteByTimestampBefore(LocalDateTime cutoff);

    @Query("SELECT ip FROM IntradayPrice ip WHERE ip.ticker = :ticker ORDER BY ip.timestamp DESC LIMIT 1")
    Optional<IntradayPrice> findLatestByTicker(@Param("ticker") String ticker);
}
