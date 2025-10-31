package me.t65.reportgenapi.db.postgres.entities.id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "article_type")
public class ArticleTypeEntity {
    @Id
    @Column(name = "article_id")
    private UUID articleId;

    @Column(name = "article_type")
    private String articleType;

    public ArticleTypeEntity() {}

    public ArticleTypeEntity(String articleType, UUID articleId) {
        this.articleType = articleType;
        this.articleId = articleId;
    }

    // Getters and setters
    public UUID getArticleId() {
        return articleId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public String getArticleType() {
        return articleType;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }
}
