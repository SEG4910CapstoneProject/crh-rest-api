package me.t65.reportgenapi.reportformatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.config.RestApiConfig;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.postgres.entities.ArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportType;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.utils.ResourceReader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class HtmlReportFormatterTests {

    private static final String reportTemplate =
            """
            <link>{{LINK-TO-DASHBOARD}}</link>
            <stat>{{STAT-LIST}}</stat>
            <categories>{{CATEGORY-LIST}}</categories>
            <link>{{LINK-TO-DASHBOARD}}</link>
            """;

    private static final String categoryTemplate =
            """
            <category>
                <cat-title>{{CATEGORY-TITLE}}</cat-title>
                <articles>{{ARTICLE-LIST}}</articles>
            </category>
            """;

    private static final String articleTemplate =
            """
            <article>
                <title>{{ARTICLE-TITLE}}</title>
                <description>{{ARTICLE-INFO}}</description>
                <link>{{ARTICLE-LINK}}</link>
            </article>
            """;
    @Mock private RestApiConfig restApiConfig;

    @Mock private ResourceReader resourceReader;

    @InjectMocks private HtmlReportFormatter htmlReportFormatter;

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
                        true);

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

        String expectedDashboardLink = "example.com";

        String expectedResponse =
                """
                <link>example.com</link>
                <stat></stat>
                <categories><category>
                    <cat-title>News Articles</cat-title>
                    <articles><article>
                    <title>article1</title>
                    <description>article 1 description</description>
                    <link>example.com/1</link>
                </article>

                <article>
                    <title>article2</title>
                    <description>article 2 description</description>
                    <link>example.com/2</link>
                </article>

                </articles>
                </category>
                </categories>
                <link>example.com</link>
                """;

        mockResourceTemplates();
        when(restApiConfig.getDashboardLink()).thenReturn(expectedDashboardLink);

        ResponseEntity<?> actual = htmlReportFormatter.format(expectedReport);
        String response = (String) actual.getBody();

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expectedResponse.trim(), response.trim());
    }

    @Test
    public void testFormat_resourceLoadFail_internalServerError() {
        Resource mockEmailReportTemplate = mock(Resource.class);
        when(restApiConfig.getEmailReportTemplate()).thenReturn(mockEmailReportTemplate);
        when(resourceReader.readResourceAsString(mockEmailReportTemplate))
                .thenThrow(new RuntimeException("Test Exception"));

        ResponseEntity<?> actual = htmlReportFormatter.format(null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    private void mockResourceTemplates() {
        Resource mockEmailReportTemplate = mock(Resource.class);
        Resource mockCategoryTemplate = mock(Resource.class);
        Resource mockArticleTemplate = mock(Resource.class);

        when(restApiConfig.getEmailReportTemplate()).thenReturn(mockEmailReportTemplate);
        when(restApiConfig.getCategoryTemplate()).thenReturn(mockCategoryTemplate);
        when(restApiConfig.getArticleTemplate()).thenReturn(mockArticleTemplate);

        when(resourceReader.readResourceAsString(mockEmailReportTemplate))
                .thenReturn(reportTemplate);
        when(resourceReader.readResourceAsString(mockCategoryTemplate))
                .thenReturn(categoryTemplate);
        when(resourceReader.readResourceAsString(mockArticleTemplate)).thenReturn(articleTemplate);
    }
}
