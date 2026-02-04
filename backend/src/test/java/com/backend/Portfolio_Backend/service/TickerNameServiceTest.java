package com.backend.Portfolio_Backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TickerNameServiceTest {

    @InjectMocks
    private TickerNameService tickerNameService;

    // ===================== getNameForTicker Tests =====================

    @Test
    void testGetNameForTicker_AAPL() {
        // Act
        String result = tickerNameService.getNameForTicker("AAPL");

        // Assert
        assertEquals("Apple Inc.", result);
    }

    @Test
    void testGetNameForTicker_MSFT() {
        // Act
        String result = tickerNameService.getNameForTicker("MSFT");

        // Assert
        assertEquals("Microsoft Corporation", result);
    }

    @Test
    void testGetNameForTicker_GOOGL() {
        // Act
        String result = tickerNameService.getNameForTicker("GOOGL");

        // Assert
        assertEquals("Alphabet Inc.", result);
    }

    @Test
    void testGetNameForTicker_AMZN() {
        // Act
        String result = tickerNameService.getNameForTicker("AMZN");

        // Assert
        assertEquals("Amazon.com Inc.", result);
    }

    @Test
    void testGetNameForTicker_NVDA() {
        // Act
        String result = tickerNameService.getNameForTicker("NVDA");

        // Assert
        assertEquals("NVIDIA Corporation", result);
    }

    @Test
    void testGetNameForTicker_TSLA() {
        // Act
        String result = tickerNameService.getNameForTicker("TSLA");

        // Assert
        assertEquals("Tesla, Inc.", result);
    }

    @Test
    void testGetNameForTicker_META() {
        // Act
        String result = tickerNameService.getNameForTicker("META");

        // Assert
        assertEquals("Meta Platforms, Inc.", result);
    }

    @Test
    void testGetNameForTicker_JPM() {
        // Act
        String result = tickerNameService.getNameForTicker("JPM");

        // Assert
        assertEquals("JPMorgan Chase & Co.", result);
    }

    @Test
    void testGetNameForTicker_V() {
        // Act
        String result = tickerNameService.getNameForTicker("V");

        // Assert
        assertEquals("Visa Inc.", result);
    }

    @Test
    void testGetNameForTicker_JNJ() {
        // Act
        String result = tickerNameService.getNameForTicker("JNJ");

        // Assert
        assertEquals("Johnson & Johnson", result);
    }

    @Test
    void testGetNameForTicker_UnknownTicker() {
        // Act
        String result = tickerNameService.getNameForTicker("UNKNOWN");

        // Assert
        assertEquals("UNKNOWN Corp.", result);
    }

    @Test
    void testGetNameForTicker_CaseInsensitive_LowerCase() {
        // Act
        String result = tickerNameService.getNameForTicker("aapl");

        // Assert
        assertEquals("Apple Inc.", result);
    }

    @Test
    void testGetNameForTicker_CaseInsensitive_MixedCase() {
        // Act
        String result = tickerNameService.getNameForTicker("AaPl");

        // Assert
        assertEquals("Apple Inc.", result);
    }

    @Test
    void testGetNameForTicker_EmptyString() {
        // Act
        String result = tickerNameService.getNameForTicker("");

        // Assert
        assertEquals(" Corp.", result);
    }

    @Test
    void testGetNameForTicker_SpacesInTicker() {
        // Act
        String result = tickerNameService.getNameForTicker("  ");

        // Assert
        assertTrue(result.contains("Corp."));
    }
}
