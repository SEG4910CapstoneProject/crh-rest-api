package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@lombok.Getter
@lombok.Setter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "articles")
public class ArticlesEntity {
    @Id
    @Column(name = "article_ID", columnDefinition = "uuid")
    private UUID articleId;

    @Column(name = "source_ID")
    private Integer sourceId;

    @Column(name = "date_ingested")
    private Instant dateIngested;

    @Column(name = "date_published")
    private Instant datePublished;

    @Column(name = "is_feature_ext")
    private Boolean isFeatureExt;

    @Column(name = "is_ML_ext")
    private Boolean isMLExt;

    @Column(name = "hashlink")
    private long hashlink;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public ArticlesEntity(UUID articleId, Integer sourceId, Instant dateIngested, Instant datePublished, Boolean isFeatureExt, Boolean isMLExt, long hashlink) {
        this.articleId = articleId;
        this.sourceId = sourceId;
        this.dateIngested = dateIngested;
        this.datePublished = datePublished;
        this.isFeatureExt = isFeatureExt;
        this.isMLExt = isMLExt;
        this.hashlink = hashlink;
        this.viewCount = 0;
    }
}
