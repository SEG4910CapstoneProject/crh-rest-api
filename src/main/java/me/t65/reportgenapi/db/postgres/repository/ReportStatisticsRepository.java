package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.ReportStatisticsEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ReportStatisticsId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportStatisticsRepository
        extends JpaRepository<ReportStatisticsEntity, ReportStatisticsId> {
    boolean existsByReportStatisticsId_ReportIdAndReportStatisticsId_StatisticIdAndSuggestion(
            Integer reportId, UUID statisticId, boolean suggestion);

    List<ReportStatisticsEntity> findByReportStatisticsId_ReportIdAndSuggestion(
            Integer reportId, boolean suggestion);
}
