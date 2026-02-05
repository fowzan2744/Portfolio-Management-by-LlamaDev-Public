package com.backend.Portfolio_Backend.dto;

import java.util.List;

/**
 * DTO for frontend news display
 * Matches frontend NewsItem interface
 */
public class NewsItemDTO {
    private String id;
    private String headline;
    private String source;
    private String publishedAt; // Relative time string (e.g., "2 hours ago")
    private String summary;
    private String url;
    private List<String> relatedSymbols;
    private String thumbnail;

    // Constructors
    public NewsItemDTO() {}

    public NewsItemDTO(String id, String headline, String source, String publishedAt, 
                       String summary, String url, List<String> relatedSymbols, String thumbnail) {
        this.id = id;
        this.headline = headline;
        this.source = source;
        this.publishedAt = publishedAt;
        this.summary = summary;
        this.url = url;
        this.relatedSymbols = relatedSymbols;
        this.thumbnail = thumbnail;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getRelatedSymbols() {
        return relatedSymbols;
    }

    public void setRelatedSymbols(List<String> relatedSymbols) {
        this.relatedSymbols = relatedSymbols;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
