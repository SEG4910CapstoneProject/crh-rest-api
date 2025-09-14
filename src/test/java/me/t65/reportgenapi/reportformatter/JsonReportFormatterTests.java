package me.t65.reportgenapi.reportformatter;

import static org.junit.jupiter.api.Assertions.*;

import me.t65.reportgenapi.controller.payload.JsonReportResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.postgres.entities.ArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportType;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.generators.JsonArticleGenerator;
import me.t65.reportgenapi.generators.JsonReportGenerator;
import me.t65.reportgenapi.generators.JsonStatGenerator;
import me.t65.reportgenapi.utils.JSoupHtmlRemover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonReportFormatterTests {

    private JsonReportFormatter jsonReportFormatter;

    @BeforeEach
    public void beforeEach() {
        jsonReportFormatter =
                new JsonReportFormatter(
                        new JsonReportGenerator(
                                new JsonArticleGenerator(new JSoupHtmlRemover()),
                                new JsonStatGenerator()));
    }

    @Test
    public void testFormat_success() {
        int reportId = 1;
        UUID articleId1 = UUID.fromString("24920bf3-b0b6-4e4a-b475-8709a2c6587e");
        UUID articleId2 = UUID.fromString("6e709535-d3f6-424f-89b7-738075517b2a");
        ReportEntity reportEntity =
                new ReportEntity(
                        reportId,
                        Instant.ofEpochMilli(1000),
                        ReportType.daily,
                        Instant.ofEpochMilli(1000),
                        true,
                        new byte[] {});

        ReportArticlesEntity reportArticlesEntity1 =
                new ReportArticlesEntity(
                        new ReportArticlesId(reportId, articleId1), (short) 0, false);
        ArticlesEntity articlesEntity1 =
                new ArticlesEntity(
                        articleId1,
                        0,
                        Instant.ofEpochMilli(1001),
                        Instant.ofEpochMilli(1002),
                        true,
                        true,
                        100);
        ArticleContentEntity articleContentEntity1 =
                new ArticleContentEntity(
                        articleId1,
                        "example.com/1",
                        "article1",
                        Instant.ofEpochMilli(1003),
                        "article 1 description");

        ReportArticlesEntity reportArticlesEntity2 =
                new ReportArticlesEntity(
                        new ReportArticlesId(reportId, articleId2), (short) 1, false);
        ArticlesEntity articlesEntity2 =
                new ArticlesEntity(
                        articleId2,
                        0,
                        Instant.ofEpochMilli(1004),
                        Instant.ofEpochMilli(1005),
                        true,
                        true,
                        101);
        ArticleContentEntity articleContentEntity2 =
                new ArticleContentEntity(
                        articleId2,
                        "example.com/2",
                        "article2",
                        Instant.ofEpochMilli(1006),
                        "article 2 description");

        RawReport expectedReport =
                new RawReport(
                        reportEntity,
                        List.of(reportArticlesEntity1, reportArticlesEntity2),
                        Map.of(articleId1, articlesEntity1, articleId2, articlesEntity2),
                        Map.of(
                                articleId1,
                                articleContentEntity1,
                                articleId2,
                                articleContentEntity2),
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        Map.of());

        ResponseEntity<?> actual = jsonReportFormatter.format(expectedReport);
        JsonReportResponse response = (JsonReportResponse) actual.getBody();

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(reportId, response.getReportId());
        assertEquals(ReportType.daily.toString(), response.getReportType());
        assertEquals(
                LocalDateTime.ofInstant(reportEntity.getLastModified(), ZoneOffset.UTC),
                response.getLastModified());
        assertEquals(reportEntity.getGenerateDate(), response.getGeneratedDate());
        assertEquals(2, response.getArticles().size());
    }
}
