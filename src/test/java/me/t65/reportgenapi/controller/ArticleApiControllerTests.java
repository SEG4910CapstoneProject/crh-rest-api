package me.t65.reportgenapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.controller.payload.ArticleByLinkRequest;
import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.UidResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
}
