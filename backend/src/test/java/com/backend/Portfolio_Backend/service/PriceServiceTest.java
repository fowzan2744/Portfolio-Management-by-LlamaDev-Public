package com.backend.Portfolio_Backend.service;

import com.backend.Portfolio_Backend.dto.AssetDetailDTO;
import com.backend.Portfolio_Backend.model.DailyPrice;
import com.backend.Portfolio_Backend.model.IntradayPrice;
import com.backend.Portfolio_Backend.model.PortfolioAsset;
import com.backend.Portfolio_Backend.repository.DailyPriceRepository;
import com.backend.Portfolio_Backend.repository.IntradayPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private IntradayPriceRepository intradayPriceRepository;

    @Mock
    private DailyPriceRepository dailyPriceRepository;

    @InjectMocks
    private PriceService priceService;

    private IntradayPrice testIntradayPrice;
    private DailyPrice testDailyPrice;
    private PortfolioAsset testAsset;

    @BeforeEach
    void setUp() {
        testIntradayPrice = new IntradayPrice();
        testIntradayPrice.setId(1L);
        testIntradayPrice.setTicker("AAPL");
        testIntradayPrice.setOpen(150.0);
        testIntradayPrice.setClose(155.0);
        testIntradayPrice.setHigh(156.0);
        testIntradayPrice.setLow(149.0);
        testIntradayPrice.setTimestamp(LocalDateTime.now());

        testDailyPrice = new DailyPrice();
        testDailyPrice.setId(1L);
        testDailyPrice.setTicker("AAPL");
        testDailyPrice.setOpen(150.0);
        testDailyPrice.setClose(153.0);
        testDailyPrice.setHigh(155.0);
        testDailyPrice.setLow(149.0);
        testDailyPrice.setPriceDate(LocalDate.now());
        testDailyPrice.setVolume(100000L);

        testAsset = new PortfolioAsset();
        testAsset.setId(1L);
        testAsset.setTicker("AAPL");
        testAsset.setQuantity(10);
        testAsset.setAvgBuyPrice(150.0);
        testAsset.setCurrentPrice(155.0);
    }

    // ===================== getLatestPrice Tests =====================

    @Test
    void testGetLatestPrice_FromIntradayPrice() {
        // Arrange
        when(intradayPriceRepository.findLatestByTicker("AAPL"))
                .thenReturn(Optional.of(testIntradayPrice));

        // Act
        Double result = priceService.getLatestPrice("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals(155.0, result);
        verify(intradayPriceRepository, times(1)).findLatestByTicker("AAPL");
        verify(dailyPriceRepository, never()).findLatestByTickerAndDate(anyString(), any());
    }

    @Test
    void testGetLatestPrice_FromDailyPrice_WhenNoIntradayPrice() {
        // Arrange
        when(intradayPriceRepository.findLatestByTicker("AAPL"))
                .thenReturn(Optional.empty());
        when(dailyPriceRepository.findLatestByTickerAndDate("AAPL", LocalDate.now()))
                .thenReturn(Optional.of(testDailyPrice));

        // Act
        Double result = priceService.getLatestPrice("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals(153.0, result);
        verify(dailyPriceRepository, times(1)).findLatestByTickerAndDate("AAPL", LocalDate.now());
    }

    @Test
    void testGetLatestPrice_ReturnsNull_WhenNoPrice() {
        // Arrange
        when(intradayPriceRepository.findLatestByTicker("UNKNOWN"))
                .thenReturn(Optional.empty());
        when(dailyPriceRepository.findLatestByTickerAndDate("UNKNOWN", LocalDate.now()))
                .thenReturn(Optional.empty());

        // Act
        Double result = priceService.getLatestPrice("UNKNOWN");

        // Assert
        assertNull(result);
    }

    // ===================== getYesterdayPrice Tests =====================

    @Test
    void testGetYesterdayPrice_Direct() {
        // Arrange
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DailyPrice yesterdayPrice = new DailyPrice();
        yesterdayPrice.setClose(152.0);
        yesterdayPrice.setPriceDate(yesterday);

        when(dailyPriceRepository.findLatestByTickerAndDate("AAPL", yesterday))
                .thenReturn(Optional.of(yesterdayPrice));

        // Act
        Double result = priceService.getYesterdayPrice("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals(152.0, result);
    }

    @Test
    void testGetYesterdayPrice_FromRecentPrices() {
        // Arrange
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(dailyPriceRepository.findLatestByTickerAndDate("AAPL", yesterday))
                .thenReturn(Optional.empty());

        DailyPrice recentPrice = new DailyPrice();
        recentPrice.setClose(151.0);
        recentPrice.setPriceDate(yesterday.minusDays(1));

        List<DailyPrice> recentPrices = List.of(recentPrice);
        when(dailyPriceRepository.findByTickerAndPriceDateBetween(
                "AAPL", 
                LocalDate.now().minusDays(7), 
                LocalDate.now().minusDays(1)))
                .thenReturn(recentPrices);

        // Act
        Double result = priceService.getYesterdayPrice("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals(151.0, result);
    }

    @Test
    void testGetYesterdayPrice_ReturnsNull_WhenNoPrice() {
        // Arrange
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(dailyPriceRepository.findLatestByTickerAndDate("UNKNOWN", yesterday))
                .thenReturn(Optional.empty());
        when(dailyPriceRepository.findByTickerAndPriceDateBetween(anyString(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        Double result = priceService.getYesterdayPrice("UNKNOWN");

        // Assert
        assertNull(result);
    }

    // ===================== getTodayOpeningPrice Tests =====================

    @Test
    void testGetTodayOpeningPrice_FromDailyPrice() {
        // Arrange
        when(dailyPriceRepository.findByTickerAndPriceDate("AAPL", LocalDate.now()))
                .thenReturn(Optional.of(testDailyPrice));

        // Act
        Double result = priceService.getTodayOpeningPrice("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals(150.0, result);
    }
    @Test
    void testGetTodayOpeningPrice_FromIntradayPrice_WhenNoDailyPrice() {
        // Arrange
        when(dailyPriceRepository.findByTickerAndPriceDate("AAPL", LocalDate.now()))
                .thenReturn(Optional.empty());

        when(intradayPriceRepository.findByTickerAndTimestampBetween(
                eq("AAPL"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of(testIntradayPrice));

        // Act
        Double result = priceService.getTodayOpeningPrice("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals(150.0, result);
    }


    @Test
    void testGetTodayOpeningPrice_ReturnsNull_WhenNoPrice() {
        // Arrange
        when(dailyPriceRepository.findByTickerAndPriceDate("UNKNOWN", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(intradayPriceRepository.findByTickerAndTimestampBetween(anyString(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        Double result = priceService.getTodayOpeningPrice("UNKNOWN");

        // Assert
        assertNull(result);
    }

    // ===================== calculateDailyChange Tests =====================

    @Test
    void testCalculateDailyChange_PositiveChange() {
        // Arrange
        when(dailyPriceRepository.findLatestByTickerAndDate(anyString(), any()))
                .thenReturn(Optional.empty());
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DailyPrice yesterdayPrice = new DailyPrice();
        yesterdayPrice.setClose(150.0);
        yesterdayPrice.setPriceDate(yesterday);

        when(dailyPriceRepository.findByTickerAndPriceDateBetween(anyString(), any(), any()))
                .thenReturn(List.of(yesterdayPrice));

        // Act
        Double[] result = priceService.calculateDailyChange("AAPL", 155.0);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(5.0, result[0]); // Change: 155 - 150
        assertEquals(3.33, result[1], 0.01); // Percent: (5 / 150) * 100
    }

    @Test
    void testCalculateDailyChange_NegativeChange() {
        // Arrange
        when(dailyPriceRepository.findLatestByTickerAndDate(anyString(), any()))
                .thenReturn(Optional.empty());
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DailyPrice yesterdayPrice = new DailyPrice();
        yesterdayPrice.setClose(160.0);
        yesterdayPrice.setPriceDate(yesterday);

        when(dailyPriceRepository.findByTickerAndPriceDateBetween(anyString(), any(), any()))
                .thenReturn(List.of(yesterdayPrice));

        // Act
        Double[] result = priceService.calculateDailyChange("AAPL", 155.0);

        // Assert
        assertNotNull(result);
        assertEquals(-5.0, result[0]); // Change: 155 - 160
        assertEquals(-3.125, result[1], 0.001); // Percent: (-5 / 160) * 100
    }

    @Test
    void testCalculateDailyChange_NullCurrentPrice() {
        // Act
        Double[] result = priceService.calculateDailyChange("AAPL", null);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void testCalculateDailyChange_ZeroCurrentPrice() {
        // Act
        Double[] result = priceService.calculateDailyChange("AAPL", 0.0);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    @Test
    void testCalculateDailyChange_NoYesterdayPrice() {
        // Arrange
        when(dailyPriceRepository.findLatestByTickerAndDate(anyString(), any()))
                .thenReturn(Optional.empty());
        when(dailyPriceRepository.findByTickerAndPriceDateBetween(anyString(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        Double[] result = priceService.calculateDailyChange("AAPL", 155.0);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result[0]);
        assertEquals(0.0, result[1]);
    }

    // ===================== toAssetDetailDTO Tests =====================

    @Test
    void testToAssetDetailDTO_WithCurrentPrice() {
        // Arrange
        testAsset.setCurrentPrice(160.0);

        // Act
        AssetDetailDTO result = priceService.toAssetDetailDTO(testAsset);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AAPL", result.getTicker());
        assertEquals(10, result.getQuantity());
        assertEquals(150.0, result.getAvgBuyPrice());
        assertEquals(160.0, result.getCurrentPrice());
        assertEquals(1600.0, result.getCurrentValue()); // 10 * 160
        assertEquals(1500.0, result.getInvestedValue()); // 10 * 150
        assertEquals(100.0, result.getProfitLoss()); // 1600 - 1500
        assertEquals(6.67, result.getProfitLossPercent(), 0.01); // (100 / 1500) * 100
    }

    @Test
    void testToAssetDetailDTO_FetchLatestPrice_WhenCurrentPriceNull() {
        // Arrange
        testAsset.setCurrentPrice(null);
        when(intradayPriceRepository.findLatestByTicker("AAPL"))
                .thenReturn(Optional.of(testIntradayPrice));

        // Act
        AssetDetailDTO result = priceService.toAssetDetailDTO(testAsset);

        // Assert
        assertNotNull(result);
        assertEquals(155.0, result.getCurrentPrice());
        assertEquals(1550.0, result.getCurrentValue()); // 10 * 155
    }

    @Test
    void testToAssetDetailDTO_UseAvgBuyPrice_WhenNoPriceAvailable() {
        // Arrange
        testAsset.setCurrentPrice(null);
        when(intradayPriceRepository.findLatestByTicker("AAPL"))
                .thenReturn(Optional.empty());
        when(dailyPriceRepository.findLatestByTickerAndDate(anyString(), any()))
                .thenReturn(Optional.empty());

        // Act
        AssetDetailDTO result = priceService.toAssetDetailDTO(testAsset);

        // Assert
        assertNotNull(result);
        assertEquals(150.0, result.getCurrentPrice()); // Falls back to avgBuyPrice
        assertEquals(1500.0, result.getCurrentValue());
        assertEquals(0.0, result.getProfitLoss());
    }
}
