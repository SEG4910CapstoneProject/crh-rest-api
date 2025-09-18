package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.MonthlyArticlesEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyArticlesRepository extends JpaRepository<MonthlyArticlesEntity, Long> {

    Optional<MonthlyArticlesEntity> findByArticleId(UUID articleId);

    List<MonthlyArticlesEntity> findTop10ByOrderByViewCountDesc();

    List<MonthlyArticlesEntity> findByIsArticleOfNoteTrue();
}
