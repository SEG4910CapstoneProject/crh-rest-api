package me.t65.reportgenapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import me.t65.reportgenapi.controller.payload.ArticleByLinkRequest;
import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.UidResponse;
import me.t65.reportgenapi.db.postgres.entities.ArticlesEntity;
import me.t65.reportgenapi.db.services.DbArticlesService;
import me.t65.reportgenapi.utils.IdGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final IdGenerator idGenerator;

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
            summary = "Update article",
            description = "This endpoint updates the specified article")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Article edit successful"),
                    @ApiResponse(responseCode = "404", description = "Unable to edit article"),
            })
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
                                    schema = @Schema(implementation = JsonArticleReportResponse.class))),
                    @ApiResponse(
                            responseCode = "204",
                            description = "No articles found for the given type",
                            content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<JsonArticleReportResponse>> getArticlesByType(@PathVariable String type) {
        List<JsonArticleReportResponse> articles = dbArticlesService.getArticlesByType(type);
        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(articles);
    }

    @Operation(
            summary = "Retrieve all article types with articles",
            description = "This endpoint returns all article types along with their corresponding articles from the past specified number of days.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Article types and articles retrieved successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = JsonArticleReportResponse.class)))
            })
    @GetMapping("/article-types-with-articles")
    public ResponseEntity<Map<String, List<JsonArticleReportResponse>>> getAllArticleTypesWithArticles(@RequestParam int days) {
        Map<String, List<JsonArticleReportResponse>> response = dbArticlesService.getAllArticleTypesWithArticles(days);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Increment view count for an article",
            description = "This endpoint increments the view count of a specific article based on its ID.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "View count incremented successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ArticlesEntity.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Article not found",
                            content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/incrementViewCount/{id}")
    public ResponseEntity<?> incrementViewCount(@PathVariable UUID id) {
        Optional<ArticlesEntity> response =
                dbArticlesService.incrementArticleViewCount(id);

        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(response.get());
        }
    }

    @Operation(
            summary = "Retrieve top 10 most viewed articles",
            description = "This endpoint returns the 10 most viewed articles.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved top 10 most viewed articles",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ArticlesEntity.class))),
            })
    @GetMapping("/top-viewed")
    public ResponseEntity<List<ArticlesEntity>> getTopViewedArticles() {
        return ResponseEntity.ok(dbArticlesService.getTop10MostViewedArticles());
    }
}