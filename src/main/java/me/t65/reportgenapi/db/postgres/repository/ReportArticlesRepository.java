package me.t65.reportgenapi.db.postgres.repository;

import jakarta.transaction.Transactional;

import me.t65.reportgenapi.db.postgres.entities.ReportArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportArticlesRepository
        extends JpaRepository<ReportArticlesEntity, ReportArticlesId> {
    int countByReportArticlesId_ReportIdAndSuggestion(Integer reportId, boolean suggestion);

    boolean existsByReportArticlesId_ReportIdAndReportArticlesId_ArticleIdAndSuggestion(
            Integer reportId, UUID articleId, boolean suggestion);

    List<ReportArticlesEntity> findByReportArticlesId_ReportIdAndSuggestion(
            Integer reportId, boolean suggestion);

    List<ReportArticlesEntity> findByReportArticlesId_ReportIdAndSuggestionOrderByArticleRankAsc(
            Integer reportId, boolean suggestion);

    List<ReportArticlesEntity> findByReportArticlesId_ReportIdInAndSuggestion(
            Collection<Integer> reportIds, boolean suggestion);

    boolean existsByReportArticlesIdReportId(int id);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReportArticlesEntity ra WHERE ra.id.reportId = :reportId")
    void deleteAllByReportId(@Param("reportId") Integer reportId);
}
