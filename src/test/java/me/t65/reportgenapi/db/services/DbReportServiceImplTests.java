package me.t65.reportgenapi.db.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.mongo.repository.ArticleContentRepository;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.entities.id.ArticleCategoryId;
import me.t65.reportgenapi.db.postgres.entities.id.IOCArticlesId;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.generators.JsonReportGenerator;
import me.t65.reportgenapi.reportformatter.RawReport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class DbReportServiceImplTests {
    private final String MOCK_ARTICLE_ID_STR = "1be0a088-1fc8-4b2d-8628-5046b3c8c015";
    private final UUID MOCK_ARTICLE_ID = UUID.fromString(MOCK_ARTICLE_ID_STR);

    @MockBean ReportRepository reportRepository;

    @MockBean ArticlesRepository articlesRepository;

    @MockBean ReportArticlesRepository reportArticlesRepository;

    @MockBean ArticleContentRepository articleContentRepository;

    @MockBean IOCArticlesEntityRepository iocArticlesEntityRepository;

    @MockBean IOCEntityRepository iocEntityRepository;

    @MockBean IOCTypeEntityRepository iocTypeEntityRepository;

    @MockBean JsonReportGenerator jsonReportGenerator;

    @MockBean ReportStatisticsRepository reportStatisticsRepository;

    @MockBean StatisticsRepository statisticsRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean ArticleCategoryRepository articleCategoryRepository;
    @MockBean ArticleTypeRepository articleTypeRepository;
    @MockBean MonthlyArticlesRepository monthlyArticlesRepository;

    @MockBean UserRepository userRepository;
    @MockBean BCryptPasswordEncoder passwordEncoder;

    @Autowired DbReportServiceImpl dbService;

    //     @Test
    //     public void testSearchReports_success() {
    //         int reportId = 1;
    //         int iocID = 2;
    //         ReportType type = ReportType.daily;
    //         ReportEntity mockReport = mock(ReportEntity.class);
    //         ArticleContentEntity mockReportProperty = mock(ArticleContentEntity.class);
    //         IOCArticlesEntity iocArticlesEntity =
    //                 new IOCArticlesEntity(new IOCArticlesId(iocID, MOCK_ARTICLE_ID));

    //         mockFakeArticle(mockReportProperty, iocArticlesEntity);
    //         when(reportRepository.findByReportType(eq(type),
    // any())).thenReturn(List.of(mockReport));
    //         when(iocTypeEntityRepository.findAll()).thenReturn(List.of(new IOCTypeEntity(1,
    // "url")));

    //         when(jsonReportGenerator.generateShortDetails(any(), any(), any(), any()))
    //                 .thenReturn(
    //                         new SearchReportDetailsResponse(
    //                                 reportId,
    //                                 type.toString(),
    //                                 null,
    //                                 null,
    //                                 false,
    //                                 Collections.emptyList(),
    //                                 Collections.emptyList(),
    //                                 Collections.emptyList()));

    //         SearchReportResponse results = dbService.searchReports(null, null, type, 0, 10);

    //         assertEquals(1, results.getReports().size());
    //         SearchReportDetailsResponse actual = results.getReports().get(0);

    //         assertEquals(reportId, actual.getReportId());
    //         assertEquals(type.toString(), actual.getReportType());
    //         assertEquals(false, actual.getEmailStatus());
    //     }

    //     @Test
    //     public void testSearchReports_dateRange_success() {
    //         LocalDate dateStart = LocalDate.parse("2023-01-01");
    //         LocalDate dateEnd = LocalDate.parse("2023-01-31");

    //         int reportId = 1;
    //         int iocID = 2;
    //         ReportType type = ReportType.daily;
    //         ReportEntity mockReport = mock(ReportEntity.class);
    //         ArticleContentEntity mockReportProperty = mock(ArticleContentEntity.class);
    //         IOCArticlesEntity iocArticlesEntity =
    //                 new IOCArticlesEntity(new IOCArticlesId(iocID, MOCK_ARTICLE_ID));

    //         mockFakeArticle(mockReportProperty, iocArticlesEntity);
    //         when(reportRepository.findByGenerateDateBetweenAndReportType(any(), any(), eq(type),
    // any()))
    //                 .thenReturn(List.of(mockReport));
    //         when(iocTypeEntityRepository.findAll()).thenReturn(List.of(new IOCTypeEntity(1,
    // "url")));
    //         when(jsonReportGenerator.generateShortDetails(any(), any(), any(), any()))
    //                 .thenReturn(
    //                         new SearchReportDetailsResponse(
    //                                 reportId,
    //                                 type.toString(),
    //                                 null,
    //                                 null,
    //                                 false,
    //                                 Collections.emptyList(),
    //                                 Collections.emptyList(),
    //                                 Collections.emptyList()));

    //         SearchReportResponse results = dbService.searchReports(dateStart, dateEnd, type, 0,
    // 10);

    //         assertEquals(1, results.getReports().size());
    //         SearchReportDetailsResponse actual = results.getReports().get(0);

    //         assertEquals(reportId, actual.getReportId());
    //         assertEquals(type.toString(), actual.getReportType());
    //         assertEquals(false, actual.getEmailStatus());
    //     }

    @Test
    public void testGetLatestReportId_emptyRepository() {
        when(reportRepository.count()).thenReturn(0L);

        int result = dbService.getLatestReportId();

        assertEquals(-1, result);
    }

    @Test
    public void testGetRawReport_reportDoesNotExist() {
        Optional<RawReport> result = dbService.getRawReport(1);

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetRawReport_reportExists() {
        int reportId = 1;
        int iocID = 2;
        ReportEntity mockReport = mock(ReportEntity.class);
        ReportArticlesEntity mockReportArticle = mock(ReportArticlesEntity.class);
        List<ReportArticlesEntity> mockReportArticles =
                Collections.singletonList(mockReportArticle);
        ArticlesEntity mockArticle = mock(ArticlesEntity.class);
        ArticleContentEntity mockReportProperty = mock(ArticleContentEntity.class);
        IOCArticlesEntity iocArticlesEntity =
                new IOCArticlesEntity(new IOCArticlesId(iocID, MOCK_ARTICLE_ID));
        ArticleCategoryEntity mockArticleCategoryEntity =
                new ArticleCategoryEntity(new ArticleCategoryId(1, MOCK_ARTICLE_ID));
        CategoryEntity mockCategoryEntity = new CategoryEntity(1, "SomeCategory");

        mockFakeArticle(mockReportProperty, iocArticlesEntity);
        ReportArticlesId mockReportArticlesId = mock(ReportArticlesId.class);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));
        when(iocTypeEntityRepository.findAll()).thenReturn(List.of(new IOCTypeEntity(1, "url")));
        when(reportArticlesRepository.findByReportArticlesId_ReportIdAndSuggestion(reportId, false))
                .thenReturn(mockReportArticles);
        when(mockReportArticle.getReportArticlesId()).thenReturn(mockReportArticlesId);
        when(mockReportArticlesId.getArticleId()).thenReturn(MOCK_ARTICLE_ID);
        when(articlesRepository.findAllById(any()))
                .thenReturn(Collections.singletonList(mockArticle));
        when(mockArticle.getArticleId()).thenReturn(MOCK_ARTICLE_ID);
        when(articleCategoryRepository.findByArticleCategoryId_ArticleIdIn(any()))
                .thenReturn(List.of(mockArticleCategoryEntity));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(mockCategoryEntity));

        Optional<RawReport> result = dbService.getRawReport(reportId);

        assertTrue(result.isPresent());
        assertEquals(mockReport, result.get().getReport());
        assertEquals(mockReportArticles, result.get().getReportArticles());
        assertEquals(mockArticle, result.get().getArticles().get(MOCK_ARTICLE_ID));
        assertEquals(mockReportProperty, result.get().getArticleContent().get(MOCK_ARTICLE_ID));
        assertEquals(mockCategoryEntity, result.get().getCategoryEntityMap().get(MOCK_ARTICLE_ID));
    }

    private void mockFakeArticle(
            ArticleContentEntity mockReportProperty, IOCArticlesEntity iocArticlesEntity) {

        when(articleContentRepository.findAllById(any()))
                .thenReturn(Collections.singletonList(mockReportProperty));
        when(mockReportProperty.getId()).thenReturn(MOCK_ARTICLE_ID);
        when(iocArticlesEntityRepository.findByIocArticlesId_ArticleIdIn(any()))
                .thenReturn(Collections.singletonList(iocArticlesEntity));
    }
}
