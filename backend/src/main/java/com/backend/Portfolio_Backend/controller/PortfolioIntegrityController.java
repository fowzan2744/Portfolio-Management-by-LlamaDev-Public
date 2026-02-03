package com.backend.Portfolio_Backend.controller;

import com.backend.Portfolio_Backend.service.PortfolioIntegrityService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/integrity")
@CrossOrigin
public class PortfolioIntegrityController {

    private final PortfolioIntegrityService integrityService;

    public PortfolioIntegrityController(PortfolioIntegrityService integrityService) {
        this.integrityService = integrityService;
    }

    @GetMapping
    public Map<String, Object> verifyIntegrity() {
        boolean isValid = integrityService.verifyIntegrity();

        return Map.of(
                "status", isValid ? "OK" : "FAILED",
                "verifiedAt", LocalDateTime.now()
        );
    }
}
