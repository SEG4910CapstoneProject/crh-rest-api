package me.t65.reportgenapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import me.t65.reportgenapi.controller.payload.*;
import me.t65.reportgenapi.db.services.DbArticlesService;
import me.t65.reportgenapi.db.services.DbReportService;
import me.t65.reportgenapi.db.services.DbStatsService;
import me.t65.reportgenapi.reportformatter.RawReport;
import me.t65.reportgenapi.reportformatter.ReportFormatter;

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
public class ReportApiControllerTests {

    private final String CUSTOM_ID_1 = "53d890f0-c160-4da4-9056-cb71d4a0e7fb";

    @Mock private DbReportService dbReportService;
    @Mock private DbArticlesService dbArticlesService;
    @Mock private DbStatsService dbStatsService;

    private Map<String, ReportFormatter> reportFormatterMap;

    private ReportApiController reportApiController;

    @BeforeEach
    public void beforeEach() {
        reportFormatterMap = new HashMap<>();
        reportApiController =
                new ReportApiController(
                        dbReportService, dbStatsService, dbArticlesService, reportFormatterMap);
    }

    // @Test
    // public void testSearchReports_dateAndType_success() {
    //     LocalDate startLocalDate = LocalDate.parse("2024-01-01");
    //     LocalDate endLocalDate = LocalDate.parse("2024-01-10");
    //     SearchReportResponse mockSearchReportResponse =
    //             new SearchReportResponse(1, List.of(new SearchReportDetailsResponse()));
    //     when(dbReportService.searchReports(startLocalDate, endLocalDate, ReportType.daily, 0,
    // 10))
    //             .thenReturn(mockSearchReportResponse);

    //     ResponseEntity<?> responseEntity =
    //             reportApiController.searchReports(
    //                     startLocalDate, endLocalDate, ReportType.daily, 0, 10);

    //     // Verify response
    //     assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    //     SearchReportResponse responseBody = (SearchReportResponse) responseEntity.getBody();
    //     assertNotNull(responseBody);
    //     assertEquals(1, responseBody.getReports().size());
    //     assertEquals(mockSearchReportResponse, responseBody);

    //     // Verify DBService
    //     verify(dbReportService, times(1))
    //             .searchReports(startLocalDate, endLocalDate, ReportType.daily, 0, 10);
    // }

    // @Test
    // public void testSearchReports_nullStartDate_badRequest() {
    //     LocalDate endLocalDate = LocalDate.parse("2024-01-10");

    //     ResponseEntity<?> responseEntity =
    //             reportApiController.searchReports(null, endLocalDate, ReportType.daily, 0, 10);

    //     // Verify response
    //     assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    //     assertEquals("Both start and end dates need to be defined", responseEntity.getBody());

    //     // Verify DBService
    //     verify(dbReportService, never()).searchReports(any(), any(), any(), anyInt(), anyInt());
    // }

    // @Test
    // public void testSearchReports_startDateAfterEndDate_badRequest() {
    //     LocalDate endLocalDate = LocalDate.parse("2024-01-01");
    //     LocalDate startLocalDate = LocalDate.parse("2024-01-10");

    //     ResponseEntity<?> responseEntity =
    //             reportApiController.searchReports(
    //                     startLocalDate, endLocalDate, ReportType.daily, 0, 10);

    //     // Verify response
    //     assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    //     assertEquals("End date must be after start date.", responseEntity.getBody());

    //     // Verify DBService
    //     verify(dbReportService, never()).searchReports(any(), any(), any(), anyInt(), anyInt());
    // }

    // @Test
    // public void testSearchReports_invalidReportType_badRequest() {
    //     LocalDate startLocalDate = LocalDate.parse("2024-01-01");
    //     LocalDate endLocalDate = LocalDate.parse("2024-01-10");

    //     ResponseEntity<?> responseEntity =
    //             reportApiController.searchReports(startLocalDate, endLocalDate, null, 0, 10);

    //     // Verify response
    //     assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    //     assertEquals("Invalid report type", responseEntity.getBody());

    //     // Verify DBService
    //     verify(dbReportService, never()).searchReports(any(), any(), any(), anyInt(), anyInt());
    // }

    // @Test
    // public void testSearchReports_negativePage_badRequest() {
    //     LocalDate startLocalDate = LocalDate.parse("2024-01-01");
    //     LocalDate endLocalDate = LocalDate.parse("2024-01-10");

    //     ResponseEntity<?> responseEntity =
    //             reportApiController.searchReports(
    //                     startLocalDate, endLocalDate, ReportType.daily, -1, 10);

    //     // Verify response
    //     assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    //     assertEquals("'page' is not a valid number", responseEntity.getBody());

    //     // Verify DBService
    //     verify(dbReportService, never()).searchReports(any(), any(), any(), anyInt(), anyInt());
    // }

    // @Test
    // public void testSearchReports_negativeLimit_badRequest() {
    //     LocalDate startLocalDate = LocalDate.parse("2024-01-01");
    //     LocalDate endLocalDate = LocalDate.parse("2024-01-10");

    //     ResponseEntity<?> responseEntity =
    //             reportApiController.searchReports(
    //                     startLocalDate, endLocalDate, ReportType.daily, 0, -1);

    //     // Verify response
    //     assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    //     assertEquals("'limit' is not a valid number", responseEntity.getBody());

    //     // Verify DBService
    //     verify(dbReportService, never()).searchReports(any(), any(), any(), anyInt(), anyInt());
    // }

    @Test
    public void testGetReportByID_success() {
        String expectedFormat = "json";
        RawReport mockRawReport = mock(RawReport.class);
        when(dbReportService.getRawReport(anyInt())).thenReturn(Optional.of(mockRawReport));

        ReportFormatter mockReportFormatter = mock(ReportFormatter.class);
        reportFormatterMap.put(expectedFormat, mockReportFormatter);
        when(mockReportFormatter.format(mockRawReport)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> actual = reportApiController.getReportByID(1, expectedFormat);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    public void testGetReportByID_invalidFormat_badRequest() {
        String expectedFormat = "badFormat";
        RawReport mockRawReport = mock(RawReport.class);
        when(dbReportService.getRawReport(anyInt())).thenReturn(Optional.of(mockRawReport));

        ResponseEntity<?> actual = reportApiController.getReportByID(1, expectedFormat);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void testGetReportByID_notFoundInDb_notFound() {
        String expectedFormat = "json";
        when(dbReportService.getRawReport(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<?> actual = reportApiController.getReportByID(1, expectedFormat);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testGetLatestReport_success() {
        String expectedFormat = "json";
        RawReport mockRawReport = mock(RawReport.class);
        when(dbReportService.getRawReport(anyInt())).thenReturn(Optional.of(mockRawReport));
        when(dbReportService.getLatestReportId()).thenReturn(1);

        ReportFormatter mockReportFormatter = mock(ReportFormatter.class);
        reportFormatterMap.put(expectedFormat, mockReportFormatter);
        when(mockReportFormatter.format(mockRawReport)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> actual = reportApiController.getLatestReport(expectedFormat);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    public void testGetLatestReport_invalidFormat_badRequest() {
        String expectedFormat = "badFormat";
        RawReport mockRawReport = mock(RawReport.class);
        when(dbReportService.getRawReport(anyInt())).thenReturn(Optional.of(mockRawReport));
        when(dbReportService.getLatestReportId()).thenReturn(1);

        ResponseEntity<?> actual = reportApiController.getLatestReport(expectedFormat);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void testGetLatestReport_notFoundInDb_notFound() {
        String expectedFormat = "json";
        when(dbReportService.getLatestReportId()).thenReturn(-1);

        ResponseEntity<?> actual = reportApiController.getLatestReport(expectedFormat);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testAddArticlesToReport_success() {
        int reportId = 1;
        String[] articleIds = {"article1", "article2"};

        when(dbArticlesService.addArticlesToReport(reportId, articleIds)).thenReturn(true);

        ResponseEntity<?> responseEntity =
                reportApiController.addArticlesToReport(reportId, articleIds);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbArticlesService, times(1)).addArticlesToReport(reportId, articleIds);
    }

    @Test
    public void testAddArticlesToReport_notFound() {
        int reportId = 1;
        String[] articleIds = {"article1", "article2"};

        when(dbArticlesService.addArticlesToReport(reportId, articleIds)).thenReturn(false);

        ResponseEntity<?> responseEntity =
                reportApiController.addArticlesToReport(reportId, articleIds);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbArticlesService, times(1)).addArticlesToReport(reportId, articleIds);
    }

    @Test
    public void testAddSingleArticleToReport_success() {
        int reportId = 1;
        String articleId = "article1";
        String[] ids = new String[] {articleId};
        when(dbArticlesService.addArticlesToReport(reportId, ids)).thenReturn(true);

        ResponseEntity<?> responseEntity =
                reportApiController.addSingleArticleToReport(reportId, articleId);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbArticlesService, times(1)).addArticlesToReport(reportId, ids);
    }

    @Test
    public void testAddSingleArticleToReport_notFound() {
        int reportId = 1;
        String articleId = "article1";

        String[] ids = new String[] {articleId};
        when(dbArticlesService.addArticlesToReport(reportId, ids)).thenReturn(false);

        ResponseEntity<?> responseEntity =
                reportApiController.addSingleArticleToReport(reportId, articleId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbArticlesService, times(1)).addArticlesToReport(reportId, ids);
    }

    @Test
    public void testRemoveArticlesFromReport_success() {
        int reportId = 1;
        String[] articleIds = {"article1", "article2"};

        when(dbArticlesService.removeArticlesFromReport(reportId, articleIds)).thenReturn(true);

        ResponseEntity<?> responseEntity =
                reportApiController.removeArticlesFromReport(reportId, articleIds);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbArticlesService, times(1)).removeArticlesFromReport(reportId, articleIds);
    }

    @Test
    public void testRemoveArticlesFromReport_notFound() {
        int reportId = 1;
        String[] articleIds = {"article1", "article2"};

        when(dbArticlesService.removeArticlesFromReport(reportId, articleIds)).thenReturn(false);

        ResponseEntity<?> responseEntity =
                reportApiController.removeArticlesFromReport(reportId, articleIds);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbArticlesService, times(1)).removeArticlesFromReport(reportId, articleIds);
    }

    @Test
    public void testRemoveSingleArticleFromReport_success() {
        int reportId = 1;
        String articleId = "article1";
        String[] ids = new String[] {articleId};
        when(dbArticlesService.removeArticlesFromReport(reportId, ids)).thenReturn(true);

        ResponseEntity<?> responseEntity =
                reportApiController.removeSingleArticlesFromReport(reportId, articleId);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbArticlesService, times(1)).removeArticlesFromReport(reportId, ids);
    }

    @Test
    public void testRemoveSingleArticleFromReport_notFound() {
        int reportId = 1;
        String articleId = "article1";

        String[] ids = new String[] {articleId};
        when(dbArticlesService.removeArticlesFromReport(reportId, ids)).thenReturn(false);

        ResponseEntity<?> responseEntity =
                reportApiController.removeSingleArticlesFromReport(reportId, articleId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbArticlesService, times(1)).removeArticlesFromReport(reportId, ids);
    }

    @Test
    public void testAddStatsToReport_success() {
        int reportId = 1;
        String[] statisticIds = {"stat1", "stat2"};

        when(dbStatsService.addStatsToReport(reportId, statisticIds)).thenReturn(true);

        ResponseEntity<?> responseEntity =
                reportApiController.addStatsToReport(reportId, statisticIds);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbStatsService, times(1)).addStatsToReport(reportId, statisticIds);
    }

    @Test
    public void testAddStatsToReport_notFound() {
        int reportId = 1;
        String[] statisticIds = {"stat1", "stat2"};

        when(dbStatsService.addStatsToReport(reportId, statisticIds)).thenReturn(false);

        ResponseEntity<?> responseEntity =
                reportApiController.addStatsToReport(reportId, statisticIds);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbStatsService, times(1)).addStatsToReport(reportId, statisticIds);
    }

    @Test
    public void testAddSingleStatToReport_success() {
        int reportId = 1;
        String statisticId = "stat1";
        String[] ids = new String[] {statisticId};

        when(dbStatsService.addStatsToReport(reportId, ids)).thenReturn(true);

        ResponseEntity<?> responseEntity =
                reportApiController.addSingleStatToReport(reportId, statisticId);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbStatsService, times(1)).addStatsToReport(reportId, ids);
    }

    @Test
    public void testAddSingleStatToReport_notFound() {
        int reportId = 1;
        String statisticId = "stat1";
        String[] ids = new String[] {statisticId};

        when(dbStatsService.addStatsToReport(reportId, ids)).thenReturn(false);

        ResponseEntity<?> responseEntity =
                reportApiController.addSingleStatToReport(reportId, statisticId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbStatsService, times(1)).addStatsToReport(reportId, ids);
    }

    @Test
    public void testRemoveStatsFromReport_success() {
        int reportId = 1;
        String[] statisticIds = {"stat1", "stat2"};

        when(dbStatsService.removeStatsFromReport(reportId, statisticIds)).thenReturn(true);

        ResponseEntity<?> responseEntity =
                reportApiController.removeStatsFromReport(reportId, statisticIds);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbStatsService, times(1)).removeStatsFromReport(reportId, statisticIds);
    }

    @Test
    public void testRemoveStatsFromReport_notFound() {
        int reportId = 1;
        String[] statisticIds = {"stat1", "stat2"};

        when(dbStatsService.removeStatsFromReport(reportId, statisticIds)).thenReturn(false);

        ResponseEntity<?> responseEntity =
                reportApiController.removeStatsFromReport(reportId, statisticIds);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbStatsService, times(1)).removeStatsFromReport(reportId, statisticIds);
    }

    @Test
    public void testRemoveSingleStatFromReport_success() {
        int reportId = 1;
        String statisticId = "stat1";
        String[] ids = new String[] {statisticId};
        when(dbStatsService.removeStatsFromReport(reportId, ids)).thenReturn(true);

        ResponseEntity<?> responseEntity =
                reportApiController.removeSingleStatFromReport(reportId, statisticId);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbStatsService, times(1)).removeStatsFromReport(reportId, ids);
    }

    @Test
    public void testRemoveSingleStatFromReport_notFound() {
        int reportId = 1;
        String statisticId = "stat1";
        String[] ids = new String[] {statisticId};
        when(dbStatsService.removeStatsFromReport(reportId, ids)).thenReturn(false);

        ResponseEntity<?> responseEntity =
                reportApiController.removeSingleStatFromReport(reportId, statisticId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbStatsService, times(1)).removeStatsFromReport(reportId, ids);
    }

    @Test
    public void testGetReportSuggestions_success() {
        JsonArticleReportResponse expectedArticleResponse =
                new JsonArticleReportResponse(
                        "someArticle",
                        "someTitle",
                        "someDescription",
                        "someCategory",
                        "someLink",
                        Collections.emptyList(),
                        LocalDate.of(2024, 1, 1));
        JsonStatsResponse expectedStatResponse =
                new JsonStatsResponse("someStat", 10, "someStatTitle", "someStatSubtitle");

        when(dbReportService.doesReportExist(anyInt())).thenReturn(true);
        when(dbArticlesService.getReportSuggestions(anyInt()))
                .thenReturn(List.of(expectedArticleResponse));
        when(dbStatsService.getReportSuggestions(anyInt()))
                .thenReturn(List.of(expectedStatResponse));

        ResponseEntity<?> actual = reportApiController.getReportSuggestions(1);

        assertEquals(HttpStatus.OK, actual.getStatusCode());

        JsonReportSuggestionsResponse actualResponse =
                (JsonReportSuggestionsResponse) actual.getBody();
        assertNotNull(actualResponse);
        assertEquals(List.of(expectedArticleResponse), actualResponse.getArticles());
        assertEquals(List.of(expectedStatResponse), actualResponse.getStats());
    }

    @Test
    public void testGetReportSuggestions_doesNotExist_notFound() {
        when(dbReportService.doesReportExist(anyInt())).thenReturn(false);

        ResponseEntity<?> actual = reportApiController.getReportSuggestions(1);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testPatchReportSuggestions_addArticle_success() {
        when(dbArticlesService.addReportSuggestion(anyInt(), any())).thenReturn(true);

        ResponseEntity<?> actual = reportApiController.patchReportSuggestions(1, CUSTOM_ID_1, null);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testPatchReportSuggestions_addArticleFailAdd_notFound() {
        when(dbArticlesService.addReportSuggestion(anyInt(), any())).thenReturn(false);

        ResponseEntity<?> actual = reportApiController.patchReportSuggestions(1, CUSTOM_ID_1, null);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testDeleteReportSuggestions_removeArticle_success() {
        when(dbArticlesService.removeReportSuggestion(anyInt(), any())).thenReturn(true);

        ResponseEntity<?> actual =
                reportApiController.deleteReportSuggestions(1, CUSTOM_ID_1, null);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testDeleteReportSuggestions_removeArticleFailAdd_notFound() {
        when(dbArticlesService.removeReportSuggestion(anyInt(), any())).thenReturn(false);

        ResponseEntity<?> actual =
                reportApiController.deleteReportSuggestions(1, CUSTOM_ID_1, null);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testPatchReportSuggestions_addStat_success() {
        when(dbStatsService.addReportSuggestion(anyInt(), any())).thenReturn(true);

        ResponseEntity<?> actual = reportApiController.patchReportSuggestions(1, null, CUSTOM_ID_1);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testPatchReportSuggestions_addStatFailAdd_notFound() {
        when(dbStatsService.addReportSuggestion(anyInt(), any())).thenReturn(false);

        ResponseEntity<?> actual = reportApiController.patchReportSuggestions(1, null, CUSTOM_ID_1);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testDeleteReportSuggestions_removeStat_success() {
        when(dbStatsService.removeReportSuggestion(anyInt(), any())).thenReturn(true);

        ResponseEntity<?> actual =
                reportApiController.deleteReportSuggestions(1, null, CUSTOM_ID_1);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testDeleteReportSuggestions_removeStatFailAdd_notFound() {
        when(dbStatsService.removeReportSuggestion(anyInt(), any())).thenReturn(false);

        ResponseEntity<?> actual =
                reportApiController.deleteReportSuggestions(1, null, CUSTOM_ID_1);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }
}
