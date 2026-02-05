package com.backend.Portfolio_Backend.controller;

import com.backend.Portfolio_Backend.dto.*;
import com.backend.Portfolio_Backend.service.GeminiService;
import com.backend.Portfolio_Backend.service.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIInsightsController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private PortfolioController portfolioController;

    @Autowired
    private NewsService newsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyze portfolio and generate AI insights
     * 
     * @param request User preferences for analysis
     * @return AI-generated or fallback insights
     */
    @PostMapping("/analyze")
    public ResponseEntity<AIInsightsResponseDTO> analyzePortfolio(@RequestBody AIInsightsRequestDTO request) {
        try {
            System.out.println("\n=== AI INSIGHTS REQUEST ===");
            System.out.println("Checking Gemini configuration...");
            
            if (!geminiService.isConfigured()) {
                System.out.println("❌ Gemini NOT configured - returning fallback");
                return ResponseEntity.ok(generateFallbackInsights(request));
            }
            
            System.out.println("✓ Gemini IS configured - calling API");

            // Gather portfolio data
            List<HoldingDTO> holdings = portfolioController.getHoldings();
            PortfolioSummaryDTO summary = portfolioController.getSummary();
            List<NewsItemDTO> news = newsService.getPortfolioNews().stream().limit(10).collect(Collectors.toList());

            // Build prompt
            String prompt = buildAnalysisPrompt(holdings, summary, news, request);

            // Call Gemini
            String jsonResponse = geminiService.generateInsights(prompt);

            // Parse response
            AIInsightsResponseDTO insights = objectMapper.readValue(jsonResponse, AIInsightsResponseDTO.class);
            insights.setAiGenerated(true);

            return ResponseEntity.ok(insights);

        } catch (Exception e) {
            System.err.println("AI analysis failed: " + e.getMessage());
            // Return fallback on error
            return ResponseEntity.ok(generateFallbackInsights(request));
        }
    }

    /**
     * Build structured prompt for Gemini AI
     */
    private String buildAnalysisPrompt(List<HoldingDTO> holdings, PortfolioSummaryDTO summary, 
                                       List<NewsItemDTO> news, AIInsightsRequestDTO request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a financial portfolio analyst. Analyze the following portfolio data and provide actionable insights.\n\n");
        
        // User preferences
        prompt.append("USER PROFILE:\n");
        prompt.append("- Risk Profile: ").append(request.getRiskProfile() != null ? request.getRiskProfile() : "Moderate").append("\n");
        prompt.append("- Investment Horizon: ").append(request.getInvestmentHorizon() != null ? request.getInvestmentHorizon() : "Medium").append("\n");
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            prompt.append("- User Notes: ").append(request.getNotes()).append("\n");
        }
        prompt.append("\n");

        // Portfolio summary
        if (summary != null) {
            prompt.append("PORTFOLIO SUMMARY:\n");
            prompt.append("- Total Value: $").append(String.format("%.2f", summary.getTotalValue())).append("\n");
            prompt.append("- Total Invested: $").append(String.format("%.2f", summary.getInvestedValue())).append("\n");
            prompt.append("- Total Gain/Loss: $").append(String.format("%.2f", summary.getProfitLoss()));
            prompt.append(" (").append(String.format("%.2f", summary.getProfitLossPercent())).append("%\n");
            prompt.append("- Daily Change: $").append(String.format("%.2f", summary.getTodayChange()));
            prompt.append(" (").append(String.format("%.2f", summary.getTodayChangePercent())).append("%\n");
            prompt.append("- Number of Holdings: ").append(holdings != null ? holdings.size() : 0).append("\n\n");
        }

        // Holdings details
        if (holdings != null && !holdings.isEmpty()) {
            prompt.append("HOLDINGS:\n");
            for (int i = 0; i < holdings.size(); i++) {
                HoldingDTO h = holdings.get(i);
                prompt.append(String.format("%d. %s (%s): %d shares, $%.2f value, %.2f%% allocation, %.2f%% daily change\n",
                        i + 1, h.getName(), h.getSymbol(), h.getShares(), h.getValue(), 
                        h.getAllocation(), h.getDailyChangePercent()));
            }
            prompt.append("\n");
        }

        // News highlights
        if (news != null && !news.isEmpty()) {
            prompt.append("RECENT NEWS HIGHLIGHTS:\n");
            for (NewsItemDTO item : news) {
                prompt.append("- ").append(item.getHeadline());
                if (!item.getRelatedSymbols().isEmpty()) {
                    prompt.append(" (").append(String.join(", ", item.getRelatedSymbols())).append(")");
                }
                prompt.append(" - ").append(item.getPublishedAt()).append("\n");
            }
            prompt.append("\n");
        }

        // Instructions for response format
        prompt.append("Provide a structured JSON response with the following format:\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"Brief 2-3 sentence portfolio overview\",\n");
        prompt.append("  \"risks\": [\"Risk 1\", \"Risk 2\", \"Risk 3\"],\n");
        prompt.append("  \"opportunities\": [\"Opportunity 1\", \"Opportunity 2\"],\n");
        prompt.append("  \"rebalancingActions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"action\": \"BUY|SELL|HOLD|REDUCE|INCREASE\",\n");
        prompt.append("      \"symbol\": \"TICKER\",\n");
        prompt.append("      \"reason\": \"Brief explanation\",\n");
        prompt.append("      \"suggestedAllocation\": 15.0\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"sentimentAnalysis\": \"Overall market sentiment based on news\",\n");
        prompt.append("  \"nextSteps\": [\"Action 1\", \"Action 2\", \"Action 3\"]\n");
        prompt.append("}\n\n");
        prompt.append("Keep insights concise, actionable, and avoid speculation. Base all recommendations on the data provided.");

        return prompt.toString();
    }

    /**
     * Generate basic calculated insights when AI is unavailable
     */
    private AIInsightsResponseDTO generateFallbackInsights(AIInsightsRequestDTO request) {
        try {
            List<HoldingDTO> holdings = portfolioController.getHoldings();
            PortfolioSummaryDTO summary = portfolioController.getSummary();

            AIInsightsResponseDTO insights = new AIInsightsResponseDTO();
            insights.setAiGenerated(false);

            // Calculate basic insights
            if (holdings != null && summary != null) {
                // Summary
                String summaryText = String.format(
                    "Your portfolio has %d holdings with a total value of $%.2f and a gain of %.2f%%. " +
                    "Daily performance shows a change of %.2f%%.",
                    holdings.size(), summary.getTotalValue(), summary.getProfitLossPercent(), summary.getTodayChangePercent()
                );
                insights.setSummary(summaryText);

                // Calculate concentration risk
                List<String> risks = new ArrayList<>();
                if (!holdings.isEmpty()) {
                    HoldingDTO topHolding = holdings.stream()
                            .max(Comparator.comparing(HoldingDTO::getAllocation))
                            .orElse(null);
                    if (topHolding != null && topHolding.getAllocation() > 25) {
                        risks.add(String.format("High concentration risk: %s represents %.1f%% of portfolio",
                                topHolding.getSymbol(), topHolding.getAllocation()));
                    }
                    if (holdings.size() < 5) {
                        risks.add("Low diversification: Consider adding more holdings to reduce risk");
                    }
                }
                if (summary.getTodayChangePercent() < -3) {
                    risks.add("Significant daily decline detected");
                }
                if (risks.isEmpty()) {
                    risks.add("No immediate risks detected in portfolio composition");
                }
                insights.setRisks(risks);

                // Opportunities
                List<String> opportunities = new ArrayList<>();
                if (summary.getProfitLossPercent() > 10) {
                    opportunities.add("Strong portfolio performance - consider taking partial profits");
                }
                if (holdings.size() < 10) {
                    opportunities.add("Expand diversification across different sectors");
                }
                opportunities.add("Review allocation quarterly to maintain balance");
                insights.setOpportunities(opportunities);

                // Rebalancing suggestions
                List<RebalancingActionDTO> actions = new ArrayList<>();
                for (HoldingDTO h : holdings) {
                    if (h.getAllocation() > 30) {
                        actions.add(new RebalancingActionDTO(
                                "REDUCE",
                                h.getSymbol(),
                                "Over-allocated position",
                                20.0
                        ));
                    }
                }
                if (actions.isEmpty()) {
                    actions.add(new RebalancingActionDTO(
                            "HOLD",
                            "ALL",
                            "Portfolio is reasonably balanced",
                            null
                    ));
                }
                insights.setRebalancingActions(actions);

                // Sentiment
                insights.setSentimentAnalysis("AI-powered sentiment analysis requires GEMINI_API_KEY configuration");

                // Next steps
                List<String> nextSteps = Arrays.asList(
                        "Monitor daily performance and news",
                        "Review allocation balance monthly",
                        "Set up GEMINI_API_KEY for AI-powered insights"
                );
                insights.setNextSteps(nextSteps);
            } else {
                insights.setSummary("Unable to analyze portfolio: No data available");
                insights.setRisks(Arrays.asList("Portfolio data unavailable"));
                insights.setOpportunities(Arrays.asList("Add holdings to begin analysis"));
                insights.setRebalancingActions(new ArrayList<>());
                insights.setSentimentAnalysis("N/A");
                insights.setNextSteps(Arrays.asList("Add holdings to your portfolio"));
            }

            return insights;

        } catch (Exception e) {
            System.err.println("Fallback insights generation failed: " + e.getMessage());
            AIInsightsResponseDTO error = new AIInsightsResponseDTO();
            error.setAiGenerated(false);
            error.setSummary("Analysis temporarily unavailable");
            error.setRisks(Arrays.asList("Unable to generate insights"));
            error.setOpportunities(new ArrayList<>());
            error.setRebalancingActions(new ArrayList<>());
            error.setSentimentAnalysis("N/A");
            error.setNextSteps(Arrays.asList("Try again later"));
            return error;
        }
    }
}
