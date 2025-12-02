package me.t65.reportgenapi.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import me.t65.reportgenapi.controller.payload.*;
import me.t65.reportgenapi.db.postgres.dto.ReportRequest;
import me.t65.reportgenapi.db.postgres.entities.EmailTemplateType;
import me.t65.reportgenapi.db.postgres.entities.ReportEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportType;
import me.t65.reportgenapi.db.services.DbArticlesService;
import me.t65.reportgenapi.db.services.DbReportService;
import me.t65.reportgenapi.db.services.DbStatsService;
import me.t65.reportgenapi.db.services.EmailService;
import me.t65.reportgenapi.reportformatter.RawReport;
import me.t65.reportgenapi.reportformatter.ReportFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@OpenAPIDefinition(
        info =
                @Info(
                        title = "CyberReportHub API",
                        version = "1.0",
                        description = "The API to interact with CyberReportHub"))
@Tag(name = "Reports")
@RequestMapping(value = "/api/v1/reports")
@RestController
public class ReportApiController {

    private final Logger LOGGER = LoggerFactory.getLogger(ReportApiController.class);
    private final DbReportService dbReportService;
    private final DbStatsService dbStatsService;
    private final DbArticlesService dbArticlesService;
    private final Map<String, ReportFormatter> reportFormatterMap;
    private final EmailService emailService;

    @Autowired
    public ReportApiController(
            DbReportService dbReportService,
            DbStatsService dbStatsService,
            DbArticlesService dbArticlesService,
            @Qualifier("formatMapper") Map<String, ReportFormatter> reportFormatterMap,
            EmailService emailService) {
        this.dbReportService = dbReportService;
        this.dbStatsService = dbStatsService;
        this.dbArticlesService = dbArticlesService;
        this.reportFormatterMap = reportFormatterMap;
        this.emailService = emailService;
    }

    @Operation(
            summary = "Gets the id of the latest report",
            description = "Gets the id of the latest report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "text/plain",
                                        schema = @Schema(implementation = String.class))),
                @ApiResponse(responseCode = "404", description = "No reports in system"),
            })
    @GetMapping("latestId")
    public ResponseEntity<?> getLatestId() {
        int reportId = dbReportService.getLatestReportId();

        if (reportId < 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(reportId);
    }

    @Operation(
            summary = "Search reports by date range and type",
            description = "This endpoint searches for reports based on date range and report type.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved reports",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchReportResponse.class))),
                @ApiResponse(responseCode = "400", description = "Bad request"),
            })
    @GetMapping("/search")
    public ResponseEntity<?> searchReports(
            @Parameter(description = "Start date (YYYY-MM-DD)")
                    @RequestParam(name = "date-start", required = false)
                    LocalDate dateStart,
            @Parameter(description = "End date (YYYY-MM-DD)")
                    @RequestParam(name = "date-end", required = false)
                    LocalDate dateEnd,
            @Parameter(description = "Type of report")
                    @RequestParam(name = "type", required = false, defaultValue = "notSpecified")
                    ReportType type,
            @Parameter(description = "Report number")
                    @RequestParam(name = "reportNo", required = false, defaultValue = "0")
                    Integer reportNo)
                //     @Parameter(
                //                     description =
                //                             "The page of the search. The offset of data is
                // determined by"
                //                                     + " \"limit\"",
                //                     schema =
                //                             @Schema(defaultValue = "0", type = "integer", format
                // = "int32"))
                //             @RequestParam(defaultValue = "0")
                //             int page,
                //     @Parameter(
                //                     description = "The limit of results returned by this api",
                //                     schema =
                //                             @Schema(
                //                                     defaultValue = "10",
                //                                     type = "integer",
                //                                     format = "int32"))
                //             @RequestParam(defaultValue = "10")
                //             int limit)
            {
        LOGGER.info(
                "in searchReports, received: date-start: {}, date-end: {}, reportNo: {}, type: {}",
                dateStart,
                dateEnd,
                reportNo,
                type);
        if (dateStart != null && dateEnd != null && !dateStart.isBefore(dateEnd)) {
            return ResponseEntity.badRequest().body("End date must be after start date.");
        }

        if (type != ReportType.notSpecified
                && type != ReportType.daily
                && type != ReportType.weekly
                && type != ReportType.monthly) {
            return ResponseEntity.badRequest().body("Unsupported report type");
        }

        // if (page < 0) {
        //     return ResponseEntity.badRequest().body("'page' is not a valid number");
        // }
        // if (limit < 0) {
        //     return ResponseEntity.badRequest().body("'limit' is not a valid number");
        // }

        return ResponseEntity.ok()
                .body(
                        dbReportService.searchReports(
                                dateStart, dateEnd, type, reportNo /* , page, limit*/));
    }

    @Operation(
            summary = "Returns report in desired format if id exists",
            description = "This endpoint gets reports in desired format")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully formatted report",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = JsonReportResponse.class)),
                            @Content(mediaType = "text/html")
                        }),
                @ApiResponse(responseCode = "400", description = "Bad request"),
                @ApiResponse(responseCode = "404", description = "Report not found"),
            })
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportByID(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @Parameter(
                            description = "The desired format",
                            required = false,
                            schema =
                                    @Schema(
                                            allowableValues = {"json", "html"},
                                            defaultValue = "json"))
                    @RequestParam(name = "format", defaultValue = "json")
                    String format) {
        Optional<RawReport> rawReport = dbReportService.getRawReport(id);
        if (rawReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ReportFormatter formatter = reportFormatterMap.get(format.toLowerCase());
        if (formatter == null) {
            return ResponseEntity.badRequest().body("invalid format: " + format);
        }

        return formatter.format(rawReport.get());
    }

    @Operation(
            summary = "Returns latest report in desired format if any exists",
            description = "This endpoint gets the latest report in desired format")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully formatted report",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = JsonReportResponse.class)),
                            @Content(mediaType = "text/html")
                        }),
                @ApiResponse(responseCode = "400", description = "Bad request"),
                @ApiResponse(responseCode = "404", description = "Report not found"),
            })
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestReport(
            @Parameter(
                            description = "The desired format",
                            required = false,
                            schema =
                                    @Schema(
                                            allowableValues = {"json", "html"},
                                            defaultValue = "json"))
                    @RequestParam(name = "format", defaultValue = "json")
                    String format) {
        int reportId = dbReportService.getLatestReportId();

        if (reportId < 0) {
            return ResponseEntity.noContent().build();
        }

        return getReportByID(reportId, format);
    }

    @Operation(
            summary = "Add articles to report",
            description = "This endpoint adds the articles specified in request body to the report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Articles added to report successfully"),
                @ApiResponse(responseCode = "404", description = "Unable to find report"),
            })
    @PatchMapping("/{id}/addArticle")
    public ResponseEntity<?> addArticlesToReport(
            @Parameter(description = "The report id", required = true) @PathVariable("id")
                    int reportId,
            @Parameter(description = "The article IDs", required = true) @RequestBody
                    String[] articleIds) {
        LOGGER.info("adding many articles to report number {}", reportId);
        boolean check = dbArticlesService.addArticlesToReport(reportId, articleIds);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Add article to report",
            description = "This endpoint adds the specified article to the report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Articles added to report successfully"),
                @ApiResponse(responseCode = "404", description = "Unable to find report"),
            })
    @PatchMapping("/{id}/addArticle/{articleId}")
    public ResponseEntity<?> addSingleArticleToReport(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @Parameter(description = "The article id", required = true) @PathVariable("articleId")
                    String articleId) {
        String[] ids = new String[] {articleId};
        boolean check = dbArticlesService.addArticlesToReport(id, ids);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Remove articles from report",
            description =
                    "This endpoint removes the articles specified in request body from the report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Articles removed from report successfully"),
                @ApiResponse(responseCode = "404", description = "Unable to find report"),
            })
    @PatchMapping("/{id}/removeArticle")
    public ResponseEntity<?> removeArticlesFromReport(
            @Parameter(description = "The report id", required = true) @PathVariable("id")
                    int reportId,
            @Parameter(description = "The article IDs", required = true) @RequestBody
                    String[] articleIds) {

        boolean check = dbArticlesService.removeArticlesFromReport(reportId, articleIds);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Remove article from report",
            description = "This endpoint removes the specified article from the report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Articles removed from report successfully"),
                @ApiResponse(responseCode = "404", description = "Unable to find report"),
            })
    @PatchMapping("/{id}/removeArticle/{articleId}")
    public ResponseEntity<?> removeSingleArticlesFromReport(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @Parameter(description = "The article id", required = true) @PathVariable("articleId")
                    String articleId) {
        String[] ids = new String[] {articleId};
        boolean check = dbArticlesService.removeArticlesFromReport(id, ids);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Add statistics to report",
            description =
                    "This endpoint adds the statistics specified in request body to the report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Statistics added to report successfully"),
                @ApiResponse(responseCode = "404", description = "Unable to add statistics"),
            })
    @PatchMapping("/{id}/addStat")
    public ResponseEntity<?> addStatsToReport(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @Parameter(description = "The statistic IDs", required = true) @RequestBody
                    String[] statisticIds) {

        boolean check = dbStatsService.addStatsToReport(id, statisticIds);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Add statistic to report",
            description = "This endpoint adds the specified statistic to the report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Statistics added to report successfully"),
                @ApiResponse(responseCode = "404", description = "Unable to add statistics"),
            })
    @PatchMapping("/{id}/addStat/{statisticId}")
    public ResponseEntity<?> addSingleStatToReport(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @Parameter(description = "The statistic id", required = true)
                    @PathVariable("statisticId")
                    String statisticId) {
        String[] ids = new String[] {statisticId};
        boolean check = dbStatsService.addStatsToReport(id, ids);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Remove statistics from report",
            description =
                    "This endpoint removes the statistics specified in request body from the"
                            + " report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Statistics removed from report successfully"),
                @ApiResponse(responseCode = "404", description = "Unable to remove statistics"),
            })
    @PatchMapping("/{id}/removeStat")
    public ResponseEntity<?> removeStatsFromReport(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @Parameter(description = "The statistic IDs", required = true) @RequestBody
                    String[] statisticIds) {

        boolean check = dbStatsService.removeStatsFromReport(id, statisticIds);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Remove statistic from report",
            description = "This endpoint removes the specified statistic from the report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Statistics removed from report successfully"),
                @ApiResponse(responseCode = "404", description = "Unable to remove statistics"),
            })
    @PatchMapping("/{id}/removeStat/{statisticId}")
    public ResponseEntity<?> removeSingleStatFromReport(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @Parameter(description = "The statistic id", required = true)
                    @PathVariable("statisticId")
                    String statisticId) {
        String[] ids = new String[] {statisticId};
        boolean check = dbStatsService.removeStatsFromReport(id, ids);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Gets a list of article and statistic suggestions for a report",
            description = "Gets a list of article suggestions for a given report id")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully gets suggestions from database",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema =
                                            @Schema(
                                                    implementation =
                                                            JsonReportSuggestionsResponse.class))
                        }),
                @ApiResponse(responseCode = "404", description = "Report not found"),
            })
    @GetMapping("/{id}/suggestions")
    public ResponseEntity<?> getReportSuggestions(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id) {
        if (!dbReportService.doesReportExist(id)) {
            return ResponseEntity.notFound().build();
        }

        List<JsonArticleReportResponse> articles = dbArticlesService.getReportSuggestions(id);
        List<JsonStatsResponse> stats = dbStatsService.getReportSuggestions(id);

        return ResponseEntity.ok(new JsonReportSuggestionsResponse(articles, stats));
    }

    @Operation(
            summary = "Adds an article or statistic as a suggestion to a report",
            description = "Adds an article or statistic as a suggestion to a report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Successfully added article to report"),
                @ApiResponse(responseCode = "404", description = "Report not found"),
            })
    @PatchMapping("/{id}/suggestions")
    public ResponseEntity<?> patchReportSuggestions(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @RequestParam(name = "articleId", required = false) String articleId,
            @RequestParam(name = "statId", required = false) String statId) {

        boolean articleResult = true;
        if (articleId != null) {
            UUID articleUid = UUID.fromString(articleId);

            articleResult = dbArticlesService.addReportSuggestion(id, articleUid);
        }

        boolean statResult = true;
        if (statId != null) {
            UUID statUid = UUID.fromString(statId);

            statResult = dbStatsService.addReportSuggestion(id, statUid);
        }

        if (!statResult || !articleResult) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Removes an article or statistic as a suggestion to a report",
            description = "Removes an article or statistic as a suggestion to a report")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Successfully removed article to report"),
                @ApiResponse(responseCode = "404", description = "Report not found"),
            })
    @DeleteMapping("/{id}/suggestions")
    public ResponseEntity<?> deleteReportSuggestions(
            @Parameter(description = "The report id", required = true) @PathVariable("id") int id,
            @RequestParam(name = "articleId", required = false) String articleId,
            @RequestParam(name = "statId", required = false) String statId) {

        boolean articleResult = true;
        if (articleId != null) {
            UUID articleUid = UUID.fromString(articleId);

            articleResult = dbArticlesService.removeReportSuggestion(id, articleUid);
        }

        boolean statResult = true;
        if (statId != null) {
            UUID statUid = UUID.fromString(statId);

            statResult = dbStatsService.removeReportSuggestion(id, statUid);
        }

        if (!statResult || !articleResult) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Creates a new report",
            description = "Generates a new report entry and returns the report ID.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Report created successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request")
            })
    @PostMapping("/create-basic-report")
    public ResponseEntity<?> createBasicReport(
            @RequestParam ReportType reportType,
            @RequestParam EmailTemplateType emailTemplateType) {

        if ((reportType != ReportType.daily)
                && (reportType != ReportType.monthly)
                && (reportType != ReportType.weekly)) {
            return ResponseEntity.badRequest().body("Unsupported report type");
        }

        if ((emailTemplateType != EmailTemplateType.nonRestricted
                && emailTemplateType != EmailTemplateType.restricted)) {
            return ResponseEntity.badRequest().body("Unsupported email template type");
        }
        int reportId =
                dbReportService.createBasicReport(Instant.now(), reportType, emailTemplateType);
        return ResponseEntity.status(201).body(Map.of("reportId", reportId));
    }

    @Operation(summary = "Deletes a report", description = "Deletes a report by ID")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Successfully deleted report"),
                @ApiResponse(responseCode = "404", description = "Report not found")
            })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable("id") int reportId) {
        boolean deleted = dbReportService.deleteReport(reportId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a report by generating a PDF from the provided request and stores it.
     *
     * @param request The JSON payload containing articles, categories, and analyst comments.
     * @return The preview pdf of the report.
     */
    @Operation(
            summary = "Generates and stores a new report",
            description =
                    "Generates a full report PDF based on the provided articles, categories, and"
                            + " analyst comments, and saves it to the database.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully created the report with the generated PDF"),
                @ApiResponse(responseCode = "400", description = "Invalid input data")
            })
    @PostMapping("create-pdf")
    public ResponseEntity<byte[]> createReport(@RequestBody ReportRequest request) {
        // Call to service layer to generate and save the report.
        byte[] pdfBytes = dbReportService.generateAndSaveReport(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=report_" + request.getReportID() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Retrieves a generated report's PDF by its ID.
     *
     * @param id The ID of the report to retrieve the PDF.
     * @return The PDF as a byte array if found, or a 404 if not found.
     */
    @Operation(
            summary = "Retrieves the PDF of a report by its ID",
            description = "Fetches the PDF of the report identified by the provided ID.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved the PDF of the report"),
                @ApiResponse(responseCode = "404", description = "Report not found")
            })
    @GetMapping("get-PDF/{id}")
    public ResponseEntity<byte[]> getReportPdf(@PathVariable Integer id) {
        // Call to service layer to get the report by ID.
        Optional<ReportEntity> report = dbReportService.getReportById(id);

        // If the report is found, return the PDF data.
        if (report.isPresent()) {
            byte[] pdfData = report.get().getPdfData();
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=report_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfData);
        } else {
            // If the report is not found, return a 404 Not Found.
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Generates a preview PDF from the provided request without storing it.
     *
     * @param request The JSON payload containing articles, categories, and analyst comments.
     * @return The generated PDF as a byte array.
     */
    @Operation(
            summary = "Generates a preview of the report PDF",
            description =
                    "Creates a PDF report based on the provided articles, categories, and analyst"
                            + " comments, but does NOT store it in the database.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully generated the preview PDF"),
                @ApiResponse(responseCode = "400", description = "Invalid input data")
            })
    @PostMapping("preview-pdf")
    public ResponseEntity<byte[]> previewReport(@RequestBody ReportRequest request) {
        // Generate the PDF but do NOT store it
        byte[] pdfBytes = dbReportService.generatePdf(request);

        // Return the generated PDF for preview
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=preview_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/{id}/sendReportEmail")
    public ResponseEntity<?> sendReportEmail(
            @Parameter(
                            description = "The report id that we want to send as an email",
                            required = true)
                    @PathVariable
                    int id,
            @Parameter(
                            description = "The email template format to use",
                            required = false,
                            schema =
                                    @Schema(
                                            allowableValues = {"restricted,nonRestricted"},
                                            defaultValue = "nonRestricted"))
                    @RequestParam(defaultValue = "nonRestricted")
                    String emailTemplate,
            @Parameter(description = "the recipient of the email", required = true) @RequestParam
                    String[] recipientList) {
        if (recipientList.length == 0) {
            return ResponseEntity.badRequest().build();
        }
        Optional<RawReport> rawReport = dbReportService.getRawReport(id);

        if (rawReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ReportFormatter formatter = reportFormatterMap.get("html");
        ResponseEntity<?> formattedReport = formatter.format(rawReport.get());
        String reportType = rawReport.get().getReport().getReportType().toString();
        String email_title =
                reportType.substring(0, 1).toUpperCase() + reportType.substring(1) + " Report --- ";
        Instant lastModifDate = rawReport.get().getReport().getLastModified();
        ZonedDateTime zdt = lastModifDate.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("MMMM d,yyyy");
        String formattedDate = zdt.format(formatterDate);
        email_title += formattedDate;

        boolean emailSent =
                emailService.sendReportByEmail(formattedReport, recipientList, email_title);
        if (emailSent) {
            LOGGER.info("the report {} was successfully sent by email", id);
        } else {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("Email sent");
    }
}
