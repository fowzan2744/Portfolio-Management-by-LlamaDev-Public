package com.backend.Portfolio_Backend.service;

import com.backend.Portfolio_Backend.model.Portfolio;
import com.backend.Portfolio_Backend.model.PortfolioAsset;
import com.backend.Portfolio_Backend.repository.PortfolioAssetRepository;
import com.backend.Portfolio_Backend.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioAssetRepository assetRepository;

    @Mock
    private PortfolioAuditService auditService;

    @InjectMocks
    private PortfolioService portfolioService;

    private Portfolio testPortfolio;
    private PortfolioAsset testAsset;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);

        testAsset = new PortfolioAsset();
        testAsset.setId(1L);
        testAsset.setPortfolio(testPortfolio);
        testAsset.setTicker("AAPL");
        testAsset.setQuantity(10);
        testAsset.setAvgBuyPrice(150.0);
        testAsset.setCurrentPrice(155.0);
    }

    // ===================== getOrCreatePortfolio Tests =====================

    @Test
    void testGetOrCreatePortfolio_WhenPortfolioExists() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));

        // Act
        Portfolio result = portfolioService.getOrCreatePortfolio();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(portfolioRepository, times(1)).findAll();
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void testGetOrCreatePortfolio_WhenPortfolioDoesNotExist() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(new ArrayList<>());
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        // Act
        Portfolio result = portfolioService.getOrCreatePortfolio();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(portfolioRepository, times(1)).findAll();
        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    // ===================== getAssets Tests =====================

    @Test
    void testGetAssets_ReturnsList() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolio(testPortfolio)).thenReturn(List.of(testAsset));

        // Act
        List<PortfolioAsset> result = portfolioService.getAssets();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        verify(assetRepository, times(1)).findByPortfolio(testPortfolio);
    }

    @Test
    void testGetAssets_ReturnsEmptyList() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolio(testPortfolio)).thenReturn(new ArrayList<>());

        // Act
        List<PortfolioAsset> result = portfolioService.getAssets();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ===================== getAssetById Tests =====================

    @Test
    void testGetAssetById_WhenAssetExists() {
        // Arrange
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));

        // Act
        PortfolioAsset result = portfolioService.getAssetById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AAPL", result.getTicker());
        verify(assetRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAssetById_WhenAssetDoesNotExist() {
        // Arrange
        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            portfolioService.getAssetById(999L);
        });
        assertEquals("Asset not found with id: 999", exception.getMessage());
        verify(assetRepository, times(1)).findById(999L);
    }

    // ===================== getAssetByTicker Tests =====================

    @Test
    void testGetAssetByTicker_WhenAssetExists() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "AAPL"))
                .thenReturn(Optional.of(testAsset));

        // Act
        PortfolioAsset result = portfolioService.getAssetByTicker("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        assertEquals(10, result.getQuantity());
    }

    @Test
    void testGetAssetByTicker_WhenAssetDoesNotExist() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "GOOGL"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            portfolioService.getAssetByTicker("GOOGL");
        });
        assertEquals("Asset not found with ticker: GOOGL", exception.getMessage());
    }

    // ===================== addAsset Tests =====================

    @Test
    void testAddAsset_NewAsset() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "MSFT"))
                .thenReturn(Optional.empty());

        PortfolioAsset newAsset = new PortfolioAsset();
        when(assetRepository.save(any(PortfolioAsset.class))).thenAnswer(invocation -> {
            PortfolioAsset asset = invocation.getArgument(0);
            asset.setId(2L);
            return asset;
        });

        // Act
        portfolioService.addAsset("MSFT", 5, 300.0);

        // Assert
        ArgumentCaptor<PortfolioAsset> assetCaptor = ArgumentCaptor.forClass(PortfolioAsset.class);
        verify(assetRepository).save(assetCaptor.capture());
        
        PortfolioAsset savedAsset = assetCaptor.getValue();
        assertEquals("MSFT", savedAsset.getTicker());
        assertEquals(5, savedAsset.getQuantity());
        assertEquals(300.0, savedAsset.getAvgBuyPrice());

        verify(auditService).logAction(testPortfolio, "ADD", "MSFT", 5, 300.0);
    }

    @Test
    void testAddAsset_ExistingAsset_UpdatesQuantityAndAvgPrice() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "AAPL"))
                .thenReturn(Optional.of(testAsset));

        when(assetRepository.save(any(PortfolioAsset.class))).thenReturn(testAsset);

        // Act
        portfolioService.addAsset("AAPL", 10, 160.0);

        // Assert
        ArgumentCaptor<PortfolioAsset> assetCaptor = ArgumentCaptor.forClass(PortfolioAsset.class);
        verify(assetRepository).save(assetCaptor.capture());
        
        PortfolioAsset savedAsset = assetCaptor.getValue();
        assertEquals(20, savedAsset.getQuantity()); // 10 + 10
        // avgPrice = (150*10 + 160*10) / (10+10) = 3100/20 = 155.0
        assertEquals(155.0, savedAsset.getAvgBuyPrice());

        verify(auditService).logAction(testPortfolio, "ADD", "AAPL", 10, 160.0);
    }

    @Test
    void testAddAsset_ExistingAssetWithNullValues() {
        // Arrange
        PortfolioAsset assetWithNulls = new PortfolioAsset();
        assetWithNulls.setId(1L);
        assetWithNulls.setPortfolio(testPortfolio);
        assetWithNulls.setTicker("GOOG");
        assetWithNulls.setQuantity(null);
        assetWithNulls.setAvgBuyPrice(null);

        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "GOOG"))
                .thenReturn(Optional.of(assetWithNulls));
        when(assetRepository.save(any(PortfolioAsset.class))).thenReturn(assetWithNulls);

        // Act
        portfolioService.addAsset("GOOG", 5, 2800.0);

        // Assert
        ArgumentCaptor<PortfolioAsset> assetCaptor = ArgumentCaptor.forClass(PortfolioAsset.class);
        verify(assetRepository).save(assetCaptor.capture());
        
        PortfolioAsset savedAsset = assetCaptor.getValue();
        assertEquals(5, savedAsset.getQuantity());
        assertEquals(2800.0, savedAsset.getAvgBuyPrice());
    }

    // ===================== removeAsset Tests =====================

    @Test
    void testRemoveAsset_PartialRemoval() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "AAPL"))
                .thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(PortfolioAsset.class))).thenReturn(testAsset);

        // Act
        portfolioService.removeAsset("AAPL", 5);

        // Assert
        ArgumentCaptor<PortfolioAsset> assetCaptor = ArgumentCaptor.forClass(PortfolioAsset.class);
        verify(assetRepository).save(assetCaptor.capture());
        
        PortfolioAsset savedAsset = assetCaptor.getValue();
        assertEquals(5, savedAsset.getQuantity()); // 10 - 5

        verify(auditService).logAction(testPortfolio, "REMOVE", "AAPL", 5, 150.0);
        verify(assetRepository, never()).delete(any());
    }

    @Test
    void testRemoveAsset_CompleteRemoval() {
        // Arrange
        testAsset.setQuantity(5); // Only 5 shares
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "AAPL"))
                .thenReturn(Optional.of(testAsset));

        // Act
        portfolioService.removeAsset("AAPL", 5);

        // Assert
        verify(assetRepository).delete(testAsset); // Should delete when quantity becomes 0
        verify(auditService).logAction(testPortfolio, "REMOVE", "AAPL", 5, 150.0);
    }

    @Test
    void testRemoveAsset_InsufficientQuantity() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "AAPL"))
                .thenReturn(Optional.of(testAsset)); // testAsset has quantity 10

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            portfolioService.removeAsset("AAPL", 15); // Try to remove 15 when only 10 available
        });
        assertEquals("Insufficient quantity", exception.getMessage());
        verify(assetRepository, never()).save(any());
        verify(assetRepository, never()).delete(any());
    }

    @Test
    void testRemoveAsset_AssetNotFound() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(testPortfolio));
        when(assetRepository.findByPortfolioAndTicker(testPortfolio, "UNKNOWN"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            portfolioService.removeAsset("UNKNOWN", 5);
        });
        assertEquals("Asset not found", exception.getMessage());
        verify(assetRepository, never()).save(any());
        verify(assetRepository, never()).delete(any());
    }
}
