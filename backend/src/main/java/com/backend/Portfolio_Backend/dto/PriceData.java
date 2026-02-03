package com.backend.Portfolio_Backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceData {

    private List<Double> open;
    private List<Double> high;
    private List<Double> low;
    private List<Double> close;
    private List<Double> volume;

    public List<Double> getOpen() { return open; }
    public void setOpen(List<Double> open) { this.open = open; }

    public List<Double> getHigh() { return high; }
    public void setHigh(List<Double> high) { this.high = high; }

    public List<Double> getLow() { return low; }
    public void setLow(List<Double> low) { this.low = low; }

    public List<Double> getClose() { return close; }
    public void setClose(List<Double> close) { this.close = close; }

    public List<Double> getVolume() { return volume; }
    public void setVolume(List<Double> volume) { this.volume = volume; }
}

