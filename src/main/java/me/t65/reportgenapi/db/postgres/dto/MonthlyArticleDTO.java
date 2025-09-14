package me.t65.reportgenapi.db.postgres.dto;

import java.util.Optional;

public class MonthlyArticleDTO {
    private String url;
    private Optional<Integer> viewCount; // Make viewCount optional
    private String title;

    public MonthlyArticleDTO(String url, Optional<Integer> viewCount, String title) {
        this.url = url;
        this.viewCount = viewCount;
        this.title = title;
    }

    // Getters and setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Optional<Integer> getViewCount() {
        return viewCount;
    }

    public void setViewCount(Optional<Integer> viewCount) {
        this.viewCount = viewCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
