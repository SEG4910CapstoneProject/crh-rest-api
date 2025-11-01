package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.ArticlesEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ArticlesRepository extends JpaRepository<ArticlesEntity, UUID> {
    List<ArticlesEntity> findByHashlink(long hashlink);

    List<ArticlesEntity> findBySourceId(Integer sourceId);

    @Query("SELECT a.articleId from ArticlesEntity a where a.datePublished >= :start_date")
    List<UUID> findAllArticleIdAfterDate(@Param("start_date") Instant start_date);
}
