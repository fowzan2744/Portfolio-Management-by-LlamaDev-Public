package com.backend.Portfolio_Backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceApiResponse {

    private String ticker;
    private PriceData price_data;
    private List<String> timestamp;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public PriceData getPrice_data() {
        return price_data;
    }

    public void setPrice_data(PriceData price_data) {
        this.price_data = price_data;
    }

    public List<String> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(List<String> timestamp) {
        this.timestamp = timestamp;
    }
}

