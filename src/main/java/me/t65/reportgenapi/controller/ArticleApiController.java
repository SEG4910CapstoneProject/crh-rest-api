package me.t65.reportgenapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import me.t65.reportgenapi.controller.payload.ArticleByLinkRequest;
import me.t65.reportgenapi.controller.payload.ArticleIngestRequest;
import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.UidResponse;
import me.t65.reportgenapi.db.postgres.dto.MonthlyArticleDTO;
import me.t65.reportgenapi.db.postgres.entities.MonthlyArticlesEntity;
import me.t65.reportgenapi.db.services.DbArticlesService;
import me.t65.reportgenapi.utils.IdGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Articles")
@RequestMapping(value = "/api/v1/articles")
@RestController
public class ArticleApiController {

    private final DbArticlesService dbArticlesService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleApiController.class);

    private final IdGenerator idGenerator;
    private List<MonthlyArticlesEntity> top10Articles;

    @Autowired
    public ArticleApiController(DbArticlesService dbArticlesService, IdGenerator idGenerator) {
        this.dbArticlesService = dbArticlesService;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "Get article", description = "This endpoint gets the specified article")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved the article",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                JsonArticleReportResponse.class))),
                @ApiResponse(responseCode = "404", description = "Unable to get article"),
            })
    @GetMapping("/{id}")
    public ResponseEntity<?> getArticle(
            @Parameter(description = "The article id", required = true) @PathVariable("id")
                    String id) {

        UUID articleUid = UUID.fromString(id);
        Optional<JsonArticleReportResponse> response = dbArticlesService.getArticleById(articleUid);

        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(response.get());
        }
    }

    @Operation(
            summary = "Get article by its link",
            description = "This endpoint gets the specified article by its link if exists")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved the article",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                JsonArticleReportResponse.class))),
                @ApiResponse(responseCode = "404", description = "Unable to get article"),
            })
    @PostMapping("/link")
    public ResponseEntity<?> getArticleByLink(
            @Parameter(description = "The article id", required = true) @RequestBody
                    ArticleByLinkRequest articleByLinkRequest) {

        Optional<JsonArticleReportResponse> response =
                dbArticlesService.getArticleByLink(articleByLinkRequest.getLink());

        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(response.get());
        }
    }

    @Operation(
            summary = "Ingest a new article from a URL",
            description =
                    "Accepts a URL and optional description; triggers ingestion/classification"
                            + " pipeline.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Article ingestion started successfully"),
                @ApiResponse(responseCode = "400", description = "Missing required link"),
                @ApiResponse(responseCode = "500", description = "Error during ingestion")
            })
    @PostMapping("/ingest")
    public ResponseEntity<?> ingestArticle(@RequestBody ArticleIngestRequest request) {
        LOGGER.info(
                "Ingest request received: link='{}', title='{}'",
                request.getLink(),
                request.getTitle());
        try {
            if (request.getLink() == null || request.getLink().isBlank()) {
                LOGGER.warn("Ingest failed — missing link.");
                return ResponseEntity.badRequest().body(Map.of("message", "Link is required"));
            }
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                LOGGER.warn("Ingest failed — missing title.");
                return ResponseEntity.badRequest().body(Map.of("message", "Title is required"));
            }

            try {
                new URL(request.getLink()); // throws MalformedURLException if invalid
            } catch (MalformedURLException e) {
                LOGGER.warn(" Invalid URL provided: {}", request.getLink());
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Please provide a valid URL."));
            }

            boolean added =
                    dbArticlesService.ingestFromUrl(
                            request.getLink(), request.getTitle(), request.getDescription());

            if (!added) {
                LOGGER.info("Article already exists: {}", request.getLink());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Article already exists"));
            }

            LOGGER.info("Successfully ingested new article: {}", request.getLink());
            return ResponseEntity.ok(Map.of("message", "Article successfully ingested"));

        } catch (Exception e) {
            LOGGER.error(
                    "Error during ingestion for link '{}': {}",
                    request.getLink(),
                    e.getMessage(),
                    e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to ingest article: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> editArticle(
            @Parameter(description = "The article id", required = true) @PathVariable("id")
                    String id,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "link") String link,
            @RequestParam(name = "description") String description,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(name = "publish-date")
                    LocalDate publishDate) {

        Instant publishDateInstant = publishDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        boolean check =
                dbArticlesService.editArticleInReport(
                        id, title, link, description, publishDateInstant);
        if (check) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Adds a new article",
            description = "This endpoint updates the specified article")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Add article successful",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = UidResponse.class)))
            })
    @PostMapping("/add")
    public ResponseEntity<?> addArticle(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "link") String link,
            @RequestParam(name = "description") String description,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(name = "publish-date")
                    LocalDate publishDate) {
        UUID uuid = idGenerator.generateId();
        dbArticlesService.addNewArticle(
                uuid,
                title,
                link,
                description,
                publishDate.atStartOfDay().toInstant(ZoneOffset.UTC));

        return ResponseEntity.ok(new UidResponse(uuid.toString()));
    }

    @Operation(
            summary = "Retrieve articles by type",
            description = "This endpoint fetches articles based on their type.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Articles retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                JsonArticleReportResponse.class))),
                @ApiResponse(
                        responseCode = "204",
                        description = "No articles found for the given type",
                        content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<JsonArticleReportResponse>> getArticlesByType(
            @PathVariable String type) {
        List<JsonArticleReportResponse> articles = dbArticlesService.getArticlesByType(type);
        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(articles);
    }

    @Operation(
            summary = "Retrieve all article types with articles",
            description =
                    "This endpoint returns all article types along with their corresponding"
                            + " articles from the past specified number of days.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Article types and articles retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                JsonArticleReportResponse.class)))
            })
    @GetMapping("/article-types-with-articles")
    public ResponseEntity<Map<String, List<JsonArticleReportResponse>>>
            getAllArticleTypesWithArticles(@RequestParam int days) {
        Map<String, List<JsonArticleReportResponse>> response =
                dbArticlesService.getAllArticleTypesWithArticles(days);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Increment view count of an article",
            description =
                    "This endpoint increments the view count of the article specified by its ID.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "View count incremented successfully",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(
                        responseCode = "404",
                        description = "Article not found",
                        content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/increment-view/{id}")
    public ResponseEntity<?> incrementViewCount(@PathVariable UUID id) {
        Optional<MonthlyArticlesEntity> entity = dbArticlesService.incrementViewCount(id);
        return entity.isPresent() ? ResponseEntity.ok(entity) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Toggle the 'article of note' status",
            description =
                    "This endpoint toggles the 'article of note' status of an article specified by"
                            + " its ID.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Article of note status toggled successfully",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(
                        responseCode = "404",
                        description = "Article not found",
                        content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/toggle-article-of-note/{id}")
    public ResponseEntity<?> toggleArticleOfNote(@PathVariable UUID id) {
        Optional<MonthlyArticlesEntity> entity = dbArticlesService.toggleArticleOfNote(id);
        return entity.isPresent() ? ResponseEntity.ok(entity) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Retrieve top 10 articles based on view count",
            description =
                    "This endpoint fetches the top 10 articles sorted by their view count in"
                            + " descending order.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Top 10 articles retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(implementation = MonthlyArticleDTO.class))),
                @ApiResponse(
                        responseCode = "204",
                        description = "No articles found",
                        content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/top-10")
    public ResponseEntity<List<MonthlyArticleDTO>> getTop10Articles() {
        List<MonthlyArticleDTO> topArticles = dbArticlesService.getTop10Articles();
        return topArticles.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(topArticles);
    }

    @Operation(
            summary = "Retrieve articles marked as 'articles of note'",
            description =
                    "This endpoint fetches all articles that are marked as 'articles of note'.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Articles of note retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(implementation = MonthlyArticleDTO.class))),
                @ApiResponse(
                        responseCode = "204",
                        description = "No articles of note found",
                        content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/articles-of-note")
    public ResponseEntity<List<JsonArticleReportResponse>> getArticlesOfNote() {
        List<JsonArticleReportResponse> articlesOfNote = dbArticlesService.getArticlesOfNote();
        return articlesOfNote.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(articlesOfNote);
    }

    @GetMapping("/my-submissions")
    public ResponseEntity<List<JsonArticleReportResponse>> getManualArticles() {
        List<JsonArticleReportResponse> manualArticles = dbArticlesService.getManualArticles();
        return manualArticles.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(manualArticles);
    }

}
