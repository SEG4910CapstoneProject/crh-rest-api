package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.SearchReportResponse;
import me.t65.reportgenapi.db.postgres.entities.ReportType;
import me.t65.reportgenapi.reportformatter.RawReport;

import java.time.LocalDate;
import java.util.Optional;

public interface DbReportService {

    boolean doesReportExist(int reportId);

    SearchReportResponse searchReports(
            LocalDate dateStart, LocalDate dateEnd, ReportType type, int page, int limit);

    int getLatestReportId();

    Optional<RawReport> getRawReport(int reportId);
}
