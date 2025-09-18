package me.t65.reportgenapi.generators;

import static org.junit.jupiter.api.Assertions.*;

import me.t65.reportgenapi.controller.payload.JsonIocResponse;
import me.t65.reportgenapi.controller.payload.JsonReportResponse;
import me.t65.reportgenapi.controller.payload.SearchReportDetailsResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.reportformatter.RawReport;
import me.t65.reportgenapi.utils.JSoupHtmlRemover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonReportGeneratorTests {
    private static final int REPORT_ID = 1;
    private static final UUID ARTICLE_ID_1 =
            UUID.fromString("24920bf3-b0b6-4e4a-b475-8709a2c6587e");
    private static final UUID ARTICLE_ID_2 =
            UUID.fromString("6e709535-d3f6-424f-89b7-738075517b2a");

    private static final ReportEntity REPORT_ENTITY =
            new ReportEntity(
                    REPORT_ID,
                    Instant.ofEpochMilli(1000),
                    ReportType.daily,
                    Instant.ofEpochMilli(1000),
                    true,
                    new byte[] {});

    private static final ReportArticlesEntity REPORT_ARTICLES_ENTITY_1 =
            new ReportArticlesEntity(
                    new ReportArticlesId(REPORT_ID, ARTICLE_ID_1), (short) 0, false);
    private static final ArticlesEntity ARTICLES_ENTITY_1 =
            new ArticlesEntity(
                    ARTICLE_ID_1,
                    0,
                    Instant.ofEpochMilli(1001),
                    Instant.ofEpochMilli(1002),
                    true,
                    true,
                    100);
    private static final ArticleContentEntity ARTICLE_CONTENT_ENTITY_1 =
            new ArticleContentEntity(
                    ARTICLE_ID_1,
                    "example.com/1",
                    "article1",
                    Instant.ofEpochMilli(1003),
                    "article 1 description");

    private static final ReportArticlesEntity REPORT_ARTICLES_ENTITY_2 =
            new ReportArticlesEntity(
                    new ReportArticlesId(REPORT_ID, ARTICLE_ID_2), (short) 1, false);
    private static final ArticlesEntity ARTICLES_ENTITY_2 =
            new ArticlesEntity(
                    ARTICLE_ID_2,
                    0,
                    Instant.ofEpochMilli(1004),
                    Instant.ofEpochMilli(1005),
                    true,
                    true,
                    101);
    private static final ArticleContentEntity ARTICLE_CONTENT_ENTITY_2 =
            new ArticleContentEntity(
                    ARTICLE_ID_2,
                    "example.com/2",
                    "article2",
                    Instant.ofEpochMilli(1006),
                    "article 2 description");

    private static final CategoryEntity ARTICLE_1_CATEGORY = new CategoryEntity(1, "someCat1");
    private static final CategoryEntity ARTICLE_2_CATEGORY = new CategoryEntity(2, "someCat2");

    private JsonReportGenerator jsonReportGenerator;

    @BeforeEach
    public void beforeEach() {
        jsonReportGenerator =
                new JsonReportGenerator(
                        new JsonArticleGenerator(new JSoupHtmlRemover()), new JsonStatGenerator());
    }

    @Test
    public void testGenerateFromRawReport_success() {
        RawReport expectedReport =
                new RawReport(
                        REPORT_ENTITY,
                        List.of(REPORT_ARTICLES_ENTITY_1, REPORT_ARTICLES_ENTITY_2),
                        Map.of(ARTICLE_ID_1, ARTICLES_ENTITY_1, ARTICLE_ID_2, ARTICLES_ENTITY_2),
                        Map.of(
                                ARTICLE_ID_1,
                                ARTICLE_CONTENT_ENTITY_1,
                                ARTICLE_ID_2,
                                ARTICLE_CONTENT_ENTITY_2),
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        Map.of(ARTICLE_ID_1, ARTICLE_1_CATEGORY, ARTICLE_ID_2, ARTICLE_2_CATEGORY));

        JsonReportResponse actual = jsonReportGenerator.generateFromRawReport(expectedReport);

        assertEquals(REPORT_ID, actual.getReportId());
        assertEquals(ReportType.daily.toString(), actual.getReportType());
        assertEquals(
                LocalDateTime.ofInstant(REPORT_ENTITY.getLastModified(), ZoneOffset.UTC),
                actual.getLastModified());
        assertEquals(REPORT_ENTITY.getGenerateDate(), actual.getGeneratedDate());
        assertEquals(2, actual.getArticles().size());
        assertEquals(
                ARTICLE_1_CATEGORY.getCategoryName(), actual.getArticles().get(0).getCategory());
        assertEquals(
                ARTICLE_2_CATEGORY.getCategoryName(), actual.getArticles().get(1).getCategory());
        assertTrue(actual.getEmailStatus());
    }

    @Test
    public void testGenerateShortDetails_success() {
        IOCEntity iocEntity1 = new IOCEntity(1, 1, "test1");
        IOCEntity iocEntity2 = new IOCEntity(2, 1, "test2");
        List<String> expectedTitles =
                List.of(ARTICLE_CONTENT_ENTITY_1.getName(), ARTICLE_CONTENT_ENTITY_2.getName());
        List<JsonIocResponse> expectedIocs =
                List.of(
                        new JsonIocResponse(
                                iocEntity1.getIocID(),
                                iocEntity1.getIocTypeId(),
                                "url",
                                iocEntity1.getValue()),
                        new JsonIocResponse(
                                iocEntity2.getIocID(),
                                iocEntity2.getIocTypeId(),
                                "url",
                                iocEntity2.getValue()));

        SearchReportDetailsResponse actual =
                jsonReportGenerator.generateShortDetails(
                        REPORT_ENTITY,
                        List.of(ARTICLE_CONTENT_ENTITY_1, ARTICLE_CONTENT_ENTITY_2),
                        List.of(iocEntity1, iocEntity2),
                        Map.of(1, "url"));

        assertEquals(REPORT_ID, actual.getReportId());
        assertEquals(ReportType.daily.toString(), actual.getReportType());
        assertEquals(
                LocalDateTime.ofInstant(REPORT_ENTITY.getLastModified(), ZoneOffset.UTC),
                actual.getLastModified());
        assertEquals(
                LocalDateTime.ofInstant(REPORT_ENTITY.getGenerateDate(), ZoneOffset.UTC),
                actual.getGeneratedDate());
        assertEquals(2, actual.getArticleTitles().size());
        assertEquals(expectedTitles, actual.getArticleTitles());
        assertEquals(expectedIocs, actual.getIocs());
        assertTrue(actual.getEmailStatus());
    }
}
