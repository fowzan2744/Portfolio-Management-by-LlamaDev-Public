package com.backend.Portfolio_Backend.service;

import com.backend.Portfolio_Backend.model.DailyPrice;
import com.backend.Portfolio_Backend.model.IntradayPrice;
import com.backend.Portfolio_Backend.repository.DailyPriceRepository;
import com.backend.Portfolio_Backend.repository.IntradayPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class PriceIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(PriceIngestionService.class);
    private final IntradayPriceRepository intraRepo;
    private final DailyPriceRepository dailyRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    public PriceIngestionService(
            IntradayPriceRepository intraRepo,
            DailyPriceRepository dailyRepo
    ) {
        this.intraRepo = intraRepo;
        this.dailyRepo = dailyRepo;
    }

    @SuppressWarnings("unchecked")
    public void fetchAndStore(String ticker) {
        try {
            String url =
                    "https://c4rm9elh30.execute-api.us-east-1.amazonaws.com/default/cachedPriceData?ticker="
                            + ticker;

            logger.info("🔄 Fetching price data for ticker: {}", ticker);
            Map<String, Object> response =
                    restTemplate.getForObject(url, Map.class);

            if (response == null) {
                logger.warn("⚠️ Null response from API for ticker: {}", ticker);
                return;
            }

            Map<String, Object> priceData =
                    (Map<String, Object>) response.get("price_data");

            if (priceData == null) {
                logger.warn("⚠️ No 'price_data' field in response for ticker: {}", ticker);
                return;
            }

            List<String> timestamps = (List<String>) priceData.get("timestamp");
            List<Object> open = (List<Object>) priceData.get("open");
            List<Object> high = (List<Object>) priceData.get("high");
            List<Object> low = (List<Object>) priceData.get("low");
            List<Object> close = (List<Object>) priceData.get("close");
            List<Object> volume = (List<Object>) priceData.get("volume");

            if (timestamps == null || timestamps.isEmpty()) {
                logger.warn("⚠️ Empty or null timestamps for ticker: {}", ticker);
                return;
            }

            LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
            LocalDate sevenDaysAgo = todayUtc.minusDays(6); // Include last 7 days
            int upserted = 0;

            for (int i = 0; i < timestamps.size(); i++) {

                LocalDateTime tsUtc =
                        LocalDateTime.parse(timestamps.get(i).replace(" ", "T"));
                LocalDate tsDate = tsUtc.toLocalDate();

                // OPTION B: Only process data from the last 7 days
                if (tsDate.isBefore(sevenDaysAgo)) continue;

                IntradayPrice price =
                        intraRepo.findByTickerAndTimestamp(ticker, tsUtc)
                                .orElseGet(IntradayPrice::new);

                price.setTicker(ticker);
                price.setTimestamp(tsUtc);
                price.setOpen(((Number) open.get(i)).doubleValue());
                price.setHigh(((Number) high.get(i)).doubleValue());
                price.setLow(((Number) low.get(i)).doubleValue());
                price.setClose(((Number) close.get(i)).doubleValue());
                price.setVolume(((Number) volume.get(i)).longValue());

                intraRepo.save(price);
                upserted++;
            }

            logger.info("✅ Upserted {} intraday rows for ticker: {}", upserted, ticker);

            aggregateDaily(ticker);
            cleanupOldIntraday();
        } catch (Exception e) {
            logger.error("❌ Error fetching/storing price data for ticker: {}", ticker, e);
        }
    }

    private void aggregateDaily(String ticker) {
        try {
            LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

            // Aggregate for the last 7 days (including today).
            for (int daysBack = 0; daysBack < 7; daysBack++) {
                LocalDate date = todayUtc.minusDays(daysBack);
                // Skip if a daily record already exists for this ticker/date
                if (dailyRepo.findByTickerAndPriceDate(ticker, date).isPresent()) {
                    logger.debug("⏭️ Daily record already exists for {} on {}", ticker, date);
                    continue;
                }

                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();

                List<IntradayPrice> prices =
                        intraRepo.findByTickerAndTimestampBetween(ticker, start, end);

                if (prices.isEmpty()) {
                    logger.debug("ℹ️ No intraday data for {} on {}", ticker, date);
                    continue;
                }

                DailyPrice daily = new DailyPrice();
                daily.setTicker(ticker);
                daily.setPriceDate(date);
                daily.setOpen(prices.get(0).getOpen());
                daily.setClose(prices.get(prices.size() - 1).getClose());
                daily.setHigh(prices.stream().mapToDouble(IntradayPrice::getHigh).max().orElse(0));
                daily.setLow(prices.stream().mapToDouble(IntradayPrice::getLow).min().orElse(0));
                daily.setVolume(prices.stream().mapToLong(IntradayPrice::getVolume).sum());

                dailyRepo.save(daily);
                logger.info("✅ Saved DailyPrice for {} on {} ({} intraday records)", ticker, date, prices.size());
            }
        } catch (Exception e) {
            logger.error("❌ Error aggregating daily prices for ticker: {}", ticker, e);
        }
    }

    private void cleanupOldIntraday() {
        LocalDateTime cutoff =
                LocalDate.now(ZoneOffset.UTC).minusDays(7).atStartOfDay();
        intraRepo.deleteByTimestampBefore(cutoff);
    }
}
