package com.backend.Portfolio_Backend.controller;

import com.backend.Portfolio_Backend.service.PortfolioIntegrityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioIntegrityControllerIntegrationTest {

    @Mock
    PortfolioIntegrityService integrityService;

    @InjectMocks
    PortfolioIntegrityController controller;

    @Test
    void verifyIntegrity_whenServiceReturnsTrue_statusOK() {
        when(integrityService.verifyIntegrity()).thenReturn(true);

        Map<String, Object> resp = controller.verifyIntegrity();
        assertEquals("OK", resp.get("status"));
        assertNotNull(resp.get("verifiedAt"));
    }

    @Test
    void verifyIntegrity_whenServiceReturnsFalse_statusFailed() {
        when(integrityService.verifyIntegrity()).thenReturn(false);

        Map<String, Object> resp = controller.verifyIntegrity();
        assertEquals("FAILED", resp.get("status"));
        assertNotNull(resp.get("verifiedAt"));
    }
}

