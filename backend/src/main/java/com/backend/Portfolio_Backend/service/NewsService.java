package com.backend.Portfolio_Backend.service;

import com.backend.Portfolio_Backend.dto.FinnhubNewsItemDTO;
import com.backend.Portfolio_Backend.dto.NewsItemDTO;
import com.backend.Portfolio_Backend.model.PortfolioAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private PortfolioService portfolioService;

    @Value("${finnhub.api.key:}")
    private String finnhubApiKey;

    @Value("${news.days.back:7}")
    private int daysBack;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String FINNHUB_COMPANY_NEWS_URL = "https://finnhub.io/api/v1/company-news";
    private static final String FINNHUB_MARKET_NEWS_URL = "https://finnhub.io/api/v1/news";

    /**
     * Get portfolio-specific news (cached for 15 minutes)
     * Fetches company news for all portfolio holdings
     */
    @Cacheable(value = "portfolioNews", unless = "#result == null || #result.isEmpty()")
    public List<NewsItemDTO> getPortfolioNews() {
        // Check if API key is configured
        if (finnhubApiKey == null || finnhubApiKey.trim().isEmpty()) {
            return getFallbackNews("portfolio");
        }

        try {
            // Get all portfolio holdings
            List<PortfolioAsset> assets = portfolioService.getAssets();
            if (assets.isEmpty()) {
                return Collections.emptyList();
            }

            // Get unique tickers
            Set<String> tickers = assets.stream()
                    .map(PortfolioAsset::getTicker)
                    .collect(Collectors.toSet());

            // Calculate date range
            LocalDate toDate = LocalDate.now();
            LocalDate fromDate = toDate.minusDays(daysBack);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Fetch news for each ticker
            List<NewsItemDTO> allNews = new ArrayList<>();
            for (String ticker : tickers) {
                try {
                    String url = String.format("%s?symbol=%s&from=%s&to=%s&token=%s",
                            FINNHUB_COMPANY_NEWS_URL,
                            ticker,
                            fromDate.format(formatter),
                            toDate.format(formatter),
                            finnhubApiKey);

                    FinnhubNewsItemDTO[] newsItems = restTemplate.getForObject(url, FinnhubNewsItemDTO[].class);
                    if (newsItems != null) {
                        for (FinnhubNewsItemDTO item : newsItems) {
                            allNews.add(transformToNewsItemDTO(item, ticker));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to fetch news for ticker " + ticker + ": " + e.getMessage());
                    // Continue with other tickers
                }
            }

            // Sort by datetime (newest first) and limit to top 50
            allNews.sort((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()));
            return allNews.stream().limit(50).collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Failed to fetch portfolio news: " + e.getMessage());
            return getFallbackNews("portfolio");
        }
    }

    /**
     * Get general market news (cached for 15 minutes)
     */
    @Cacheable(value = "marketNews", unless = "#result == null || #result.isEmpty()")
    public List<NewsItemDTO> getMarketNews() {
        // Check if API key is configured
        if (finnhubApiKey == null || finnhubApiKey.trim().isEmpty()) {
            return getFallbackNews("market");
        }

        try {
            String url = String.format("%s?category=general&token=%s",
                    FINNHUB_MARKET_NEWS_URL,
                    finnhubApiKey);

            FinnhubNewsItemDTO[] newsItems = restTemplate.getForObject(url, FinnhubNewsItemDTO[].class);
            if (newsItems == null || newsItems.length == 0) {
                return getFallbackNews("market");
            }

            // Transform and limit to top 50
            return Arrays.stream(newsItems)
                    .limit(50)
                    .map(item -> transformToNewsItemDTO(item, null))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Failed to fetch market news: " + e.getMessage());
            return getFallbackNews("market");
        }
    }

    /**
     * Transform Finnhub news item to frontend DTO
     */
    private NewsItemDTO transformToNewsItemDTO(FinnhubNewsItemDTO finnhubItem, String ticker) {
        NewsItemDTO dto = new NewsItemDTO();
        dto.setId(String.valueOf(finnhubItem.getId()));
        dto.setHeadline(finnhubItem.getHeadline());
        dto.setSource(finnhubItem.getSource());
        dto.setPublishedAt(formatRelativeTime(finnhubItem.getDatetime()));
        dto.setSummary(finnhubItem.getSummary());
        dto.setUrl(finnhubItem.getUrl());
        dto.setThumbnail(finnhubItem.getImage());

        // Parse related symbols
        List<String> relatedSymbols = new ArrayList<>();
        if (ticker != null) {
            relatedSymbols.add(ticker);
        }
        if (finnhubItem.getRelated() != null && !finnhubItem.getRelated().trim().isEmpty()) {
            String[] symbols = finnhubItem.getRelated().split("[,\\s]+");
            for (String symbol : symbols) {
                String cleaned = symbol.trim();
                if (!cleaned.isEmpty() && !relatedSymbols.contains(cleaned)) {
                    relatedSymbols.add(cleaned);
                }
            }
        }
        dto.setRelatedSymbols(relatedSymbols);

        return dto;
    }

    /**
     * Convert UNIX timestamp to relative time string (e.g., "2 hours ago")
     */
    private String formatRelativeTime(Long unixTimestamp) {
        if (unixTimestamp == null) {
            return "Unknown";
        }

        Instant newsTime = Instant.ofEpochSecond(unixTimestamp);
        Instant now = Instant.now();

        long minutes = ChronoUnit.MINUTES.between(newsTime, now);
        long hours = ChronoUnit.HOURS.between(newsTime, now);
        long days = ChronoUnit.DAYS.between(newsTime, now);

        if (minutes < 60) {
            return minutes <= 1 ? "1 minute ago" : minutes + " minutes ago";
        } else if (hours < 24) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (days < 7) {
            return days == 1 ? "1 day ago" : days + " days ago";
        } else {
            // Format as date for older news
            LocalDate date = newsTime.atZone(ZoneId.systemDefault()).toLocalDate();
            return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }
    }

    /**
     * Fallback news when API key is missing or API fails
     */
    private List<NewsItemDTO> getFallbackNews(String type) {
        List<NewsItemDTO> fallbackNews = new ArrayList<>();
        
        if ("portfolio".equals(type)) {
            fallbackNews.add(createFallbackNewsItem(
                    "fallback-1",
                    "Configure Finnhub API Key",
                    "System",
                    "Just now",
                    "To view real-time portfolio news, please set the FINNHUB_API_KEY environment variable. Get your free API key at https://finnhub.io",
                    "https://finnhub.io",
                    Arrays.asList("SYSTEM"),
                    null
            ));
        } else {
            fallbackNews.add(createFallbackNewsItem(
                    "fallback-2",
                    "Market News Unavailable",
                    "System",
                    "Just now",
                    "To view real-time market news, please set the FINNHUB_API_KEY environment variable. Get your free API key at https://finnhub.io",
                    "https://finnhub.io",
                    Arrays.asList("MARKET"),
                    null
            ));
        }
        
        return fallbackNews;
    }

    private NewsItemDTO createFallbackNewsItem(String id, String headline, String source, 
                                                String publishedAt, String summary, String url,
                                                List<String> relatedSymbols, String thumbnail) {
        return new NewsItemDTO(id, headline, source, publishedAt, summary, url, relatedSymbols, thumbnail);
    }
}
