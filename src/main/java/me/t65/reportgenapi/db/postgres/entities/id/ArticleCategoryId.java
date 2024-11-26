package me.t65.reportgenapi.db.postgres.entities.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@lombok.Setter
@lombok.Builder
@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Embeddable
public class ArticleCategoryId {
    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "article_id")
    private UUID articleId;
}
