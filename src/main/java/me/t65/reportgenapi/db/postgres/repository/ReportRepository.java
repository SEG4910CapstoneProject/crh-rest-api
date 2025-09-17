package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.ReportEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Integer> {

    List<ReportEntity> findByGenerateDateBetweenAndReportType(
            Instant generateDateStart,
            Instant generateDateEnd,
            ReportType reportType,
            Pageable pageable);
    List<ReportEntity> findByGenerateDateLessThanEqual(
            Instant generateDateEnd);
    List<ReportEntity> findByGenerateDateLessThanEqualAndReportId(
            Instant generateDateEnd,
            long reportId);
    List<ReportEntity> findByGenerateDateGreaterThanEqual(
            Instant generateDateStart);
    List<ReportEntity> findByGenerateDateGreaterThanEqualAndReportId(
            Instant generateDateStart,
            long reportId);
    List<ReportEntity> findByGenerateDateBetween(
            Instant generateDateStart,
            Instant generateDateEnd);
    List<ReportEntity> findByGenerateDateBetweenAndReportId(
            Instant generateDateStart,
            Instant generateDateEnd,
            long reportId);
    List<ReportEntity> findByReportId(long reportId);
    

    long countByGenerateDateBetweenAndReportType(
            Instant generateDateStart, Instant generateDateEnd, ReportType reportType);

    long countByReportType(ReportType reportType);

    List<ReportEntity> findByReportType(ReportType reportType/* , Pageable pageable*/);

    ReportEntity findFirstByOrderByGenerateDateDesc();
}
