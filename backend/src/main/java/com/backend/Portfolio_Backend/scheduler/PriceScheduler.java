package com.backend.Portfolio_Backend.scheduler;

import com.backend.Portfolio_Backend.repository.PortfolioAssetRepository;
import com.backend.Portfolio_Backend.service.PriceIngestionService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class PriceScheduler {

    private final PortfolioAssetRepository assetRepo;
    private final PriceIngestionService priceService;

    public PriceScheduler(
            PortfolioAssetRepository assetRepo,
            PriceIngestionService priceService
    ) {
        this.assetRepo = assetRepo;
        this.priceService = priceService;
    }

    // ✅ RUN ONCE AFTER APP IS FULLY READY
    // TEMPORARILY DISABLED - causing startup crashes
    // @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        System.out.println("🚀 Initial price fetch on application startup (DISABLED)");
        // refreshPrices();
    }

    // 🔁 RUN EVERY 15 MINUTES
    @Scheduled(cron = "0 */15 * * * *")
    public void refreshPrices() {
        assetRepo.findDistinctTickers()
                .forEach(priceService::fetchAndStore);
    }
}
