package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import me.t65.reportgenapi.db.postgres.entities.id.ArticleCategoryId;

import java.util.UUID;

@lombok.Getter
@lombok.Setter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "article_category")
public class ArticleCategoryEntity {
    @EmbeddedId private ArticleCategoryId articleCategoryId;

    // Manually add a constructor for JPA
    public ArticleCategoryEntity(Integer articleId, UUID categoryId) {
        this.articleCategoryId = new ArticleCategoryId(articleId, categoryId);
    }
}
