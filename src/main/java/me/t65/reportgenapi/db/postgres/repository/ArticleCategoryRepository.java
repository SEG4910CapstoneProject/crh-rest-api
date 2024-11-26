package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.ArticleCategoryEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ArticleCategoryId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleCategoryRepository
        extends JpaRepository<ArticleCategoryEntity, ArticleCategoryId> {
    List<ArticleCategoryEntity> findByArticleCategoryId_ArticleIdIn(Collection<UUID> articleIds);
}
