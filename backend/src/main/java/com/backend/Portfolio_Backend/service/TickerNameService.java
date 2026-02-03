package com.backend.Portfolio_Backend.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TickerNameService {
    
    private static final Map<String, String> TICKER_TO_NAME = new HashMap<>();
    
    static {
        // Common ticker mappings - can be expanded
        TICKER_TO_NAME.put("AAPL", "Apple Inc.");
        TICKER_TO_NAME.put("MSFT", "Microsoft Corporation");
        TICKER_TO_NAME.put("GOOGL", "Alphabet Inc.");
        TICKER_TO_NAME.put("AMZN", "Amazon.com Inc.");
        TICKER_TO_NAME.put("NVDA", "NVIDIA Corporation");
        TICKER_TO_NAME.put("TSLA", "Tesla, Inc.");
        TICKER_TO_NAME.put("META", "Meta Platforms, Inc.");
        TICKER_TO_NAME.put("JPM", "JPMorgan Chase & Co.");
        TICKER_TO_NAME.put("V", "Visa Inc.");
        TICKER_TO_NAME.put("JNJ", "Johnson & Johnson");
    }
    
    public String getNameForTicker(String ticker) {
        return TICKER_TO_NAME.getOrDefault(ticker.toUpperCase(), ticker + " Corp.");
    }
}
