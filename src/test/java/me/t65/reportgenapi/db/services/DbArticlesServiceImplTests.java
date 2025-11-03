package me.t65.reportgenapi.db.services;

import static me.t65.reportgenapi.TestUtils.*;
import static me.t65.reportgenapi.TestUtils.assertListContains;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.JsonIocResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.mongo.repository.ArticleContentRepository;
import me.t65.reportgenapi.db.postgres.dto.MonthlyArticleDTO;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.entities.id.ArticleCategoryId;
import me.t65.reportgenapi.db.postgres.entities.id.ArticleTypeEntity;
import me.t65.reportgenapi.db.postgres.entities.id.IOCArticlesId;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.utils.DateService;
import me.t65.reportgenapi.utils.NormalizeLinks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

    @MockBean private DbUserTagsService dbUserTagsService;
    @MockBean private UserTagRepository userTagRepository;
    @MockBean private UserTagArticleRepository userTagArticleRepository;

    @Autowired DbArticlesServiceImpl dbArticlesService;

    @BeforeEach
    void setUp() {
        dbArticlesService = Mockito.spy(dbArticlesService);
    }

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

    @Test
    void testGetArticlesByType_success() {
        String articleType = "TECH";
        UUID articleId1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID articleId2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

        ArticleTypeEntity atEntity1 = new ArticleTypeEntity(articleType, articleId1);
        ArticleTypeEntity atEntity2 = new ArticleTypeEntity(articleType, articleId2);
        List<ArticleTypeEntity> articleTypeEntities = List.of(atEntity1, atEntity2);

        JsonArticleReportResponse response1 =
                new JsonArticleReportResponse(
                        articleId1.toString(),
                        "Article 1 Title",
                        "Description...",
                        "Category",
                        "Link",
                        Collections.emptyList(),
                        LocalDate.now());
        JsonArticleReportResponse response2 =
                new JsonArticleReportResponse(
                        articleId2.toString(),
                        "Article 2 Title",
                        "Description...",
                        "Category",
                        "Link",
                        Collections.emptyList(),
                        LocalDate.now());

        when(articleTypeRepository.findByArticleType(articleType)).thenReturn(articleTypeEntities);
        when(dbArticlesService.getArticleById(articleId1)).thenReturn(Optional.of(response1));
        when(dbArticlesService.getArticleById(articleId2)).thenReturn(Optional.of(response2));

        List<JsonArticleReportResponse> result = dbArticlesService.getArticlesByType(articleType);

        verify(articleTypeRepository, times(1)).findByArticleType(articleType);
        verify(dbArticlesService, times(1)).getArticleById(articleId1);
        verify(dbArticlesService, times(1)).getArticleById(articleId2);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(articleId1.toString(), result.get(0).getArticleId());
        assertEquals(articleId2.toString(), result.get(1).getArticleId());
    }

    @Test
    void testGetArticlesByType_noEntitiesFound_returnsEmptyList() {
        String articleType = "NON_EXISTENT_TYPE";

        when(articleTypeRepository.findByArticleType(articleType))
                .thenReturn(Collections.emptyList());

        List<JsonArticleReportResponse> result = dbArticlesService.getArticlesByType(articleType);

        verify(articleTypeRepository, times(1)).findByArticleType(articleType);

        verify(dbArticlesService, never()).getArticleById(any(UUID.class));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetArticlesByType_partialRetrieval_filtersMissingArticle() {
        String articleType = "PARTIAL";
        UUID articleIdToFind = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID articleIdToMiss = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ArticleTypeEntity atEntityFind = new ArticleTypeEntity(articleType, articleIdToFind);
        ArticleTypeEntity atEntityMiss = new ArticleTypeEntity(articleType, articleIdToMiss);
        List<ArticleTypeEntity> articleTypeEntities = List.of(atEntityFind, atEntityMiss);

        JsonArticleReportResponse foundResponse =
                new JsonArticleReportResponse(
                        articleIdToFind.toString(),
                        "Found Title",
                        "Desc",
                        "Cat",
                        "Link",
                        Collections.emptyList(),
                        LocalDate.now());

        when(articleTypeRepository.findByArticleType(articleType)).thenReturn(articleTypeEntities);

        doReturn(Optional.of(foundResponse))
                .when(dbArticlesService)
                .getArticleById(articleIdToFind);
        doReturn(Optional.empty()).when(dbArticlesService).getArticleById(articleIdToMiss);

        List<JsonArticleReportResponse> result = dbArticlesService.getArticlesByType(articleType);

        verify(dbArticlesService, times(1)).getArticleById(articleIdToFind);
        verify(dbArticlesService, times(1)).getArticleById(articleIdToMiss);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(articleIdToFind.toString(), result.get(0).getArticleId());
    }

    @Test
    void testGetAllArticleTypesWithArticles_successWithFiltering() {
        int days = 7;
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tenDaysAgo = today.minusDays(10);

        UUID idA1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID idA2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID idB1 = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID idOld = UUID.fromString("44444444-4444-4444-4444-444444444444");

        String typeA = "TYPE_A";
        String typeB = "TYPE_B";

        List<ArticleTypeEntity> allTypes =
                List.of(
                        new ArticleTypeEntity(typeA, idA1),
                        new ArticleTypeEntity(typeA, idA2),
                        new ArticleTypeEntity(typeB, idB1),
                        new ArticleTypeEntity(typeB, idOld));

        JsonArticleReportResponse respA1 =
                new JsonArticleReportResponse(
                        idA1.toString(), "A1", "D", "C", "L", Collections.emptyList(), yesterday);
        JsonArticleReportResponse respA2 =
                new JsonArticleReportResponse(
                        idA2.toString(), "A2", "D", "C", "L", Collections.emptyList(), today);
        JsonArticleReportResponse respB1 =
                new JsonArticleReportResponse(
                        idB1.toString(), "B1", "D", "C", "L", Collections.emptyList(), yesterday);
        JsonArticleReportResponse respOld =
                new JsonArticleReportResponse(
                        idOld.toString(),
                        "OLD",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        tenDaysAgo);

        when(articleTypeRepository.findAll()).thenReturn(allTypes);

        doReturn(List.of(respA1, respA2, respOld)).when(dbArticlesService).getArticlesByType(typeA);
        doReturn(List.of(respB1, respOld)).when(dbArticlesService).getArticlesByType(typeB);

        Map<String, List<JsonArticleReportResponse>> result =
                dbArticlesService.getAllArticleTypesWithArticles(days);

        verify(articleTypeRepository, times(1)).findAll();
        verify(dbArticlesService, times(1)).getArticlesByType(typeA);
        verify(dbArticlesService, times(1)).getArticlesByType(typeB);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(typeA));
        assertTrue(result.containsKey(typeB));

        List<JsonArticleReportResponse> listA = result.get(typeA);
        assertEquals(2, listA.size());
        assertEquals(idA2.toString(), listA.get(0).getArticleId());
        assertEquals(idA1.toString(), listA.get(1).getArticleId());

        List<JsonArticleReportResponse> listB = result.get(typeB);
        assertEquals(1, listB.size());
        assertEquals(idB1.toString(), listB.get(0).getArticleId());
    }

    @Test
    void testGetAllArticleTypesWithArticles_noTypesFound_returnsEmptyMap() {
        int days = 7;

        when(articleTypeRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, List<JsonArticleReportResponse>> result =
                dbArticlesService.getAllArticleTypesWithArticles(days);

        verify(articleTypeRepository, times(1)).findAll();
        verify(dbArticlesService, never()).getArticlesByType(anyString());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllArticleTypesWithArticles_allArticlesTooOld_returnsMapWithEmptyList() {
        int days = 7;
        String typeC = "TYPE_C";

        List<ArticleTypeEntity> allTypes = List.of(new ArticleTypeEntity(typeC, UUID.randomUUID()));

        JsonArticleReportResponse respOld =
                new JsonArticleReportResponse(
                        UUID.randomUUID().toString(),
                        "Old",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        LocalDate.now().minusDays(10));

        when(articleTypeRepository.findAll()).thenReturn(allTypes);

        doReturn(List.of(respOld)).when(dbArticlesService).getArticlesByType(typeC);

        Map<String, List<JsonArticleReportResponse>> result =
                dbArticlesService.getAllArticleTypesWithArticles(days);

        verify(dbArticlesService, times(1)).getArticlesByType(typeC);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(typeC));

        List<JsonArticleReportResponse> listC = result.get(typeC);
        assertNotNull(listC);
        assertTrue(listC.isEmpty());
    }

    @Test
    void testIncrementViewCount_articleExistsInMonthlyTable_incrementsViewCount() {
        UUID articleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        int initialViewCount = 5;

        JsonArticleReportResponse articleResponse =
                new JsonArticleReportResponse(
                        articleId.toString(),
                        "Title",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        LocalDate.now());
        doReturn(Optional.of(articleResponse)).when(dbArticlesService).getArticleById(articleId);

        MonthlyArticlesEntity existingEntity = new MonthlyArticlesEntity();
        existingEntity.setArticleId(articleId);
        existingEntity.setViewCount(initialViewCount);

        when(monthlyArticlesRepository.findByArticleId(articleId))
                .thenReturn(Optional.of(existingEntity));
        when(monthlyArticlesRepository.save(existingEntity))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<MonthlyArticlesEntity> result = dbArticlesService.incrementViewCount(articleId);

        assertTrue(result.isPresent());
        MonthlyArticlesEntity updatedEntity = result.get();
        assertEquals(articleId, updatedEntity.getArticleId());
        assertEquals(initialViewCount + 1, updatedEntity.getViewCount());

        verify(dbArticlesService, times(1)).getArticleById(articleId);
        verify(monthlyArticlesRepository, times(1)).findByArticleId(articleId);
        verify(monthlyArticlesRepository, times(1)).save(existingEntity);
        verify(monthlyArticlesRepository, never())
                .save(argThat(entity -> entity.getViewCount() == 0));
    }

    @Test
    void testIncrementViewCount_articleNotYetInMonthlyTable_createsNewEntry() {
        UUID articleId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        LocalDate datePublished = LocalDate.of(2025, 10, 26);
        String title = "New Article Title";

        JsonArticleReportResponse articleResponse =
                new JsonArticleReportResponse(
                        articleId.toString(),
                        title,
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        datePublished);
        doReturn(Optional.of(articleResponse)).when(dbArticlesService).getArticleById(articleId);

        when(monthlyArticlesRepository.findByArticleId(articleId)).thenReturn(Optional.empty());

        when(monthlyArticlesRepository.save(any(MonthlyArticlesEntity.class)))
                .thenAnswer(
                        invocation -> {
                            MonthlyArticlesEntity entity = invocation.getArgument(0);
                            return entity;
                        });

        Optional<MonthlyArticlesEntity> result = dbArticlesService.incrementViewCount(articleId);

        assertTrue(result.isPresent());
        MonthlyArticlesEntity newEntity = result.get();

        assertEquals(articleId, newEntity.getArticleId());
        assertEquals(datePublished, newEntity.getDatePublished());
        assertEquals(title, newEntity.getTitle());
        assertEquals(1, newEntity.getViewCount());

        verify(monthlyArticlesRepository, times(1)).findByArticleId(articleId);
        verify(monthlyArticlesRepository, times(2)).save(any(MonthlyArticlesEntity.class));
    }

    @Test
    void testIncrementViewCount_articleNotFoundInMainTable_returnsEmpty() {
        UUID articleId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        doReturn(Optional.empty()).when(dbArticlesService).getArticleById(articleId);

        Optional<MonthlyArticlesEntity> result = dbArticlesService.incrementViewCount(articleId);

        assertTrue(result.isEmpty());

        verify(monthlyArticlesRepository, never()).findByArticleId(any(UUID.class));
        verify(monthlyArticlesRepository, never()).save(any(MonthlyArticlesEntity.class));
    }

    @Test
    void testToggleArticleOfNote_articleExistsInMonthlyTable_togglesState() {
        UUID articleId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        JsonArticleReportResponse articleResponse =
                new JsonArticleReportResponse(
                        articleId.toString(),
                        "Title",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        LocalDate.now());
        doReturn(Optional.of(articleResponse)).when(dbArticlesService).getArticleById(articleId);

        MonthlyArticlesEntity existingEntity = new MonthlyArticlesEntity();
        existingEntity.setArticleId(articleId);
        existingEntity.setArticleOfNote(false); // Start as false

        when(monthlyArticlesRepository.findByArticleId(articleId))
                .thenReturn(Optional.of(existingEntity));
        when(monthlyArticlesRepository.save(existingEntity))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<MonthlyArticlesEntity> result = dbArticlesService.toggleArticleOfNote(articleId);

        assertTrue(result.isPresent());
        assertTrue(result.get().isArticleOfNote()); // Should be true now

        verify(dbArticlesService, times(1)).getArticleById(articleId);
        verify(monthlyArticlesRepository, times(1)).findByArticleId(articleId);
        verify(monthlyArticlesRepository, times(1)).save(existingEntity);
    }

    @Test
    void testToggleArticleOfNote_articleNotYetInMonthlyTable_createsAndSetsTrue() {
        UUID articleId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        LocalDate datePublished = LocalDate.of(2025, 10, 26);
        String title = "New Article Title";

        JsonArticleReportResponse articleResponse =
                new JsonArticleReportResponse(
                        articleId.toString(),
                        title,
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        datePublished);
        doReturn(Optional.of(articleResponse)).when(dbArticlesService).getArticleById(articleId);

        when(monthlyArticlesRepository.findByArticleId(articleId)).thenReturn(Optional.empty());

        when(monthlyArticlesRepository.save(any(MonthlyArticlesEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<MonthlyArticlesEntity> result = dbArticlesService.toggleArticleOfNote(articleId);

        assertTrue(result.isPresent());
        MonthlyArticlesEntity newEntity = result.get();

        assertEquals(articleId, newEntity.getArticleId());
        assertTrue(newEntity.isArticleOfNote()); // Should be toggled to true

        verify(monthlyArticlesRepository, times(1)).findByArticleId(articleId);
        verify(monthlyArticlesRepository, times(2)).save(any(MonthlyArticlesEntity.class));
    }

    @Test
    void testToggleArticleOfNote_articleNotFoundInMainTable_returnsEmpty() {
        UUID articleId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        doReturn(Optional.empty()).when(dbArticlesService).getArticleById(articleId);

        Optional<MonthlyArticlesEntity> result = dbArticlesService.toggleArticleOfNote(articleId);

        assertTrue(result.isEmpty());

        verify(monthlyArticlesRepository, never()).findByArticleId(any(UUID.class));
        verify(monthlyArticlesRepository, never()).save(any(MonthlyArticlesEntity.class));
    }

    @Test
    void testGetTop10Articles_success_returnsSortedDTOs() {
        UUID id1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
        UUID id2 = UUID.fromString("10000000-0000-0000-0000-000000000002");

        MonthlyArticlesEntity entity1 = new MonthlyArticlesEntity();
        entity1.setArticleId(id1);
        entity1.setViewCount(100);

        MonthlyArticlesEntity entity2 = new MonthlyArticlesEntity();
        entity2.setArticleId(id2);
        entity2.setViewCount(50);

        List<MonthlyArticlesEntity> topArticles = List.of(entity1, entity2);

        JsonArticleReportResponse resp1 =
                new JsonArticleReportResponse(
                        id1.toString(),
                        "Title 1",
                        "D",
                        "C",
                        "link1",
                        Collections.emptyList(),
                        LocalDate.now());
        JsonArticleReportResponse resp2 =
                new JsonArticleReportResponse(
                        id2.toString(),
                        "Title 2",
                        "D",
                        "C",
                        "link2",
                        Collections.emptyList(),
                        LocalDate.now());

        when(monthlyArticlesRepository.findTop10ByOrderByViewCountDesc()).thenReturn(topArticles);
        doReturn(Optional.of(resp1)).when(dbArticlesService).getArticleById(id1);
        doReturn(Optional.of(resp2)).when(dbArticlesService).getArticleById(id2);

        List<MonthlyArticleDTO> result = dbArticlesService.getTop10Articles();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("link1", result.get(0).getUrl());
        assertEquals(100, result.get(0).getViewCount().orElse(0));
        assertEquals("link2", result.get(1).getUrl());
        assertEquals(50, result.get(1).getViewCount().orElse(0));

        verify(monthlyArticlesRepository, times(1)).findTop10ByOrderByViewCountDesc();
        verify(dbArticlesService, times(1)).getArticleById(id1);
        verify(dbArticlesService, times(1)).getArticleById(id2);
    }

    @Test
    void testGetTop10Articles_someArticlesMissing_returnsFewerDTOs() {
        UUID id1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
        UUID id2 = UUID.fromString("10000000-0000-0000-0000-000000000002");

        MonthlyArticlesEntity entity1 = new MonthlyArticlesEntity();
        entity1.setArticleId(id1);
        entity1.setViewCount(100);

        MonthlyArticlesEntity entity2 = new MonthlyArticlesEntity();
        entity2.setArticleId(id2);
        entity2.setViewCount(50);

        List<MonthlyArticlesEntity> topArticles = List.of(entity1, entity2);

        JsonArticleReportResponse resp1 =
                new JsonArticleReportResponse(
                        id1.toString(),
                        "Title 1",
                        "D",
                        "C",
                        "link1",
                        Collections.emptyList(),
                        LocalDate.now());

        when(monthlyArticlesRepository.findTop10ByOrderByViewCountDesc()).thenReturn(topArticles);
        doReturn(Optional.of(resp1)).when(dbArticlesService).getArticleById(id1);
        doReturn(Optional.empty()).when(dbArticlesService).getArticleById(id2); // ID2 is missing

        List<MonthlyArticleDTO> result = dbArticlesService.getTop10Articles();

        assertNotNull(result);
        assertEquals(1, result.size()); // Only ID1 should be present
        assertEquals(id1.toString(), result.get(0).getArticleId().toString());

        verify(monthlyArticlesRepository, times(1)).findTop10ByOrderByViewCountDesc();
    }

    @Test
    void testGetArticlesOfNote_success_returnsResponses() {
        UUID id1 = UUID.fromString("30000000-0000-0000-0000-000000000001");
        UUID id2 = UUID.fromString("30000000-0000-0000-0000-000000000002");

        MonthlyArticlesEntity entity1 = new MonthlyArticlesEntity();
        entity1.setArticleId(id1);

        MonthlyArticlesEntity entity2 = new MonthlyArticlesEntity();
        entity2.setArticleId(id2);

        List<MonthlyArticlesEntity> articlesOfNote = List.of(entity1, entity2);

        JsonArticleReportResponse resp1 =
                new JsonArticleReportResponse(
                        id1.toString(),
                        "Title 1",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        LocalDate.now());
        JsonArticleReportResponse resp2 =
                new JsonArticleReportResponse(
                        id2.toString(),
                        "Title 2",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        LocalDate.now());

        when(monthlyArticlesRepository.findByIsArticleOfNoteTrue()).thenReturn(articlesOfNote);
        doReturn(Optional.of(resp1)).when(dbArticlesService).getArticleById(id1);
        doReturn(Optional.of(resp2)).when(dbArticlesService).getArticleById(id2);

        List<JsonArticleReportResponse> result = dbArticlesService.getArticlesOfNote();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(id1.toString(), result.get(0).getArticleId());

        verify(monthlyArticlesRepository, times(1)).findByIsArticleOfNoteTrue();
        verify(dbArticlesService, times(1)).getArticleById(id1);
    }

    @Test
    void testGetArticlesOfNote_noArticles_returnsEmptyList() {
        when(monthlyArticlesRepository.findByIsArticleOfNoteTrue())
                .thenReturn(Collections.emptyList());

        List<JsonArticleReportResponse> result = dbArticlesService.getArticlesOfNote();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(monthlyArticlesRepository, times(1)).findByIsArticleOfNoteTrue();
        verify(dbArticlesService, never()).getArticleById(any(UUID.class));
    }

    @Test
    void testAddFavourite_success_savesNewEntry() {
        Long userId = 1L;
        UUID articleId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(articlesRepository.existsById(articleId)).thenReturn(true);
        when(userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId))
                .thenReturn(false);
        when(userFavouriteRepository.save(any(UserFavouriteEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = dbArticlesService.addFavourite(userId, articleId);

        assertTrue(result);

        verify(userFavouriteRepository, times(1))
                .save(
                        argThat(
                                fav ->
                                        fav.getUserId().equals(userId)
                                                && fav.getArticleId().equals(articleId)));
        verify(articlesRepository, times(1)).existsById(articleId);
    }

    @Test
    void testAddFavourite_articleNotFound_returnsFalse() {
        Long userId = 1L;
        UUID articleId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        when(articlesRepository.existsById(articleId)).thenReturn(false);

        boolean result = dbArticlesService.addFavourite(userId, articleId);

        assertFalse(result);

        verify(articlesRepository, times(1)).existsById(articleId);
        verify(userFavouriteRepository, never())
                .existsByUserIdAndArticleId(anyLong(), any(UUID.class));
        verify(userFavouriteRepository, never()).save(any(UserFavouriteEntity.class));
    }

    @Test
    void testAddFavourite_duplicateEntry_returnsTrueWithoutSaving() {
        Long userId = 1L;
        UUID articleId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(articlesRepository.existsById(articleId)).thenReturn(true);
        when(userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId))
                .thenReturn(true);

        boolean result = dbArticlesService.addFavourite(userId, articleId);

        assertTrue(result);

        verify(userFavouriteRepository, times(1)).existsByUserIdAndArticleId(userId, articleId);
        verify(userFavouriteRepository, never()).save(any(UserFavouriteEntity.class));
    }

    @Test
    void testRemoveFavourite_exists_deletesEntry() {
        Long userId = 1L;
        UUID articleId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        when(userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId))
                .thenReturn(true);

        dbArticlesService.removeFavourite(userId, articleId);

        verify(userFavouriteRepository, times(1)).existsByUserIdAndArticleId(userId, articleId);
        verify(userFavouriteRepository, times(1)).deleteByUserIdAndArticleId(userId, articleId);
    }

    @Test
    void testRemoveFavourite_doesNotExist_doesNothing() {
        Long userId = 1L;
        UUID articleId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        when(userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId))
                .thenReturn(false);

        dbArticlesService.removeFavourite(userId, articleId);

        verify(userFavouriteRepository, times(1)).existsByUserIdAndArticleId(userId, articleId);
        verify(userFavouriteRepository, never())
                .deleteByUserIdAndArticleId(anyLong(), any(UUID.class));
    }

    @Test
    void testGetFavouritesForUser_noFavourites_returnsEmptyList() {
        Long userId = 1L;
        when(userFavouriteRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<JsonArticleReportResponse> result = dbArticlesService.getFavouritesForUser(userId);

        assertTrue(result.isEmpty());
        verify(userFavouriteRepository, times(1)).findByUserId(userId);
        verify(dbArticlesService, never()).getArticleById(any(UUID.class));
    }

    @Test
    void testGetFavouritesForUser_withFavourites_returnsMatchingArticles() {
        Long userId = 1L;
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        UserFavouriteEntity fav1 = new UserFavouriteEntity();
        fav1.setArticleId(id1);
        UserFavouriteEntity fav2 = new UserFavouriteEntity();
        fav2.setArticleId(id2);
        List<UserFavouriteEntity> favourites = Arrays.asList(fav1, fav2);

        JsonArticleReportResponse resp1 =
                new JsonArticleReportResponse(
                        id1.toString(),
                        "Title 1",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        LocalDate.now());
        JsonArticleReportResponse resp2 =
                new JsonArticleReportResponse(
                        id2.toString(),
                        "Title 2",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        LocalDate.now());

        when(userFavouriteRepository.findByUserId(userId)).thenReturn(favourites);
        doReturn(Optional.of(resp1)).when(dbArticlesService).getArticleById(id1);
        doReturn(Optional.of(resp2)).when(dbArticlesService).getArticleById(id2);

        List<JsonArticleReportResponse> result = dbArticlesService.getFavouritesForUser(userId);

        assertEquals(2, result.size());
        assertEquals(id1.toString(), result.get(0).getArticleId());
        assertEquals(id2.toString(), result.get(1).getArticleId());
        verify(userFavouriteRepository, times(1)).findByUserId(userId);
        verify(dbArticlesService, times(1)).getArticleById(id1);
        verify(dbArticlesService, times(1)).getArticleById(id2);
    }

    @Test
    void testGetFavouritesForUser_skipsMissingArticles() {
        Long userId = 1L;
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID id2 = UUID.fromString("99999999-9999-9999-9999-999999999999"); // Missing article

        UserFavouriteEntity fav1 = new UserFavouriteEntity();
        fav1.setArticleId(id1);
        UserFavouriteEntity fav2 = new UserFavouriteEntity();
        fav2.setArticleId(id2);
        List<UserFavouriteEntity> favourites = Arrays.asList(fav1, fav2);

        JsonArticleReportResponse resp1 =
                new JsonArticleReportResponse(
                        id1.toString(),
                        "Title 1",
                        "D",
                        "C",
                        "L",
                        Collections.emptyList(),
                        LocalDate.now());

        when(userFavouriteRepository.findByUserId(userId)).thenReturn(favourites);
        doReturn(Optional.of(resp1)).when(dbArticlesService).getArticleById(id1);
        doReturn(Optional.empty()).when(dbArticlesService).getArticleById(id2);

        List<JsonArticleReportResponse> result = dbArticlesService.getFavouritesForUser(userId);

        assertEquals(1, result.size());
        assertEquals(id1.toString(), result.get(0).getArticleId());
        verify(userFavouriteRepository, times(1)).findByUserId(userId);
        verify(dbArticlesService, times(1)).getArticleById(id1);
        verify(dbArticlesService, times(1)).getArticleById(id2);
    }

    @Test
    void testIngestFromUrl_success_savesNewArticle() {
        String link = "http://test.com/new";
        String title = "New Title";
        String description = "A new description.";

        when(dbArticlesService.getArticleByLink(link)).thenReturn(Optional.empty());

        boolean result = dbArticlesService.ingestFromUrl(link, title, description);

        assertTrue(result);

        verify(dbArticlesService, times(1)).getArticleByLink(link);
        verify(dbArticlesService, times(1))
                .addNewArticle(
                        any(UUID.class), eq(title), eq(link), eq(description), any(Instant.class));
    }

    @Test
    void testIngestFromUrl_nullLink_returnsFalse() {
        String link = null;

        boolean result = dbArticlesService.ingestFromUrl(link, "Title", "Desc");

        assertFalse(result);

        verify(dbArticlesService, never()).getArticleByLink(anyString());
        verify(dbArticlesService, never()).addNewArticle(any(), any(), any(), any(), any());
    }

    @Test
    void testIngestFromUrl_blankLink_returnsFalse() {
        String link = "   ";

        boolean result = dbArticlesService.ingestFromUrl(link, "Title", "Desc");

        assertFalse(result);

        verify(dbArticlesService, never()).getArticleByLink(anyString());
        verify(dbArticlesService, never()).addNewArticle(any(), any(), any(), any(), any());
    }

    @Test
    void testIngestFromUrl_articleAlreadyExists_returnsFalse() {
        String link = "http://test.com/existing";

        JsonArticleReportResponse existing = new JsonArticleReportResponse();
        when(dbArticlesService.getArticleByLink(link)).thenReturn(Optional.of(existing));

        boolean result = dbArticlesService.ingestFromUrl(link, "Title", "Desc");

        assertFalse(result);

        verify(dbArticlesService, times(1)).getArticleByLink(link);
        verify(dbArticlesService, never()).addNewArticle(any(), any(), any(), any(), any());
    }

    @Test
    void testIngestFromUrl_nullTitle_usesDefault() {
        String link = "http://test.com/new";
        String title = null;

        when(dbArticlesService.getArticleByLink(link)).thenReturn(Optional.empty());

        dbArticlesService.ingestFromUrl(link, title, "Desc");

        verify(dbArticlesService, times(1))
                .addNewArticle(
                        any(UUID.class),
                        eq("Untitled Article"),
                        eq(link),
                        eq("Desc"),
                        any(Instant.class));
    }

    @Test
    void testIngestFromUrl_blankDescription_usesDefault() {
        String link = "http://test.com/new";
        String description = "";

        when(dbArticlesService.getArticleByLink(link)).thenReturn(Optional.empty());

        dbArticlesService.ingestFromUrl(link, "Title", description);

        verify(dbArticlesService, times(1))
                .addNewArticle(
                        any(UUID.class),
                        eq("Title"),
                        eq(link),
                        eq("No description provided."),
                        any(Instant.class));
    }

    @Test
    void testIngestFromUrl_exceptionDuringSave_throwsRuntimeException() {
        String link = "http://test.com/fail";
        String title = "Title";

        when(dbArticlesService.getArticleByLink(link)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("DB Save Error"))
                .when(dbArticlesService)
                .addNewArticle(any(), any(), any(), any(), any());

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> {
                            dbArticlesService.ingestFromUrl(link, title, "Desc");
                        });

        assertTrue(thrown.getMessage().contains("Error ingesting article"));
        verify(dbArticlesService, times(1)).addNewArticle(any(), any(), any(), any(), any());
    }
}
