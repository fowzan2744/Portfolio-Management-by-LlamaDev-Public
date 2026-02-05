package com.backend.Portfolio_Backend.controller;

import com.backend.Portfolio_Backend.dto.NewsItemDTO;
import com.backend.Portfolio_Backend.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * Get portfolio-specific news
     * Returns news for all holdings in the portfolio
     * 
     * @return List of news items related to portfolio holdings
     */
    @GetMapping("/portfolio")
    public ResponseEntity<List<NewsItemDTO>> getPortfolioNews() {
        try {
            List<NewsItemDTO> news = newsService.getPortfolioNews();
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            System.err.println("Error fetching portfolio news: " + e.getMessage());
            // Return empty list on error to prevent breaking frontend
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get general market news
     * Returns latest market news across all categories
     * 
     * @return List of general market news items
     */
    @GetMapping("/market")
    public ResponseEntity<List<NewsItemDTO>> getMarketNews() {
        try {
            List<NewsItemDTO> news = newsService.getMarketNews();
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            System.err.println("Error fetching market news: " + e.getMessage());
            // Return empty list on error to prevent breaking frontend
            return ResponseEntity.ok(List.of());
        }
    }
}
