package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.SearchReportDetailsResponse;
import me.t65.reportgenapi.controller.payload.SearchReportResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.mongo.repository.ArticleContentRepository;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.generators.JsonReportGenerator;
import me.t65.reportgenapi.reportformatter.RawReport;
import me.t65.reportgenapi.utils.StreamUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbReportServiceImpl implements DbReportService {

    private final StreamUtils streamUtils;

    private final ReportRepository reportRepository;
    private final ArticlesRepository articlesRepository;
    private final ReportArticlesRepository reportArticlesRepository;
    private final ArticleContentRepository articleContentRepository;
    private final IOCArticlesEntityRepository iocArticlesEntityRepository;
    private final ReportStatisticsRepository reportStatisticsRepository;

    private final JsonReportGenerator jsonReportGenerator;
    private final DbEntitiesUtils dbEntitiesUtils;
    private final DbArticlesService dbArticlesService;

    @Autowired
    public DbReportServiceImpl(
            StreamUtils streamUtils,
            ReportRepository reportRepository,
            ArticlesRepository articlesRepository,
            ReportArticlesRepository reportArticlesRepository,
            ArticleContentRepository articleContentRepository,
            IOCArticlesEntityRepository iocArticlesEntityRepository,
            ReportStatisticsRepository reportStatisticsRepository,
            JsonReportGenerator jsonReportGenerator,
            DbEntitiesUtils dbEntitiesUtils,
            DbArticlesService dbArticlesService) {
        this.streamUtils = streamUtils;

        this.reportRepository = reportRepository;
        this.articlesRepository = articlesRepository;
        this.reportArticlesRepository = reportArticlesRepository;
        this.articleContentRepository = articleContentRepository;
        this.iocArticlesEntityRepository = iocArticlesEntityRepository;
        this.reportStatisticsRepository = reportStatisticsRepository;

        this.jsonReportGenerator = jsonReportGenerator;
        this.dbEntitiesUtils = dbEntitiesUtils;
        this.dbArticlesService = dbArticlesService;
    }

    @Override
    public boolean doesReportExist(int reportId) {
        return reportRepository.existsById(reportId);
    }

    @Override
    public SearchReportResponse searchReports(
            LocalDate dateStart, LocalDate dateEnd, ReportType type, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.Direction.DESC, "reportId");

        List<ReportEntity> reports;
        long totalCount;

        if (dateStart == null || dateEnd == null) {
            // If dates are not defined, assume all data
            reports = reportRepository.findByReportType(type, pageable);
            totalCount = reportRepository.countByReportType(type);
        } else {
            // dates are defined, search between dates
            Instant startDateTime = dateStart.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endDateTime = dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC);

            reports =
                    reportRepository.findByGenerateDateBetweenAndReportType(
                            startDateTime, endDateTime, type, pageable);
            totalCount =
                    reportRepository.countByGenerateDateBetweenAndReportType(
                            startDateTime, endDateTime, type);
        }
        return new SearchReportResponse(totalCount, getSearchDetails(reports));
    }

    @Override
    public int getLatestReportId() {
        if (reportRepository.count() == 0) {
            return -1;
        }
        return reportRepository.findFirstByOrderByGenerateDateDesc().getReportId();
    }

    @Override
    public Optional<RawReport> getRawReport(int reportId) {
        Optional<ReportEntity> reportEntity = reportRepository.findById(reportId);
        if (reportEntity.isEmpty()) {
            return Optional.empty();
        }

        List<ReportArticlesEntity> reportArticles =
                reportArticlesRepository.findByReportArticlesId_ReportIdAndSuggestion(
                        reportId, false);

        // Articles + Article Content
        Set<UUID> articleIds =
                dbEntitiesUtils.getArticleIdFromReportArticleEntities(reportArticles);
        Map<UUID, ArticlesEntity> articles =
                streamUtils.getIdObjectMap(
                        articlesRepository.findAllById(articleIds), ArticlesEntity::getArticleId);
        Map<UUID, ArticleContentEntity> articleContents =
                streamUtils.getIdObjectMap(
                        articleContentRepository.findAllById(articleIds),
                        ArticleContentEntity::getId);

        // IOCs
        List<IOCArticlesEntity> iocArticlesEntities =
                iocArticlesEntityRepository.findByIocArticlesId_ArticleIdIn(articleIds);

        // Stats
        List<ReportStatisticsEntity> statReportEntities =
                reportStatisticsRepository.findByReportStatisticsId_ReportIdAndSuggestion(
                        reportEntity.get().getReportId(), false);
        Map<Integer, List<UUID>> articleToStatListMapping =
                dbEntitiesUtils.getReportToStatListMapping(statReportEntities);
        Map<UUID, StatisticEntity> statIdToStatEntityMap =
                dbEntitiesUtils.getStatIdToStatEntityMap(statReportEntities);

        // Create Raw report objects from maps
        return generateRawReport(
                reportEntity.get(),
                articleIds,
                reportArticles,
                articles,
                articleContents,
                iocArticlesEntities,
                articleToStatListMapping,
                statIdToStatEntityMap);
    }

    // Given maps from querying, generate the report object
    private Optional<RawReport> generateRawReport(
            ReportEntity reportEntity,
            Set<UUID> articleIds,
            List<ReportArticlesEntity> reportArticles,
            Map<UUID, ArticlesEntity> articlesEntityMapping,
            Map<UUID, ArticleContentEntity> articlesContentMapping,
            List<IOCArticlesEntity> iocArticlesEntities,
            Map<Integer, List<UUID>> reportToStatListMapping,
            Map<UUID, StatisticEntity> statIdToStatEntityMap) {
        if (reportEntity == null) {
            return Optional.empty();
        }

        int reportId = reportEntity.getReportId();

        // Articles
        Map<UUID, ArticlesEntity> articles =
                streamUtils.filterEntriesFromMap(
                        articlesEntityMapping, entry -> articleIds.contains(entry.getKey()));
        Map<UUID, ArticleContentEntity> articleContent =
                streamUtils.filterEntriesFromMap(
                        articlesContentMapping, entry -> articleIds.contains(entry.getKey()));

        // IOCs
        Map<UUID, List<IOCEntity>> articleIdToIOCEntityMapping =
                dbEntitiesUtils.getArticleIdToIocEntityListMapping(articleIds, iocArticlesEntities);

        // Stats
        Map<Integer, List<StatisticEntity>> reportIdToStatEntityMapping =
                reportToStatListMapping.entrySet().stream()
                        // Only take iocs for this report
                        .filter(entry -> entry.getKey() == reportId)
                        // Convert id to entities
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry ->
                                                entry.getValue().stream()
                                                        .map(statIdToStatEntityMap::get)
                                                        .toList()));

        Map<UUID, CategoryEntity> articleIdToCategoryEntity =
                dbArticlesService.getArticleToCategoryEntityMap(articleIds);

        return Optional.of(
                new RawReport(
                        reportEntity,
                        reportArticles,
                        articles,
                        articleContent,
                        articleIdToIOCEntityMapping,
                        dbEntitiesUtils.getIocTypeIdToNameMap(),
                        reportIdToStatEntityMapping,
                        articleIdToCategoryEntity));
    }

    private List<SearchReportDetailsResponse> getSearchDetails(List<ReportEntity> reports) {
        List<Integer> reportIds = reports.stream().map(ReportEntity::getReportId).toList();

        // ReportArticle Entities (the union between the 2 tables)
        List<ReportArticlesEntity> reportArticles =
                reportArticlesRepository.findByReportArticlesId_ReportIdInAndSuggestion(
                        reportIds, false);

        // Articles + Article Content
        Set<UUID> articleIds =
                dbEntitiesUtils.getArticleIdFromReportArticleEntities(reportArticles);
        Map<UUID, ArticleContentEntity> articleContents =
                streamUtils.getIdObjectMap(
                        articleContentRepository.findAllById(articleIds),
                        ArticleContentEntity::getId);

        // IOCs
        List<IOCArticlesEntity> iocArticlesEntities =
                iocArticlesEntityRepository.findByIocArticlesId_ArticleIdIn(articleIds);
        Map<UUID, List<IOCEntity>> articleIdToIocEntity =
                dbEntitiesUtils.getArticleIdToIocEntityListMapping(articleIds, iocArticlesEntities);

        return reports.stream()
                .map(
                        report ->
                                generateSearchDetails(
                                        report,
                                        dbEntitiesUtils.getArticleIdFromReportArticleEntities(
                                                reportArticles, report.getReportId()),
                                        articleContents,
                                        articleIdToIocEntity))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<SearchReportDetailsResponse> generateSearchDetails(
            ReportEntity reportEntity,
            Set<UUID> articleIds,
            Map<UUID, ArticleContentEntity> articlesContentMapping,
            Map<UUID, List<IOCEntity>> articleIdToIocEntity) {
        if (reportEntity == null) {
            return Optional.empty();
        }

        // Articles
        List<ArticleContentEntity> articleContents =
                articlesContentMapping.entrySet().stream()
                        .filter(entry -> articleIds.contains(entry.getKey()))
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .toList();

        // IOCs
        List<IOCEntity> iocEntities =
                articleIdToIocEntity.entrySet().stream()
                        // Only take iocs for this report
                        .filter(entry -> articleIds.contains(entry.getKey()))
                        // get only the values (List of IOCs in articles)
                        .map(Map.Entry::getValue)
                        // Expand the lists
                        .flatMap(Collection::stream)
                        // Count appearances
                        .collect(Collectors.groupingBy(entity -> entity, Collectors.counting()))
                        .entrySet()
                        .stream()
                        // Sort appearances
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        // Get IOCEntity
                        .map(Map.Entry::getKey)
                        .toList();

        return Optional.of(
                jsonReportGenerator.generateShortDetails(
                        reportEntity,
                        articleContents,
                        iocEntities,
                        dbEntitiesUtils.getIocTypeIdToNameMap()));
    }

    /**
     * Creates a new report entry in the database.
     *
     * @param generateDate The timestamp when the report is created.
     * @param reportType   The type of report (e.g., DAILY, WEEKLY).
     * @return The generated report ID.
     */
    public int createBasicReport(Instant generateDate, ReportType reportType) {
        ReportEntity report = new ReportEntity();
        report.setGenerateDate(generateDate);
        report.setLastModified(generateDate);
        report.setReportType(reportType);
        report.setEmailStatus(false); // Default status

        ReportEntity savedReport = reportRepository.save(report);
        return savedReport.getReportId();
    }

    /**
     * Deletes a report in the database.
     *
     * @param reportId   The ID of report.
     * @return If the function was successful or not
     */
    public boolean deleteReport(int reportId) {
        if (!reportRepository.existsById(reportId)) {
            return false;
        }

        reportRepository.deleteById(reportId);
        return true;
    }
}
