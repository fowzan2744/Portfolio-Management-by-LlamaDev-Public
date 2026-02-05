package com.backend.Portfolio_Backend.controller;

import com.backend.Portfolio_Backend.dto.*;
import com.backend.Portfolio_Backend.model.DailyPrice;
import com.backend.Portfolio_Backend.model.IntradayPrice;
import com.backend.Portfolio_Backend.model.PortfolioAsset;
import com.backend.Portfolio_Backend.repository.DailyPriceRepository;
import com.backend.Portfolio_Backend.repository.IntradayPriceRepository;
import com.backend.Portfolio_Backend.service.PortfolioService;
import com.backend.Portfolio_Backend.service.PriceService;
import com.backend.Portfolio_Backend.service.TickerNameService;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PriceService priceService;
    private final TickerNameService tickerNameService;
    private final DailyPriceRepository dailyPriceRepository;
    private final IntradayPriceRepository intradayPriceRepository;

    public PortfolioController(PortfolioService portfolioService, 
                              PriceService priceService,
                              TickerNameService tickerNameService,
                              DailyPriceRepository dailyPriceRepository,
                              IntradayPriceRepository intradayPriceRepository) {
        this.portfolioService = portfolioService;
        this.priceService = priceService;
        this.tickerNameService = tickerNameService;
        this.dailyPriceRepository = dailyPriceRepository;
        this.intradayPriceRepository = intradayPriceRepository;
    }

    // 🔹 Get holdings in frontend format
    @GetMapping("/holdings")
    public List<HoldingDTO> getHoldings() {
        List<PortfolioAsset> assets = portfolioService.getAssets();
        if (assets.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate total portfolio value first
        Map<String, Double> currentPrices = new HashMap<>();
        Map<String, Double[]> dailyChanges = new HashMap<>();

        for (PortfolioAsset asset : assets) {
            Double currentPrice = priceService.getLatestPrice(asset.getTicker());
            if (currentPrice == null || currentPrice == 0) {
                currentPrice = asset.getAvgBuyPrice();
            }
            currentPrices.put(asset.getTicker(), currentPrice);
            
            // Calculate daily change
            Double[] change = priceService.calculateDailyChange(asset.getTicker(), currentPrice);
            dailyChanges.put(asset.getTicker(), change);
        }

        // Calculate total value (must be final for lambda)
        final double totalValue = assets.stream()
                .mapToDouble(asset -> {
                    Double currentPrice = currentPrices.get(asset.getTicker());
                    return asset.getQuantity() * currentPrice;
                })
                .sum();

        // Convert to HoldingDTO with allocations
        return assets.stream().map(asset -> {
            Double currentPrice = currentPrices.get(asset.getTicker());
            Double value = asset.getQuantity() * currentPrice;
            Double allocation = totalValue > 0 ? (value / totalValue) * 100 : 0.0;
            
            Double[] change = dailyChanges.get(asset.getTicker());
            Double dailyChange = change[0] * asset.getQuantity(); // Total dollar change
            Double dailyChangePercent = change[1];

            return new HoldingDTO(
                    asset.getId().toString(),
                    asset.getTicker(),
                    tickerNameService.getNameForTicker(asset.getTicker()),
                    asset.getQuantity(),
                    asset.getAvgBuyPrice(),
                    currentPrice,
                    value,
                    allocation,
                    dailyChange,
                    dailyChangePercent
            );
        }).collect(Collectors.toList());
    }

    // 🔹 Get portfolio summary (total value, profit/loss, etc.)
    @GetMapping("/summary")
    public PortfolioSummaryDTO getSummary() {
        List<HoldingDTO> holdings = getHoldings();
        
        if (holdings.isEmpty()) {
            return new PortfolioSummaryDTO(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "N/A");
        }

        double totalValue = holdings.stream().mapToDouble(HoldingDTO::getValue).sum();
        double totalInvested = holdings.stream()
                .mapToDouble(h -> h.getShares() * h.getAvgCost())
                .sum();
        double totalGain = totalValue - totalInvested;
        double totalGainPercent = totalInvested > 0 ? (totalGain / totalInvested) * 100 : 0;
        
        // Calculate daily change
        double dailyChange = holdings.stream().mapToDouble(HoldingDTO::getDailyChange).sum();
        double dailyChangePercent = totalValue > 0 ? (dailyChange / (totalValue - dailyChange)) * 100 : 0;

        List<PortfolioAsset> assets = portfolioService.getAssets();
        String dataAvailableSince = assets.isEmpty() ? "N/A" :
                assets.stream()
                        .map(a -> a.getCreatedAt())
                        .min(LocalDateTime::compareTo)
                        .map(dt -> dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                        .orElse("N/A");

        return new PortfolioSummaryDTO(
                totalValue,
                totalInvested,
                totalGain,
                totalGainPercent,
                dailyChange,
                dailyChangePercent,
                dataAvailableSince
        );
    }


    @GetMapping("/growth")
    public List<PortfolioGrowthDTO> getPortfolioGrowth(
            @RequestParam(defaultValue = "1D") String range
    ) {

        List<PortfolioAsset> assets = portfolioService.getAssets();
        if (assets.isEmpty()) {
            return new ArrayList<>();
        }

        List<PortfolioGrowthDTO> growthData = new ArrayList<>();

        // ======================================================
        // ✅ 1D RANGE (Intraday) - Use actual timestamps from DB
        // ======================================================
        if ("1D".equals(range)) {

            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime now = LocalDateTime.now();

            // Step 1: Collect all intraday prices for all assets
            Map<String, List<IntradayPrice>> intradayData = new HashMap<>();
            Set<LocalDateTime> allTimestamps = new TreeSet<>();

            for (PortfolioAsset asset : assets) {
                List<IntradayPrice> prices =
                        intradayPriceRepository.findByTickerAndTimestampBetween(
                                asset.getTicker(),
                                todayStart,
                                now
                        );

                if (!prices.isEmpty()) {
                    intradayData.put(asset.getTicker(), prices);
                    // Collect all unique timestamps
                    prices.forEach(p -> allTimestamps.add(p.getTimestamp()));
                }
            }

            // Step 2: If we have actual data, use it. Otherwise, create fallback intervals
            if (!allTimestamps.isEmpty()) {
                // Group timestamps into 15-minute buckets to avoid too many data points
                Map<LocalDateTime, List<LocalDateTime>> timeBuckets = new TreeMap<>();

                for (LocalDateTime timestamp : allTimestamps) {
                    // Round down to nearest 15 minutes
                    int minute = timestamp.getMinute();
                    int roundedMinute = (minute / 15) * 15;
                    LocalDateTime bucket = timestamp.withMinute(roundedMinute).withSecond(0).withNano(0);
                    timeBuckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(timestamp);
                }

                // Step 3: For each time bucket, calculate portfolio value
                for (Map.Entry<LocalDateTime, List<LocalDateTime>> bucketEntry : timeBuckets.entrySet()) {
                    LocalDateTime bucketTime = bucketEntry.getKey();
                    // Use the latest timestamp in this bucket
                    LocalDateTime actualTime = bucketEntry.getValue().stream()
                            .max(Comparator.naturalOrder())
                            .orElse(bucketTime);

                    String timeKey = String.format("%d:%02d", actualTime.getHour(), actualTime.getMinute());
                    double portfolioValue = 0;

                    for (PortfolioAsset asset : assets) {
                        List<IntradayPrice> prices = intradayData.get(asset.getTicker());

                        if (prices != null && !prices.isEmpty()) {
                            // Find the closest price at or before this bucket time
                            Optional<IntradayPrice> closest = prices.stream()
                                    .filter(p -> !p.getTimestamp().isAfter(actualTime))
                                    .max(Comparator.comparing(IntradayPrice::getTimestamp));

                            if (closest.isPresent()) {
                                portfolioValue += asset.getQuantity() * closest.get().getClose();
                            } else {
                                // Fallback: use latest available price
                                Double latestPrice = priceService.getLatestPrice(asset.getTicker());
                                portfolioValue += asset.getQuantity() *
                                        (latestPrice != null ? latestPrice : asset.getAvgBuyPrice());
                            }
                        } else {
                            // No intraday data for this ticker, use latest price
                            Double latestPrice = priceService.getLatestPrice(asset.getTicker());
                            portfolioValue += asset.getQuantity() *
                                    (latestPrice != null ? latestPrice : asset.getAvgBuyPrice());
                        }
                    }

                    growthData.add(new PortfolioGrowthDTO(timeKey, portfolioValue));
                }
            } else {
                // No intraday data for today - try yesterday as fallback
                LocalDate yesterday = LocalDate.now().minusDays(1);
                LocalDateTime yesterdayStart = yesterday.atStartOfDay();
                LocalDateTime yesterdayEnd = yesterday.plusDays(1).atStartOfDay();

                Map<String, List<IntradayPrice>> yesterdayData = new HashMap<>();
                Set<LocalDateTime> yesterdayTimestamps = new TreeSet<>();

                for (PortfolioAsset asset : assets) {
                    List<IntradayPrice> prices =
                            intradayPriceRepository.findByTickerAndTimestampBetween(
                                    asset.getTicker(),
                                    yesterdayStart,
                                    yesterdayEnd
                            );

                    if (!prices.isEmpty()) {
                        yesterdayData.put(asset.getTicker(), prices);
                        prices.forEach(p -> yesterdayTimestamps.add(p.getTimestamp()));
                    }
                }

                if (!yesterdayTimestamps.isEmpty()) {
                    // Use yesterday's data grouped into 15-minute buckets
                    Map<LocalDateTime, List<LocalDateTime>> timeBuckets = new TreeMap<>();

                    for (LocalDateTime timestamp : yesterdayTimestamps) {
                        int minute = timestamp.getMinute();
                        int roundedMinute = (minute / 15) * 15;
                        LocalDateTime bucket = timestamp.withMinute(roundedMinute).withSecond(0).withNano(0);
                        timeBuckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(timestamp);
                    }

                    for (Map.Entry<LocalDateTime, List<LocalDateTime>> bucketEntry : timeBuckets.entrySet()) {
                        LocalDateTime actualTime = bucketEntry.getValue().stream()
                                .max(Comparator.naturalOrder())
                                .orElse(bucketEntry.getKey());

                        String timeKey = String.format("%d:%02d", actualTime.getHour(), actualTime.getMinute());
                        double portfolioValue = 0;

                        for (PortfolioAsset asset : assets) {
                            List<IntradayPrice> prices = yesterdayData.get(asset.getTicker());

                            if (prices != null && !prices.isEmpty()) {
                                Optional<IntradayPrice> closest = prices.stream()
                                        .filter(p -> !p.getTimestamp().isAfter(actualTime))
                                        .max(Comparator.comparing(IntradayPrice::getTimestamp));

                                if (closest.isPresent()) {
                                    portfolioValue += asset.getQuantity() * closest.get().getClose();
                                } else {
                                    Double latestPrice = priceService.getLatestPrice(asset.getTicker());
                                    portfolioValue += asset.getQuantity() *
                                            (latestPrice != null ? latestPrice : asset.getAvgBuyPrice());
                                }
                            } else {
                                Double latestPrice = priceService.getLatestPrice(asset.getTicker());
                                portfolioValue += asset.getQuantity() *
                                        (latestPrice != null ? latestPrice : asset.getAvgBuyPrice());
                            }
                        }

                        growthData.add(new PortfolioGrowthDTO(timeKey, portfolioValue));
                    }
                } else {
                    // No intraday data at all - show current portfolio value as single point
                    double portfolioValue = 0;
                    for (PortfolioAsset asset : assets) {
                        Double price = priceService.getLatestPrice(asset.getTicker());
                        if (price == null) price = asset.getAvgBuyPrice();
                        portfolioValue += asset.getQuantity() * price;
                    }

                    LocalDateTime currentTime = LocalDateTime.now();
                    String timeKey = String.format("%d:%02d", currentTime.getHour(), currentTime.getMinute());
                    growthData.add(new PortfolioGrowthDTO(timeKey, portfolioValue));
                }
            }
        }

        // ======================================================
        // ✅ 1W RANGE (Last 5 Trading Days)
        // ======================================================
        else if ("1W".equals(range)) {

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(10);

            Map<String, List<DailyPrice>> dailyData = new HashMap<>();

            for (PortfolioAsset asset : assets) {
                List<DailyPrice> prices =
                        dailyPriceRepository.findByTickerAndPriceDateBetween(
                                asset.getTicker(),
                                startDate,
                                endDate
                        );

                if (!prices.isEmpty()) {
                    dailyData.put(asset.getTicker(), prices);
                }
            }

            // Collect last 5 trading days
            List<LocalDate> tradingDays = new ArrayList<>();
            LocalDate date = endDate;

            while (tradingDays.size() < 5) {

                if (!(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY)) {

                    tradingDays.add(date);
                }

                date = date.minusDays(1);
            }

            Collections.reverse(tradingDays);

            Map<String, Double> dayValueMap = new LinkedHashMap<>();

            for (LocalDate day : tradingDays) {

                String label =
                        day.getDayOfWeek().toString().substring(0, 3);

                double portfolioValue = 0;

                for (PortfolioAsset asset : assets) {

                    List<DailyPrice> prices =
                            dailyData.get(asset.getTicker());

                    if (prices != null && !prices.isEmpty()) {

                        Optional<DailyPrice> match =
                                prices.stream()
                                        .filter(p -> p.getPriceDate().equals(day))
                                        .findFirst();

                        if (match.isPresent()) {
                            portfolioValue += asset.getQuantity() * match.get().getClose();
                        } else {
                            Double latestPrice =
                                    priceService.getLatestPrice(asset.getTicker());

                            portfolioValue += asset.getQuantity() *
                                    (latestPrice != null
                                            ? latestPrice
                                            : asset.getAvgBuyPrice());
                        }

                    } else {
                        Double latestPrice =
                                priceService.getLatestPrice(asset.getTicker());

                        portfolioValue += asset.getQuantity() *
                                (latestPrice != null
                                        ? latestPrice
                                        : asset.getAvgBuyPrice());
                    }
                }

                dayValueMap.put(label, portfolioValue);
            }

            growthData = dayValueMap.entrySet().stream()
                    .map(e -> new PortfolioGrowthDTO(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }

        return growthData;
    }

    // 🔹 Get asset allocation data
    @GetMapping("/allocation")
    public List<AllocationDTO> getAllocation() {
        List<HoldingDTO> holdings = getHoldings();
        return holdings.stream()
                .map(h -> new AllocationDTO(h.getSymbol(), h.getAllocation(), h.getName()))
                .collect(Collectors.toList());
    }

    // 🔹 Get current portfolio holdings (basic info) - kept for backward compatibility
    @GetMapping("/assets")
    public List<PortfolioAsset> getAssets() {
        return portfolioService.getAssets();
    }

    // 🔹 Get assets with current prices and P&L calculated - kept for backward compatibility
    @GetMapping("/assets/detailed")
    public List<AssetDetailDTO> getAssetsDetailed() {
        List<PortfolioAsset> assets = portfolioService.getAssets();
        return assets.stream()
                .map(priceService::toAssetDetailDTO)
                .collect(Collectors.toList());
    }

    // 🔹 Get a specific asset with current price and P&L
    @GetMapping("/assets/{id}")
    public AssetDetailDTO getAssetDetailed(@PathVariable Long id) {
        PortfolioAsset asset = portfolioService.getAssetById(id);
        return priceService.toAssetDetailDTO(asset);
    }

    // 🔹 Get current price for a specific ticker
    @GetMapping("/prices/{ticker}")
    public PriceResponseDTO getCurrentPrice(@PathVariable String ticker) {
        Double price = priceService.getLatestPrice(ticker);
        if (price == null) {
            return new PriceResponseDTO(ticker, null, "No price data available");
        }
        return new PriceResponseDTO(ticker, price, "OK");
    }

    // 🔹 Add asset to portfolio
    @PostMapping("/assets")
    public HoldingDTO addAsset(@RequestBody AddAssetRequest request) {
        portfolioService.addAsset(
                request.getTicker(),
                request.getQuantity(),
                request.getAvgBuyPrice()
        );
        // Return the newly added/updated asset in frontend format
        List<HoldingDTO> holdings = getHoldings();
        return holdings.stream()
                .filter(h -> h.getSymbol().equalsIgnoreCase(request.getTicker()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Asset not found after adding"));
    }

    // 🔹 Remove asset from portfolio
    @DeleteMapping("/assets")
    public ApiResponse removeAsset(@RequestBody RemoveAssetRequest request) {
        portfolioService.removeAsset(
                request.getTicker(),
                request.getQuantity()
        );
        return new ApiResponse("Asset removed successfully");
    }

    // ================= DTOs =================

    public static class AddAssetRequest {
        private String ticker;
        private int quantity;
        private double avgBuyPrice;

        public String getTicker() {
            return ticker;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getAvgBuyPrice() {
            return avgBuyPrice;
        }

        public void setAvgBuyPrice(double avgBuyPrice) {
            this.avgBuyPrice = avgBuyPrice;
        }
    }

    public static class RemoveAssetRequest {
        private String ticker;
        private int quantity;

        public String getTicker() {
            return ticker;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class PriceResponseDTO {
        private String ticker;
        private Double currentPrice;
        private String status;

        public PriceResponseDTO() {
        }

        public PriceResponseDTO(String ticker, Double currentPrice, String status) {
            this.ticker = ticker;
            this.currentPrice = currentPrice;
            this.status = status;
        }

        public String getTicker() {
            return ticker;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public Double getCurrentPrice() {
            return currentPrice;
        }

        public void setCurrentPrice(Double currentPrice) {
            this.currentPrice = currentPrice;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ApiResponse {
        private String message;

        public ApiResponse() {
        }

        public ApiResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
} 