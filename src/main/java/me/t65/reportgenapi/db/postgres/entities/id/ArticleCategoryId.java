package me.t65.reportgenapi.db.postgres.entities.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@lombok.Setter
@lombok.Builder
@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Embeddable
public class ArticleCategoryId implements Serializable {
    private static final long serialVersionUID = 8097381078233078316L;

    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "article_id")
    private UUID articleId;
}
