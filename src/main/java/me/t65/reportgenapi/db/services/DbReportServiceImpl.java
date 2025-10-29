package me.t65.reportgenapi.db.services;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;

import me.t65.reportgenapi.controller.payload.SearchReportDetailsResponse;
import me.t65.reportgenapi.controller.payload.SearchReportResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.mongo.repository.ArticleContentRepository;
import me.t65.reportgenapi.db.postgres.dto.ReportRequest;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.generators.JsonReportGenerator;
import me.t65.reportgenapi.reportformatter.RawReport;
import me.t65.reportgenapi.utils.StreamUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
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
    private final Logger LOGGER = LoggerFactory.getLogger(DbReportServiceImpl.class);

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
            LocalDate dateStart,
            LocalDate dateEnd,
            ReportType type,
            Integer reportNo /* , int page, int limit*/) {
        // Tech debt: Specification might need to be used here, to manage growing number of filters

        // Pageable pageable = PageRequest.of(page, limit, Sort.Direction.DESC, "reportId");
        // Pageable pageable = PageRequest.of(page, limit, Sort.Direction.DESC, "reportId");

        List<ReportEntity> reports;
        long totalCount = 0;
        LOGGER.info("Comparing dates and the report number");
        if (dateStart == null && dateEnd == null) {
            // all data should be returned
            reports =
                    (reportNo == 0
                            ? reportRepository.findAll()
                            : reportRepository.findByReportId(
                                    reportNo)); // TODO: CAP331, next only get reports respecting
            // the limit asked.
        } else if (dateStart == null && dateEnd != null) {
            Instant endDateTime = dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC);
            reports =
                    (reportNo == 0
                            ? reportRepository.findByGenerateDateLessThanEqual(endDateTime)
                            : reportRepository.findByGenerateDateLessThanEqualAndReportId(
                                    endDateTime, reportNo));
        } else if (dateStart != null && dateEnd == null) {
            Instant startDateTime = dateStart.atStartOfDay().toInstant(ZoneOffset.UTC);
            reports =
                    (reportNo == 0
                            ? reportRepository.findByGenerateDateGreaterThanEqual(startDateTime)
                            : reportRepository.findByGenerateDateGreaterThanEqualAndReportId(
                                    startDateTime, reportNo));
        } else {
            Instant startDateTime = dateStart.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endDateTime = dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC);
            reports =
                    (reportNo == 0
                            ? reportRepository.findByGenerateDateBetween(startDateTime, endDateTime)
                            : reportRepository.findByGenerateDateBetweenAndReportId(
                                    startDateTime, endDateTime, reportNo));
        }

        if (type != ReportType.notSpecified) {
            reports =
                    reports.stream().filter(report -> report.getReportType().equals(type)).toList();
        }

        totalCount = reports.size();
        return new SearchReportResponse(totalCount, getSearchDetails(reports.reversed()));
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
     * @param reportType The type of report (e.g., DAILY, WEEKLY, MONTHLY).
     * @param template_type The template email type of the report (ai_generated template type
     *     -non-restricted- or detailed template type -restricted-)
     * @return The generated report ID.
     */
    public int createBasicReport(
            Instant generateDate, ReportType reportType, EmailTemplateType template_type) {
        ReportEntity report = new ReportEntity();
        report.setGenerateDate(generateDate);
        report.setLastModified(generateDate);
        report.setReportType(reportType);
        report.setEmailStatus(false); // Default status
        report.setPdfData(null); // Empty array instead of null
        report.setEmailType(template_type);

        ReportEntity savedReport = reportRepository.save(report);
        return savedReport.getReportId();
    }

    /**
     * Deletes a report in the database.
     *
     * @param reportId The ID of report.
     * @return If the function was successful or not
     */
    public boolean deleteReport(int reportId) {
        if (!reportRepository.existsById(reportId)) {
            return false;
        }

        reportRepository.deleteById(reportId);
        return true;
    }

    /**
     * Generates a PDF report and saves it to the database.
     *
     * @param request The report request containing report details.
     * @return The ID of the saved report.
     * @throws RuntimeException if the report does not exist.
     */
    public byte[] generateAndSaveReport(ReportRequest request) {

        // Ensure the report exists
        Optional<ReportEntity> optionalReport = reportRepository.findById(request.getReportID());
        if (optionalReport.isEmpty()) {
            throw new RuntimeException("Report not found with ID: " + request.getReportID());
        }
        byte[] pdfBytes = generatePdf(request);

        ReportEntity report = optionalReport.get();
        report.setPdfData(pdfBytes);
        ReportEntity savedReport = reportRepository.save(report);

        return savedReport.getPdfData();
    }

    /**
     * Generates a PDF report from the given request data.
     *
     * @param request The report request containing articles and metadata.
     * @return A byte array representing the generated PDF.
     */
    public byte[] generatePdf(ReportRequest request) {
        List<ReportRequest.ArticleDetails> articles = request.getArticles();
        DeviceRgb color = new DeviceRgb(0, 102, 204);
        Map<String, List<ReportRequest.ArticleDetails>> articlesByCategory =
                articles.stream()
                        .collect(Collectors.groupingBy(ReportRequest.ArticleDetails::getCategory));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream));
        Document document = new Document(pdfDoc);

        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        // Create a date block with a blue background
        Paragraph dateBlock =
                new Paragraph("Daily Report for: " + formattedDate)
                        .setBold()
                        .setFontSize(14)
                        .setFontColor(new DeviceRgb(255, 255, 255)) // White text
                        .setBackgroundColor(color) // Blue background
                        .setPadding(5) // Add some padding
                        .setBorder(Border.NO_BORDER) // Remove border
                        .setMarginBottom(10); // Add some spacing below
        document.add(dateBlock);
        document.add(new Paragraph("")); // Spacing

        // Analyst comments
        document.add(
                new Paragraph("Analyst Comments").setBold().setFontSize(14).setFontColor(color));
        document.add(new Paragraph(request.getAnalystComments()).setItalic());
        document.add(new Paragraph("")); // Space

        // Blue separator line
        SolidLine blueLine = new SolidLine();
        blueLine.setLineWidth(1.5f);
        blueLine.setColor(color);
        document.add(new LineSeparator(blueLine));

        // Loop through categories
        for (Map.Entry<String, List<ReportRequest.ArticleDetails>> entry :
                articlesByCategory.entrySet()) {
            document.add(
                    new Paragraph(entry.getKey()).setBold().setFontSize(14).setFontColor(color));

            for (ReportRequest.ArticleDetails article : entry.getValue()) {
                document.add(new Paragraph("Title: " + article.getTitle()));
                document.add(new Paragraph("Link: " + article.getLink()));
                document.add(new Paragraph("Type: " + article.getType()));
                document.add(new Paragraph("")); // Spacing
            }

            document.add(new LineSeparator(blueLine));
        }

        // Statistics Section
        List<ReportRequest.StatisticDetails> statistics = request.getStatistics();
        if (statistics != null && !statistics.isEmpty()) {
            document.add(new Paragraph("Statistics").setBold().setFontSize(14).setFontColor(color));

            for (ReportRequest.StatisticDetails stat : statistics) {
                document.add(new Paragraph("Title: " + stat.getTitle()).setBold());
                document.add(new Paragraph("Subtitle: " + stat.getSubtitle()));
                document.add(new Paragraph("")); // Space between statistics
            }

            document.add(new LineSeparator(blueLine));
        }

        document.close();
        return outputStream.toByteArray();
    }

    /**
     * Retrieves a report by its ID.
     *
     * @param reportId The ID of the report.
     * @return An Optional containing the report entity if found.
     */
    public Optional<ReportEntity> getReportById(Integer reportId) {
        return reportRepository.findById(reportId);
    }
}
