package me.t65.reportgenapi.db.postgres.dto;

import java.util.List;
import java.util.UUID;

public class ReportRequest {
    private String analystComments;
    private List<ArticleDetails> articles;
    private List<StatisticDetails> statistics; // <-- Added this
    int reportID;

    // Getters and Setters
    public String getAnalystComments() {
        return analystComments;
    }

    public int getReportID() {
        return reportID;
    }

    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    public void setAnalystComments(String analystComments) {
        this.analystComments = analystComments;
    }

    public List<ArticleDetails> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleDetails> articles) {
        this.articles = articles;
    }

    public List<StatisticDetails> getStatistics() { // <-- Added this
        return statistics;
    }

    public void setStatistics(List<StatisticDetails> statistics) { // <-- Added this
        this.statistics = statistics;
    }

    // Inner class for article details
    public static class ArticleDetails {
        private UUID articleId;
        private String title;
        private String category;
        private String link;
        private String type;

        // Getters and Setters
        public UUID getArticleId() {
            return articleId;
        }

        public void setArticleId(UUID articleId) {
            this.articleId = articleId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    // Inner class for statistics details
    public static class StatisticDetails {
        private String title;
        private String subtitle;

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }
    }
}
