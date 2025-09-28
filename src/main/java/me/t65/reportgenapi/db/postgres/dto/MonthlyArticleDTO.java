package me.t65.reportgenapi.db.postgres.dto;

import java.util.Optional;
import java.util.UUID;

public class MonthlyArticleDTO {
    private String url;
    private Optional<Integer> viewCount; // Make viewCount optional
    private String title;
    private UUID articleId;

    public MonthlyArticleDTO(
            String url, Optional<Integer> viewCount, String title, UUID article_id) {
        this.url = url;
        this.viewCount = viewCount;
        this.title = title;
        this.articleId = article_id;
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

    public UUID getArticleId() {
        return articleId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }
}
