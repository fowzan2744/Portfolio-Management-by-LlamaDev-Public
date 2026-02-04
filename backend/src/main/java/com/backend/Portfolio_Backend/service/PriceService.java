package com.backend.Portfolio_Backend.service;

import com.backend.Portfolio_Backend.dto.AssetDetailDTO;
import com.backend.Portfolio_Backend.model.DailyPrice;
import com.backend.Portfolio_Backend.model.IntradayPrice;
import com.backend.Portfolio_Backend.model.PortfolioAsset;
import com.backend.Portfolio_Backend.repository.DailyPriceRepository;
import com.backend.Portfolio_Backend.repository.IntradayPriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate; 
import java.util.List;
import java.util.Optional;

@Service
public class PriceService {

    private final IntradayPriceRepository intradayPriceRepository;
    private final DailyPriceRepository dailyPriceRepository;

    public PriceService(IntradayPriceRepository intradayPriceRepository, 
                       DailyPriceRepository dailyPriceRepository) {
        this.intradayPriceRepository = intradayPriceRepository;
        this.dailyPriceRepository = dailyPriceRepository;
    }

    /**
     * Get the latest price for a ticker from intraday prices
     */
    public Double getLatestPrice(String ticker) {
        Optional<IntradayPrice> latestPrice = intradayPriceRepository
                .findLatestByTicker(ticker);

        if (latestPrice.isPresent()) {
            return latestPrice.get().getClose();
        }

        // If no intraday price exists, try daily price
        Optional<DailyPrice> latestDailyPrice = dailyPriceRepository
                .findLatestByTickerAndDate(ticker, LocalDate.now());
        
        if (latestDailyPrice.isPresent()) {
            return latestDailyPrice.get().getClose();
        }

        return null;
    }

    /**
     * Get yesterday's closing price for a ticker
     */
    public Double getYesterdayPrice(String ticker) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Optional<DailyPrice> yesterdayPrice = dailyPriceRepository
                .findLatestByTickerAndDate(ticker, yesterday);
        
        if (yesterdayPrice.isPresent()) {
            return yesterdayPrice.get().getClose();
        }
        
        // If no yesterday price, try to get the latest available daily price before today
        List<DailyPrice> recentPrices = dailyPriceRepository
                .findByTickerAndPriceDateBetween(ticker, LocalDate.now().minusDays(7), LocalDate.now().minusDays(1));
        
        if (!recentPrices.isEmpty()) {
            // Get the most recent price (should be sorted by date)
            return recentPrices.stream()
                    .max(java.util.Comparator.comparing(DailyPrice::getPriceDate))
                    .map(DailyPrice::getClose)
                    .orElse(null);
        }
        
        return null;
    }

    /**
     * Get today's opening price for a ticker
     */
    public Double getTodayOpeningPrice(String ticker) {
        LocalDate today = LocalDate.now();
        Optional<DailyPrice> todayPrice = dailyPriceRepository
                .findByTickerAndPriceDate(ticker, today);
        
        if (todayPrice.isPresent()) {
            return todayPrice.get().getOpen();
        }
        
        // Try to get from intraday prices
        List<IntradayPrice> intradayPrices = intradayPriceRepository
                .findByTickerAndTimestampBetween(
                        ticker, 
                        today.atStartOfDay(), 
                        java.time.LocalDateTime.now());
        
        if (!intradayPrices.isEmpty()) {
            Optional<IntradayPrice> firstIntraday = intradayPrices.stream()
                    .min(java.util.Comparator.comparing(IntradayPrice::getTimestamp));
            
            if (firstIntraday.isPresent()) {
                return firstIntraday.get().getOpen();
            }
        }
        
        return null;
    }

    /**
     * Calculate daily change for an asset
     * Simple calculation: today's price - yesterday's price
     */
    public Double[] calculateDailyChange(String ticker, Double currentPrice) {
        if (currentPrice == null || currentPrice == 0) {
            return new Double[]{0.0, 0.0};
        }
        
        // Get yesterday's closing price
        Double yesterdayPrice = getYesterdayPrice(ticker);
        
        // If no yesterday price available, return 0
        if (yesterdayPrice == null || yesterdayPrice == 0) {
            return new Double[]{0.0, 0.0};
        }
        
        // Calculate change: today's price - yesterday's price
        Double change = currentPrice - yesterdayPrice;
        Double changePercent = (change / yesterdayPrice) * 100;
        
        return new Double[]{change, changePercent};
    }

    /**
     * Convert PortfolioAsset to AssetDetailDTO with computed values
     */
    public AssetDetailDTO toAssetDetailDTO(PortfolioAsset asset) {
        Double currentPrice = asset.getCurrentPrice();
        
        // If currentPrice is not set, try to fetch from latest intraday price
        if (currentPrice == null || currentPrice == 0) {
            Double latestPrice = getLatestPrice(asset.getTicker());
            if (latestPrice != null) {
                currentPrice = latestPrice;
            } else {
                currentPrice = asset.getAvgBuyPrice();
            }
        }

        Double investedValue = asset.getInvestedValue();
        Double currentValue = asset.getQuantity() * currentPrice;
        Double profitLoss = currentValue - investedValue;
        Double profitLossPercent = investedValue > 0 ? (profitLoss / investedValue) * 100 : 0;

        return new AssetDetailDTO(
                asset.getId(),
                asset.getTicker(),
                asset.getQuantity(),
                asset.getAvgBuyPrice(),
                currentPrice,
                currentValue,
                investedValue,
                profitLoss,
                profitLossPercent
        );
    }
}
