package com.backend.Portfolio_Backend.controller;

import com.backend.Portfolio_Backend.model.PortfolioAsset;
import com.backend.Portfolio_Backend.repository.DailyPriceRepository;
import com.backend.Portfolio_Backend.repository.IntradayPriceRepository;
import com.backend.Portfolio_Backend.service.PortfolioService;
import com.backend.Portfolio_Backend.service.PriceService;
import com.backend.Portfolio_Backend.service.TickerNameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerIntegrationTest {

    @Mock
    PortfolioService portfolioService;

    @Mock
    PriceService priceService;

    @Mock
    TickerNameService tickerNameService;

    @Mock
    DailyPriceRepository dailyPriceRepository;

    @Mock
    IntradayPriceRepository intradayPriceRepository;

    @InjectMocks
    PortfolioController controller;

    @BeforeEach
    void setup() {
        // nothing special
    }

    @Test
    void getHoldings_whenNoAssets_returnsEmptyList() {
        when(portfolioService.getAssets()).thenReturn(List.of());

        var result = controller.getHoldings();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentPrice_whenNoPrice_returnsNoPriceStatus() {
        when(priceService.getLatestPrice("ABC")).thenReturn(null);

        var resp = controller.getCurrentPrice("ABC");
        assertEquals("ABC", resp.getTicker());
        assertNull(resp.getCurrentPrice());
        assertEquals("No price data available", resp.getStatus());
    }

    @Test
    void getSummary_withSingleAsset_computesTotals() {
        PortfolioAsset asset = new PortfolioAsset();
        asset.setId(1L);
        asset.setTicker("TST");
        asset.setQuantity(10);
        asset.setAvgBuyPrice(5.0);
        asset.setCreatedAt(LocalDateTime.now().minusDays(2));

        when(portfolioService.getAssets()).thenReturn(List.of(asset));
        when(priceService.getLatestPrice("TST")).thenReturn(6.0);
        when(priceService.calculateDailyChange("TST", 6.0)).thenReturn(new Double[]{0.5, 9.09});
        when(tickerNameService.getNameForTicker("TST")).thenReturn("Test Asset");

        var summary = controller.getSummary();
        assertNotNull(summary);
        assertEquals(60.0, summary.getTotalValue(), 0.001);
        assertEquals(50.0, summary.getInvestedValue(), 0.001);
        assertEquals(10.0, summary.getProfitLoss(), 0.001);
    }

    @Test
    void addAsset_postAndReturnHolding() {
        PortfolioAsset asset = new PortfolioAsset();
        asset.setId(2L);
        asset.setTicker("NEW");
        asset.setQuantity(5);
        asset.setAvgBuyPrice(2.0);
        asset.setCreatedAt(LocalDateTime.now());

        when(portfolioService.getAssets()).thenReturn(List.of(asset));
        when(priceService.getLatestPrice("NEW")).thenReturn(3.0);
        when(priceService.calculateDailyChange("NEW", 3.0)).thenReturn(new Double[]{0.0, 0.0});
        when(tickerNameService.getNameForTicker("NEW")).thenReturn("New Asset");

        PortfolioController.AddAssetRequest req = new PortfolioController.AddAssetRequest();
        req.setTicker("NEW");
        req.setQuantity(5);
        req.setAvgBuyPrice(2.0);

        var holding = controller.addAsset(req);
        assertNotNull(holding);
        assertEquals("NEW", holding.getSymbol());
        assertEquals("New Asset", holding.getName());
    }
}
