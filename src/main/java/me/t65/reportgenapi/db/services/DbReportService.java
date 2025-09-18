package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.SearchReportResponse;
import me.t65.reportgenapi.db.postgres.dto.ReportRequest;
import me.t65.reportgenapi.db.postgres.entities.ReportEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportType;
import me.t65.reportgenapi.reportformatter.RawReport;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public interface DbReportService {

    boolean doesReportExist(int reportId);

    SearchReportResponse searchReports(
            LocalDate dateStart, LocalDate dateEnd, ReportType type, Integer reportNo/* , int page, int limit*/);

    int getLatestReportId();

    Optional<RawReport> getRawReport(int reportId);

    public int createBasicReport(Instant generateDate, ReportType reportType);

    boolean deleteReport(int reportId);

    byte[] generateAndSaveReport(ReportRequest request);

    byte[] generatePdf(ReportRequest request);

    Optional<ReportEntity> getReportById(Integer reportId);
}
