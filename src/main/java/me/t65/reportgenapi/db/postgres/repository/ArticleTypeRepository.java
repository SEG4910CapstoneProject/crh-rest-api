package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.id.ArticleTypeEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleTypeRepository extends JpaRepository<ArticleTypeEntity, UUID> {
    List<ArticleTypeEntity> findByArticleType(String articleType);
}
