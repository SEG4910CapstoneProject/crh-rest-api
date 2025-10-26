package me.t65.reportgenapi.db.services;

import static me.t65.reportgenapi.TestUtils.*;
import static me.t65.reportgenapi.TestUtils.assertListContains;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.JsonIocResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.mongo.repository.ArticleContentRepository;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.entities.id.ArticleCategoryId;
import me.t65.reportgenapi.db.postgres.entities.id.IOCArticlesId;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.utils.DateService;
import me.t65.reportgenapi.utils.NormalizeLinks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class DbArticlesServiceImplTests {

    private final String STAT_ID_1 = "1d6aaa8a-d283-4d02-966d-d28889f93889";
    private final String STAT_ID_2 = "816d120b-c1da-40e9-a582-50cfa9ec9c75";
    private final String STAT_ID_3 = "b5b7f678-4483-4693-9662-723613409f05";

    private final UUID STAT_UID_1 = UUID.fromString(STAT_ID_1);
    private final UUID STAT_UID_2 = UUID.fromString(STAT_ID_2);
    private final UUID STAT_UID_3 = UUID.fromString(STAT_ID_3);

    @MockBean ReportRepository reportRepository;

    @MockBean ArticlesRepository articlesRepository;

    @MockBean ReportArticlesRepository reportArticlesRepository;

    @MockBean ArticleContentRepository articleContentRepository;

    @MockBean IOCArticlesEntityRepository iocArticlesEntityRepository;

    @MockBean IOCEntityRepository iocEntityRepository;
    @MockBean IOCTypeEntityRepository iocTypeEntityRepository;
    @MockBean ArticleCategoryRepository articleCategoryRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean MonthlyArticlesRepository monthlyArticlesRepository;

    @MockBean ArticleTypeRepository articleTypeRepository;
    @MockBean DateService dateService;

    @MockBean UserRepository userRepository;
    @MockBean BCryptPasswordEncoder passwordEncoder;

    @MockBean private UserFavouriteRepository userFavouriteRepository;

    @Autowired DbArticlesServiceImpl dbArticlesService;

    @Test
    public void testAddArticlesToReport_success() {
        when(reportArticlesRepository.countByReportArticlesId_ReportIdAndSuggestion(
                        anyInt(), anyBoolean()))
                .thenReturn(3);
        when(articlesRepository.existsById(any())).thenReturn(true);

        boolean actual =
                dbArticlesService.addArticlesToReport(1, new String[] {STAT_ID_1, STAT_ID_2});

        assertTrue(actual);
        ArgumentCaptor<List> statListCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportArticlesRepository).saveAll(statListCaptor.capture());
        assertListContains(
                List.of(
                        getReportArticlesEntity(1, STAT_UID_1, (short) 3, false),
                        getReportArticlesEntity(1, STAT_UID_2, (short) 4, false)),
                statListCaptor.getValue());
    }

    @Test
    public void testAddArticlesToReport_doesNotExist_falseReturn() {
        when(reportArticlesRepository.countByReportArticlesId_ReportIdAndSuggestion(
                        anyInt(), anyBoolean()))
                .thenReturn(3);
        when(articlesRepository.existsById(any())).thenReturn(false);

        boolean actual = dbArticlesService.addArticlesToReport(1, new String[] {STAT_ID_1});

        assertFalse(actual);
        verify(reportArticlesRepository, never()).saveAll(any());
    }

    @Test
    public void testAddArticlesToReport_invalidUid_falseReturn() {
        when(reportArticlesRepository.countByReportArticlesId_ReportIdAndSuggestion(
                        anyInt(), anyBoolean()))
                .thenReturn(3);

        boolean actual = dbArticlesService.addArticlesToReport(1, new String[] {"Bad Uid"});

        assertFalse(actual);
        verify(reportArticlesRepository, never()).saveAll(any());
    }

    @Test
    public void testRemoveArticlesFromReport_success() {
        ReportArticlesEntity reportArticlesEntity1 =
                getReportArticlesEntity(1, STAT_UID_1, (short) 0, false);
        ReportArticlesEntity reportArticlesEntity2 =
                getReportArticlesEntity(1, STAT_UID_2, (short) 1, false);
        ReportArticlesEntity reportArticlesEntity3 =
                getReportArticlesEntity(1, STAT_UID_3, (short) 2, false);

        ReportArticlesEntity expectedNewReportArticlesEntity2 =
                getReportArticlesEntity(1, STAT_UID_2, (short) 0, false);
        ReportArticlesEntity expectedNewReportArticlesEntity3 =
                getReportArticlesEntity(1, STAT_UID_3, (short) 1, false);

        when(reportArticlesRepository.findById(reportArticlesEntity1.getReportArticlesId()))
                .thenReturn(Optional.of(reportArticlesEntity1));
        when(reportArticlesRepository
                        .findByReportArticlesId_ReportIdAndSuggestionOrderByArticleRankAsc(
                                1, false))
                .thenReturn(List.of(reportArticlesEntity2, reportArticlesEntity3));

        boolean actual = dbArticlesService.removeArticlesFromReport(1, new String[] {STAT_ID_1});

        assertTrue(actual);
        ArgumentCaptor<List> deleteStatListCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportArticlesRepository).deleteAll(deleteStatListCaptor.capture());
        assertListContains(List.of(reportArticlesEntity1), deleteStatListCaptor.getValue());

        ArgumentCaptor<List> saveStatListCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportArticlesRepository).saveAll(saveStatListCaptor.capture());
        assertListContains(
                List.of(expectedNewReportArticlesEntity2, expectedNewReportArticlesEntity3),
                saveStatListCaptor.getValue());
    }

    @Test
    public void testRemoveArticlesFromReport_doesNotExist_falseReturn() {
        when(reportArticlesRepository.findById(any())).thenReturn(Optional.empty());

        boolean actual = dbArticlesService.removeArticlesFromReport(1, new String[] {STAT_ID_1});

        assertFalse(actual);
    }

    @Test
    public void testRemoveArticlesFromReport_invalidUid_falseReturn() {
        boolean actual = dbArticlesService.removeArticlesFromReport(1, new String[] {"badUid"});

        assertFalse(actual);
    }

    @Test
    public void testEditArticleInReport_success() {
        ArticleContentEntity articleContentEntity =
                new ArticleContentEntity(
                        STAT_UID_1,
                        "someLinkOriginal",
                        "someNameOriginal",
                        Instant.ofEpochMilli(100),
                        "someDescriptionOriginal");
        String expectedLink = "someLinkNew";
        String expectedName = "someNameNew";
        String expectedDescription = "someDescriptionNew";
        Instant expectedInstant = Instant.ofEpochMilli(500);
        ArticleContentEntity expectedArticleContent =
                new ArticleContentEntity(
                        STAT_UID_1,
                        expectedLink,
                        expectedName,
                        expectedInstant,
                        expectedDescription);

        when(articleContentRepository.findById(any()))
                .thenReturn(Optional.of(articleContentEntity));

        boolean actual =
                dbArticlesService.editArticleInReport(
                        STAT_ID_1,
                        expectedName,
                        expectedLink,
                        expectedDescription,
                        expectedInstant);

        assertTrue(actual);

        verify(articleContentRepository).save(expectedArticleContent);
    }

    @Test
    public void testEditArticleInReport_notFound_falseReturn() {
        when(articleContentRepository.findById(any())).thenReturn(Optional.empty());

        boolean actual =
                dbArticlesService.editArticleInReport(
                        STAT_ID_1, "name", "link", "description", Instant.ofEpochMilli(200));

        assertFalse(actual);
    }

    @Test
    public void testGetReportSuggestions_success() {
        LocalDate publishDate = LocalDate.of(2024, 1, 1);
        when(reportRepository.existsById(anyInt())).thenReturn(true);
        when(iocTypeEntityRepository.findAll()).thenReturn(List.of(new IOCTypeEntity(1, "url")));
        JsonArticleReportResponse expectedResponse =
                new JsonArticleReportResponse(
                        STAT_ID_1,
                        "someTitle",
                        "someDescription",
                        "someCategory",
                        "someLink",
                        List.of(new JsonIocResponse(2, 1, "url", "someIoc1")),
                        publishDate);
        ReportArticlesEntity reportArticlesEntity =
                getReportArticlesEntity(1, STAT_UID_1, (short) 0, true);

        when(reportArticlesRepository.findByReportArticlesId_ReportIdAndSuggestion(1, true))
                .thenReturn(List.of(reportArticlesEntity));
        when(iocArticlesEntityRepository.findByIocArticlesId_ArticleIdIn(any()))
                .thenReturn(List.of(new IOCArticlesEntity(new IOCArticlesId(2, STAT_UID_1))));
        when(articlesRepository.findAllById(any()))
                .thenReturn(
                        List.of(
                                new ArticlesEntity(
                                        STAT_UID_1,
                                        1,
                                        Instant.ofEpochMilli(1000),
                                        publishDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                                        true,
                                        true,
                                        20)));
        when(iocEntityRepository.findAllById(any()))
                .thenReturn(List.of(new IOCEntity(2, 1, "someIoc1")));
        when(articleContentRepository.findAllById(any()))
                .thenReturn(
                        List.of(
                                new ArticleContentEntity(
                                        STAT_UID_1,
                                        expectedResponse.getLink(),
                                        expectedResponse.getTitle(),
                                        Instant.ofEpochMilli(200),
                                        expectedResponse.getDescription())));
        when(articleCategoryRepository.findByArticleCategoryId_ArticleIdIn(any()))
                .thenReturn(
                        List.of(new ArticleCategoryEntity(new ArticleCategoryId(1, STAT_UID_1))));
        when(categoryRepository.findAllById(any()))
                .thenReturn(List.of(new CategoryEntity(1, expectedResponse.getCategory())));

        List<JsonArticleReportResponse> actual = dbArticlesService.getReportSuggestions(1);

        assertEquals(1, actual.size());
        assertEquals(expectedResponse, actual.get(0));
    }

    @Test
    public void testGetReportSuggestions_reportDoesNotExist_empty() {
        when(reportRepository.existsById(anyInt())).thenReturn(false);
        List<JsonArticleReportResponse> actual = dbArticlesService.getReportSuggestions(1);
        assertEquals(0, actual.size());
    }

    @Test
    public void testAddReportSuggestion_success() {
        when(reportRepository.existsById(anyInt())).thenReturn(true);
        when(articlesRepository.existsById(any())).thenReturn(true);
        when(reportArticlesRepository.countByReportArticlesId_ReportIdAndSuggestion(
                        anyInt(), eq(true)))
                .thenReturn(3);

        boolean actual = dbArticlesService.addReportSuggestion(1, STAT_UID_1);

        assertTrue(actual);
        verify(reportArticlesRepository)
                .save(
                        new ReportArticlesEntity(
                                new ReportArticlesId(1, STAT_UID_1), (short) 3, true));
    }

    @Test
    public void testAddReportSuggestion_reportIdDoesNotExist_falseReturn() {
        when(reportRepository.existsById(anyInt())).thenReturn(false);
        boolean actual = dbArticlesService.addReportSuggestion(1, STAT_UID_1);

        assertFalse(actual);
    }

    @Test
    public void testAddReportSuggestion_articleIdDoesNotExist_falseReturn() {
        when(reportRepository.existsById(anyInt())).thenReturn(true);
        when(articlesRepository.existsById(any())).thenReturn(false);
        boolean actual = dbArticlesService.addReportSuggestion(1, STAT_UID_1);

        assertFalse(actual);
    }

    @Test
    public void testRemoveReportSuggestion_success() {
        ReportArticlesEntity reportArticlesEntity1 =
                getReportArticlesEntity(1, STAT_UID_1, (short) 0, true);
        ReportArticlesEntity reportArticlesEntity2 =
                getReportArticlesEntity(1, STAT_UID_2, (short) 1, true);
        ReportArticlesEntity reportArticlesEntity3 =
                getReportArticlesEntity(1, STAT_UID_3, (short) 2, true);

        ReportArticlesEntity expectedNewReportArticlesEntity2 =
                getReportArticlesEntity(1, STAT_UID_2, (short) 0, true);
        ReportArticlesEntity expectedNewReportArticlesEntity3 =
                getReportArticlesEntity(1, STAT_UID_3, (short) 1, true);

        when(reportRepository.existsById(anyInt())).thenReturn(true);
        when(reportArticlesRepository
                        .existsByReportArticlesId_ReportIdAndReportArticlesId_ArticleIdAndSuggestion(
                                anyInt(), any(), eq(true)))
                .thenReturn(true);
        when(reportArticlesRepository
                        .findByReportArticlesId_ReportIdAndSuggestionOrderByArticleRankAsc(
                                anyInt(), eq(true)))
                .thenReturn(
                        List.of(
                                reportArticlesEntity1,
                                reportArticlesEntity2,
                                reportArticlesEntity3));

        boolean actual = dbArticlesService.removeReportSuggestion(1, STAT_UID_1);

        assertTrue(actual);
        verify(reportArticlesRepository).deleteById(new ReportArticlesId(1, STAT_UID_1));
        ArgumentCaptor<List> saveListCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportArticlesRepository).saveAll(saveListCaptor.capture());
        assertListContains(
                List.of(expectedNewReportArticlesEntity2, expectedNewReportArticlesEntity3),
                saveListCaptor.getValue());
    }

    @Test
    public void testRemoveReportSuggestion_reportDoesNotExist_falseReturn() {
        when(reportRepository.existsById(anyInt())).thenReturn(false);

        boolean actual = dbArticlesService.removeReportSuggestion(1, STAT_UID_1);

        assertFalse(actual);
    }

    @Test
    public void testRemoveReportSuggestion_reportArticleDoesNotExist_trueReturn() {
        when(reportRepository.existsById(anyInt())).thenReturn(true);
        when(reportArticlesRepository
                        .existsByReportArticlesId_ReportIdAndReportArticlesId_ArticleIdAndSuggestion(
                                anyInt(), any(), eq(true)))
                .thenReturn(false);

        boolean actual = dbArticlesService.removeReportSuggestion(1, STAT_UID_1);

        assertTrue(actual);
    }

    @Test
    public void testGetArticleToIocEntityListMap_success() {
        List<UUID> articleIds = List.of(STAT_UID_1, STAT_UID_2);
        List<IOCArticlesEntity> iocArticlesEntities =
                List.of(
                        new IOCArticlesEntity(new IOCArticlesId(1, STAT_UID_1)),
                        new IOCArticlesEntity(new IOCArticlesId(2, STAT_UID_1)),
                        new IOCArticlesEntity(new IOCArticlesId(3, STAT_UID_2)));
        List<IOCEntity> iocEntities =
                List.of(
                        new IOCEntity(1, 1, "1"),
                        new IOCEntity(2, 2, "2"),
                        new IOCEntity(3, 3, "3"));

        when(iocArticlesEntityRepository.findByIocArticlesId_ArticleIdIn(eq(articleIds)))
                .thenReturn(iocArticlesEntities);
        when(iocEntityRepository.findAllById(any())).thenReturn(iocEntities);

        Map<UUID, List<IOCEntity>> actual =
                dbArticlesService.getArticleToIocEntityListMap(articleIds);

        assertEquals(List.of(iocEntities.get(0), iocEntities.get(1)), actual.get(STAT_UID_1));
        assertEquals(List.of(iocEntities.get(2)), actual.get(STAT_UID_2));
    }

    @Test
    public void testAddNewArticle_success() {
        String expectedTitle = "someTitle";
        String expectedLink = "someLink";
        long expectedNormalizedLink = NormalizeLinks.normalizeAndHashLink(expectedLink);
        String expectedDescription = "someDescription";
        Instant expectedPublishDate = Instant.ofEpochMilli(1000);
        Instant expectedIngestDate = Instant.ofEpochMilli(2000);

        ArticlesEntity expectedArticlesEntity =
                new ArticlesEntity(
                        STAT_UID_1,
                        99,
                        expectedIngestDate,
                        expectedPublishDate,
                        false,
                        false,
                        expectedNormalizedLink);
        ArticleContentEntity expectedArticleContentEntity =
                new ArticleContentEntity(
                        STAT_UID_1,
                        expectedLink,
                        expectedTitle,
                        expectedPublishDate,
                        expectedDescription);

        when(dateService.getCurrentInstant()).thenReturn(expectedIngestDate);

        dbArticlesService.addNewArticle(
                STAT_UID_1, expectedTitle, expectedLink, expectedDescription, expectedPublishDate);

        verify(articlesRepository).save(expectedArticlesEntity);
        verify(articleContentRepository).save(expectedArticleContentEntity);
    }

    @Test
    public void testGetArticleByLink_success() {
        String sampleLink = "https://example.com/resource";
        String expectedLink = "example.com/resource";
        long expectedHash = NormalizeLinks.normalizeAndHashLink(expectedLink);

        ArticlesEntity expectedArticleEntity =
                new ArticlesEntity(
                        STAT_UID_1,
                        1,
                        Instant.ofEpochMilli(1000),
                        Instant.ofEpochMilli(2000),
                        true,
                        true,
                        expectedHash);
        List<ArticlesEntity> articlesEntities =
                List.of(
                        expectedArticleEntity,
                        new ArticlesEntity(
                                STAT_UID_2,
                                2,
                                Instant.ofEpochMilli(3000),
                                Instant.ofEpochMilli(4000),
                                true,
                                true,
                                expectedHash));

        ArticleContentEntity expectedArticleContentEntity =
                new ArticleContentEntity(
                        STAT_UID_1,
                        expectedLink,
                        "someTitle",
                        Instant.ofEpochMilli(2000),
                        "someDescription");
        List<ArticleContentEntity> articleContentEntities =
                List.of(
                        expectedArticleContentEntity,
                        new ArticleContentEntity(
                                STAT_UID_2,
                                "a bad link",
                                "someTitle2",
                                Instant.ofEpochMilli(4000),
                                "someDescription2"));

        List<IOCArticlesEntity> iocArticlesEntities =
                List.of(new IOCArticlesEntity(new IOCArticlesId(1, STAT_UID_1)));
        List<IOCEntity> iocEntities = List.of(new IOCEntity(1, 1, "someValue"));

        JsonArticleReportResponse expectedResponse =
                new JsonArticleReportResponse(
                        STAT_ID_1,
                        "someTitle",
                        "someDescription",
                        "someCategory",
                        expectedLink,
                        List.of(new JsonIocResponse(1, 1, "url", "someValue")),
                        LocalDate.ofInstant(Instant.ofEpochMilli(2000), ZoneOffset.UTC));

        when(articlesRepository.findByHashlink(expectedHash)).thenReturn(articlesEntities);
        when(articleContentRepository.findAllById(any())).thenReturn(articleContentEntities);
        when(iocArticlesEntityRepository.findByIocArticlesId_ArticleIdIn(any()))
                .thenReturn(iocArticlesEntities);
        when(iocEntityRepository.findAllById(any())).thenReturn(iocEntities);
        when(iocTypeEntityRepository.findAll()).thenReturn(List.of(new IOCTypeEntity(1, "url")));
        when(articleCategoryRepository.findByArticleCategoryId_ArticleIdIn(any()))
                .thenReturn(
                        List.of(new ArticleCategoryEntity(new ArticleCategoryId(1, STAT_UID_1))));
        when(categoryRepository.findAllById(any()))
                .thenReturn(List.of(new CategoryEntity(1, expectedResponse.getCategory())));

        Optional<JsonArticleReportResponse> actual = dbArticlesService.getArticleByLink(sampleLink);

        assertTrue(actual.isPresent());
        assertEquals(expectedResponse, actual.get());
    }

    @Test
    public void testGetArticleByLink_noMatchingLink_emptyOptional() {
        String sampleLink = "https://example.com/resource";
        long expectedHash = NormalizeLinks.normalizeAndHashLink(sampleLink);

        ArticlesEntity expectedArticleEntity =
                new ArticlesEntity(
                        STAT_UID_1,
                        1,
                        Instant.ofEpochMilli(1000),
                        Instant.ofEpochMilli(2000),
                        true,
                        true,
                        expectedHash);
        List<ArticlesEntity> articlesEntities =
                List.of(
                        expectedArticleEntity,
                        new ArticlesEntity(
                                STAT_UID_2,
                                2,
                                Instant.ofEpochMilli(3000),
                                Instant.ofEpochMilli(4000),
                                true,
                                true,
                                expectedHash));

        ArticleContentEntity expectedArticleContentEntity =
                new ArticleContentEntity(
                        STAT_UID_1,
                        "still a bad link",
                        "someTitle",
                        Instant.ofEpochMilli(2000),
                        "someDescription");
        List<ArticleContentEntity> articleContentEntities =
                List.of(
                        expectedArticleContentEntity,
                        new ArticleContentEntity(
                                STAT_UID_2,
                                "a bad link",
                                "someTitle2",
                                Instant.ofEpochMilli(4000),
                                "someDescription2"));

        when(articlesRepository.findByHashlink(expectedHash)).thenReturn(articlesEntities);
        when(articleContentRepository.findAllById(any())).thenReturn(articleContentEntities);

        Optional<JsonArticleReportResponse> actual = dbArticlesService.getArticleByLink(sampleLink);

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetArticleByLink_noMatchingHash_emptyOptional() {
        String sampleLink = "https://example.com/resource";

        when(articlesRepository.findByHashlink(anyLong())).thenReturn(List.of());

        Optional<JsonArticleReportResponse> actual = dbArticlesService.getArticleByLink(sampleLink);

        assertTrue(actual.isEmpty());
    }

    private ReportArticlesEntity getReportArticlesEntity(
            Integer reportId, UUID articleId, short rank, boolean suggestion) {
        return new ReportArticlesEntity(
                new ReportArticlesId(reportId, articleId), rank, suggestion);
    }

    @Test
    void testGetArticleToCategoryEntityMap_success() {
        // Define the input UUIDs
        UUID articleId1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID articleId2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        Collection<UUID> articleIds = Arrays.asList(articleId1, articleId2);

        // Create mock ArticleCategoryEntity objects
        ArticleCategoryEntity acEntity1 = new ArticleCategoryEntity();
        acEntity1.setArticleCategoryId(new ArticleCategoryId(1, articleId1));
        ArticleCategoryEntity acEntity2 = new ArticleCategoryEntity();
        acEntity2.setArticleCategoryId(new ArticleCategoryId(2, articleId2));
        List<ArticleCategoryEntity> acEntities = Arrays.asList(acEntity1, acEntity2);

        // Create mock CategoryEntity objects
        CategoryEntity cEntity1 = new CategoryEntity();
        cEntity1.setCategoryId(1);
        cEntity1.setCategoryName("Category 1");
        CategoryEntity cEntity2 = new CategoryEntity();
        cEntity2.setCategoryId(2);
        cEntity2.setCategoryName("Category 2");
        List<CategoryEntity> cEntities = Arrays.asList(cEntity1, cEntity2);

        when(articleCategoryRepository.findByArticleCategoryId_ArticleIdIn(articleIds))
                .thenReturn(acEntities);
        when(categoryRepository.findAllById(anySet())).thenReturn(cEntities);

        Map<UUID, CategoryEntity> result =
                dbArticlesService.getArticleToCategoryEntityMap(articleIds);

        verify(articleCategoryRepository).findByArticleCategoryId_ArticleIdIn(articleIds);
        verify(categoryRepository).findAllById(Set.of(1, 2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(articleId1));
        assertTrue(result.containsKey(articleId2));
        assertEquals("Category 1", result.get(articleId1).getCategoryName());
        assertEquals("Category 2", result.get(articleId2).getCategoryName());
    }
}
