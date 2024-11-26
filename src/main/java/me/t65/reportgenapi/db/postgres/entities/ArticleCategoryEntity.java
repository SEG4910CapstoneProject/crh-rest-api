package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import me.t65.reportgenapi.db.postgres.entities.id.ArticleCategoryId;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "article_category")
public class ArticleCategoryEntity {
    @EmbeddedId private ArticleCategoryId articleCategoryId;
}
