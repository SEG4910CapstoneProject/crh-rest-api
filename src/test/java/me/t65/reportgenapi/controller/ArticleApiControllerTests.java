package me.t65.reportgenapi.controller;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.controller.payload.ArticleByLinkRequest;
import me.t65.reportgenapi.controller.payload.ArticleIngestRequest;
import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.UidResponse;
import me.t65.reportgenapi.db.postgres.dto.MonthlyArticleDTO;
import me.t65.reportgenapi.db.postgres.entities.MonthlyArticlesEntity;
import me.t65.reportgenapi.db.services.DbArticlesService;
import me.t65.reportgenapi.utils.IdGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class ArticleApiControllerTests {

    private final String CUSTOM_ID_1 = "53d890f0-c160-4da4-9056-cb71d4a0e7fb";
    private final UUID CUSTOM_UID_1 = UUID.fromString(CUSTOM_ID_1);

    @Mock private DbArticlesService dbArticlesService;
    @Mock private IdGenerator idGenerator;

    private ArticleApiController articleApiController;

    @BeforeEach
    public void beforeEach() {
        articleApiController = new ArticleApiController(dbArticlesService, idGenerator);
    }

    // --- Utility Methods for Mocking (Cleaned) ---

    private JsonArticleReportResponse createMockResponse(UUID id) {
        return new JsonArticleReportResponse(
                id.toString(),
                "Title",
                "Description",
                "Category",
                "Link",
                List.of(),
                LocalDate.now());
    }

    private MonthlyArticleDTO createMockDTO(UUID id) {
        return new MonthlyArticleDTO(id.toString(), Optional.of(10), "DTO Title", id);
    }

    private MonthlyArticlesEntity createMockEntity(UUID id) {
        MonthlyArticlesEntity entity = new MonthlyArticlesEntity();
        entity.setArticleId(id);
        return entity;
    }

    @Test
    public void testGetArticle_success() {
        JsonArticleReportResponse expectedResponse =
                new JsonArticleReportResponse(
                        CUSTOM_ID_1,
                        "someTitle",
                        "someDescription",
                        "someCategory",
                        "someLink",
                        List.of(),
                        LocalDate.of(2020, 1, 1));

        when(dbArticlesService.getArticleById(eq(CUSTOM_UID_1)))
                .thenReturn(Optional.of(expectedResponse));

        ResponseEntity<?> actual = articleApiController.getArticle(CUSTOM_ID_1);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expectedResponse, actual.getBody());
    }

    @Test
    public void testGetArticle_failedToGet_notFound() {
        when(dbArticlesService.getArticleById(eq(CUSTOM_UID_1))).thenReturn(Optional.empty());

        ResponseEntity<?> actual = articleApiController.getArticle(CUSTOM_ID_1);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testEditArticleInReport_success() {
        when(dbArticlesService.editArticleInReport(any(), any(), any(), any(), any()))
                .thenReturn(true);

        ResponseEntity<?> actual =
                articleApiController.editArticle(
                        "someArticle",
                        "someTitle",
                        " someLink",
                        "someDescription",
                        LocalDate.of(2024, 1, 1));

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testEditArticleInReport_fail_NotFound() {
        when(dbArticlesService.editArticleInReport(any(), any(), any(), any(), any()))
                .thenReturn(false);

        ResponseEntity<?> actual =
                articleApiController.editArticle(
                        "someArticle",
                        "someTitle",
                        " someLink",
                        "someDescription",
                        LocalDate.of(2024, 1, 1));

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testAddArticle_success() {
        String expectedTitle = "someTitle";
        String expectedLink = "someLink";
        String expectedDescription = "someDescription";
        LocalDate expectedLocalDate = LocalDate.of(2020, 1, 1);

        when(idGenerator.generateId()).thenReturn(CUSTOM_UID_1);

        ResponseEntity<?> actual =
                articleApiController.addArticle(
                        expectedTitle, expectedLink, expectedDescription, expectedLocalDate);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(new UidResponse(CUSTOM_ID_1), actual.getBody());
    }

    @Test
    public void testGetArticleByLink_success() {
        String expectedLink = "someLink";
        JsonArticleReportResponse expectedJsonArticleReportResponse =
                new JsonArticleReportResponse(
                        CUSTOM_ID_1,
                        "someTitle",
                        "someDescription",
                        "someCategory",
                        "someLink",
                        List.of(),
                        LocalDate.of(2020, 1, 1));

        when(dbArticlesService.getArticleByLink(eq(expectedLink)))
                .thenReturn(Optional.of(expectedJsonArticleReportResponse));

        ResponseEntity<?> actual =
                articleApiController.getArticleByLink(new ArticleByLinkRequest(expectedLink));

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expectedJsonArticleReportResponse, actual.getBody());
    }

    @Test
    public void testGetArticleByLink_notFound() {
        when(dbArticlesService.getArticleByLink(any())).thenReturn(Optional.empty());

        ResponseEntity<?> actual =
                articleApiController.getArticleByLink(new ArticleByLinkRequest("someLink"));

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testGetArticlesByType_success() {
        String type = "TEST_TYPE";
        List<JsonArticleReportResponse> articles = Arrays.asList(createMockResponse(CUSTOM_UID_1));
        when(dbArticlesService.getArticlesByType(type)).thenReturn(articles);

        ResponseEntity<List<JsonArticleReportResponse>> actual =
                articleApiController.getArticlesByType(type);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(articles.size(), actual.getBody().size());
        verify(dbArticlesService, times(1)).getArticlesByType(type);
    }

    @Test
    public void testGetArticlesByType_noContent() {
        String type = "EMPTY_TYPE";
        when(dbArticlesService.getArticlesByType(type)).thenReturn(Collections.emptyList());

        ResponseEntity<List<JsonArticleReportResponse>> actual =
                articleApiController.getArticlesByType(type);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        verify(dbArticlesService, times(1)).getArticlesByType(type);
    }

    @Test
    public void testGetAllArticleTypesWithArticles_success() {
        int days = 30;
        Map<String, List<JsonArticleReportResponse>> mockMap =
                Map.of("A", List.of(createMockResponse(CUSTOM_UID_1)));
        when(dbArticlesService.getAllArticleTypesWithArticles(days)).thenReturn(mockMap);

        ResponseEntity<Map<String, List<JsonArticleReportResponse>>> actual =
                articleApiController.getAllArticleTypesWithArticles(days);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertTrue(actual.getBody().containsKey("A"));
        verify(dbArticlesService, times(1)).getAllArticleTypesWithArticles(days);
    }

    @Test
    public void testIncrementViewCount_success() {
        MonthlyArticlesEntity entity = createMockEntity(CUSTOM_UID_1);
        when(dbArticlesService.incrementViewCount(CUSTOM_UID_1)).thenReturn(Optional.of(entity));

        ResponseEntity<?> actual = articleApiController.incrementViewCount(CUSTOM_UID_1);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        verify(dbArticlesService, times(1)).incrementViewCount(CUSTOM_UID_1);
    }

    @Test
    public void testIncrementViewCount_notFound() {
        when(dbArticlesService.incrementViewCount(CUSTOM_UID_1)).thenReturn(Optional.empty());

        ResponseEntity<?> actual = articleApiController.incrementViewCount(CUSTOM_UID_1);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        verify(dbArticlesService, times(1)).incrementViewCount(CUSTOM_UID_1);
    }

    @Test
    public void testToggleArticleOfNote_success() {
        MonthlyArticlesEntity entity = createMockEntity(CUSTOM_UID_1);
        when(dbArticlesService.toggleArticleOfNote(CUSTOM_UID_1)).thenReturn(Optional.of(entity));

        ResponseEntity<?> actual = articleApiController.toggleArticleOfNote(CUSTOM_UID_1);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        verify(dbArticlesService, times(1)).toggleArticleOfNote(CUSTOM_UID_1);
    }

    @Test
    public void testToggleArticleOfNote_notFound() {
        when(dbArticlesService.toggleArticleOfNote(CUSTOM_UID_1)).thenReturn(Optional.empty());

        ResponseEntity<?> actual = articleApiController.toggleArticleOfNote(CUSTOM_UID_1);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        verify(dbArticlesService, times(1)).toggleArticleOfNote(CUSTOM_UID_1);
    }

    @Test
    public void testGetTop10Articles_success() {
        List<MonthlyArticleDTO> articles = Arrays.asList(createMockDTO(CUSTOM_UID_1));
        when(dbArticlesService.getTop10Articles()).thenReturn(articles);

        ResponseEntity<List<MonthlyArticleDTO>> actual = articleApiController.getTop10Articles();

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(articles.size(), actual.getBody().size());
        verify(dbArticlesService, times(1)).getTop10Articles();
    }

    @Test
    public void testGetTop10Articles_noContent() {
        when(dbArticlesService.getTop10Articles()).thenReturn(Collections.emptyList());

        ResponseEntity<List<MonthlyArticleDTO>> actual = articleApiController.getTop10Articles();

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        verify(dbArticlesService, times(1)).getTop10Articles();
    }

    @Test
    public void testGetArticlesOfNote_success() {
        List<JsonArticleReportResponse> articles = Arrays.asList(createMockResponse(CUSTOM_UID_1));
        when(dbArticlesService.getArticlesOfNote()).thenReturn(articles);

        ResponseEntity<List<JsonArticleReportResponse>> actual =
                articleApiController.getArticlesOfNote();

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(articles.size(), actual.getBody().size());
        verify(dbArticlesService, times(1)).getArticlesOfNote();
    }

    @Test
    public void testGetArticlesOfNote_noContent() {
        when(dbArticlesService.getArticlesOfNote()).thenReturn(Collections.emptyList());

        ResponseEntity<List<JsonArticleReportResponse>> actual =
                articleApiController.getArticlesOfNote();

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        verify(dbArticlesService, times(1)).getArticlesOfNote();
    }

    @Test
    public void testGetManualArticles_success() {
        List<JsonArticleReportResponse> articles = Arrays.asList(createMockResponse(CUSTOM_UID_1));
        when(dbArticlesService.getManualArticles()).thenReturn(articles);

        ResponseEntity<List<JsonArticleReportResponse>> actual =
                articleApiController.getManualArticles();

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(articles.size(), actual.getBody().size());
        verify(dbArticlesService, times(1)).getManualArticles();
    }

    @Test
    public void testGetManualArticles_noContent() {
        when(dbArticlesService.getManualArticles()).thenReturn(Collections.emptyList());

        ResponseEntity<List<JsonArticleReportResponse>> actual =
                articleApiController.getManualArticles();

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        verify(dbArticlesService, times(1)).getManualArticles();
    }

    @Test
    public void testIngestArticle_success() {
        ArticleIngestRequest request = new ArticleIngestRequest();
        request.setLink("http://valid.com/article");
        request.setTitle("Valid Title");
        request.setDescription("Description");

        when(dbArticlesService.getArticleByLink(request.getLink())).thenReturn(Optional.empty());
        doNothing()
                .when(dbArticlesService)
                .addNewArticle(any(), anyString(), anyString(), anyString(), any());

        ResponseEntity<?> actual = articleApiController.ingestArticle(request, "Bearer validtoken");

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        verify(dbArticlesService, times(1))
                .addNewArticle(any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testIngestArticle_conflict_alreadyExists() {
        ArticleIngestRequest request = new ArticleIngestRequest();
        request.setLink("http://existing.com/article");
        request.setTitle("Existing Title");

        when(dbArticlesService.getArticleByLink(request.getLink()))
                .thenReturn(Optional.of(mock(JsonArticleReportResponse.class)));

        ResponseEntity<?> actual = articleApiController.ingestArticle(request, "Bearer token");

        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
        verify(dbArticlesService, never())
                .addNewArticle(any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testIngestArticle_badRequest_missingLink() {
        ArticleIngestRequest request = new ArticleIngestRequest();
        request.setLink("");
        request.setTitle("Valid Title");

        ResponseEntity<?> actual = articleApiController.ingestArticle(request, "Bearer token");

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        verify(dbArticlesService, never())
                .addNewArticle(any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testIngestArticle_badRequest_missingTitle() {
        ArticleIngestRequest request = new ArticleIngestRequest();
        request.setLink("http://link.com");
        request.setTitle("");

        ResponseEntity<?> actual = articleApiController.ingestArticle(request, "Bearer token");

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        verify(dbArticlesService, never())
                .addNewArticle(any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testIngestArticle_badRequest_invalidUrlFormat() {
        // Since we cannot easily mock the `new URL()` call inside the controller method,
        // we rely on the implementation to throw MalformedURLException for a clearly bad format.
        // We simulate a request that will fail the URL constructor check.
        ArticleIngestRequest request = new ArticleIngestRequest();
        request.setLink("invalid-link-format");
        request.setTitle("Valid Title");

        // Since the controller catches the MalformedURLException internally,
        // we only assert the HTTP response.
        ResponseEntity<?> actual = articleApiController.ingestArticle(request, "Bearer token");

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        verify(dbArticlesService, never())
                .addNewArticle(any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testIngestArticle_internalServerError() {
        ArticleIngestRequest request = new ArticleIngestRequest();
        request.setLink("http://valid.com/error");
        request.setTitle("Error Title");
        request.setDescription(null); // can be null

        when(dbArticlesService.getArticleByLink(request.getLink())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Database error"))
                .when(dbArticlesService)
                .addNewArticle(any(), anyString(), anyString(), nullable(String.class), any());

        ResponseEntity<?> actual = articleApiController.ingestArticle(request, "Bearer token");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
        verify(dbArticlesService, times(1))
                .addNewArticle(any(), anyString(), anyString(), nullable(String.class), any());
    }
}