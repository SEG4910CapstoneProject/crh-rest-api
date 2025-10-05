package me.t65.reportgenapi.db.services;

import static me.t65.reportgenapi.TestUtils.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.controller.payload.JsonStatsResponse;
import me.t65.reportgenapi.db.postgres.entities.ReportStatisticsEntity;
import me.t65.reportgenapi.db.postgres.entities.StatisticEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ReportStatisticsId;
import me.t65.reportgenapi.db.postgres.repository.*;

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

import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class DbStatsServiceImplTests {

    private final String STAT_ID_1 = "1d6aaa8a-d283-4d02-966d-d28889f93889";
    private final String STAT_ID_2 = "816d120b-c1da-40e9-a582-50cfa9ec9c75";

    private final UUID STAT_UID_1 = UUID.fromString(STAT_ID_1);
    private final UUID STAT_UID_2 = UUID.fromString(STAT_ID_2);

    @MockBean ReportRepository reportRepository;

    @MockBean StatisticsRepository statisticsRepository;

    @MockBean ReportStatisticsRepository reportStatisticsRepository;
    @MockBean ArticleTypeRepository articleTypeRepository;
    @MockBean MonthlyArticlesRepository monthlyArticlesRepository;
    @Autowired DbStatsServiceImpl dbStatsService;

    @MockBean UserRepository userRepository;
    @MockBean BCryptPasswordEncoder passwordEncoder;

    @Test
    public void testAddStatsToReport_success() {
        when(statisticsRepository.existsById(any())).thenReturn(true);

        boolean result = dbStatsService.addStatsToReport(1, new String[] {STAT_ID_1, STAT_ID_2});

        assertTrue(result);

        ArgumentCaptor<List> statListCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportStatisticsRepository).saveAll(statListCaptor.capture());
        assertEquals(2, statListCaptor.getValue().size());
        assertListContains(
                List.of(
                        getReportStatEntity(1, STAT_UID_1, false),
                        getReportStatEntity(1, STAT_UID_2, false)),
                statListCaptor.getValue());
    }

    @Test
    public void testAddStatsToReport_statDoesNotExist_notSavedAndFalseReturn() {
        when(statisticsRepository.existsById(any())).thenReturn(false);

        boolean result = dbStatsService.addStatsToReport(1, new String[] {STAT_ID_1});

        assertFalse(result);
        verify(reportStatisticsRepository, never()).saveAll(any());
    }

    @Test
    public void testAddStatsToReport_invalidUUID_notSavedAndFalseReturn() {
        boolean result = dbStatsService.addStatsToReport(1, new String[] {"BadUid"});

        assertFalse(result);
        verify(reportStatisticsRepository, never()).saveAll(any());
    }

    @Test
    public void testRemoveStatsFromReport_success() {
        when(reportStatisticsRepository.existsById(any())).thenReturn(true);
        boolean result =
                dbStatsService.removeStatsFromReport(1, new String[] {STAT_ID_1, STAT_ID_2});

        assertTrue(result);

        ArgumentCaptor<List> statListCaptor = ArgumentCaptor.forClass(List.class);
        verify(reportStatisticsRepository).deleteAllById(statListCaptor.capture());
        assertEquals(2, statListCaptor.getValue().size());
        assertListContains(
                List.of(
                        new ReportStatisticsId(1, STAT_UID_1),
                        new ReportStatisticsId(1, STAT_UID_2)),
                statListCaptor.getValue());
    }

    @Test
    public void testRemoveStatsFromReport_statDoesNotExist_notSavedAndFalseReturn() {
        when(reportStatisticsRepository.existsById(any())).thenReturn(false);

        boolean result = dbStatsService.removeStatsFromReport(1, new String[] {STAT_ID_1});

        assertFalse(result);
        verify(reportStatisticsRepository, never()).saveAll(any());
    }

    @Test
    public void testRemoveStatsFromReport_invalidUUID_notSavedAndFalseReturn() {
        boolean result = dbStatsService.removeStatsFromReport(1, new String[] {"BadUid"});

        assertFalse(result);
        verify(reportStatisticsRepository, never()).saveAll(any());
    }

    @Test
    public void testEditStat_success() {
        StatisticEntity expectedEntity =
                new StatisticEntity(STAT_UID_1, 1, "some title", "some substitle");

        boolean result = dbStatsService.editStat(STAT_UID_1, 1, "some title", "some substitle");

        assertTrue(result);
        verify(statisticsRepository).save(expectedEntity);
    }

    @Test
    public void testEditStat_exception_falseReturn() {
        when(statisticsRepository.save(any())).thenThrow(new RuntimeException("Some error"));

        boolean result = dbStatsService.editStat(STAT_UID_1, 1, "some title", "some substitle");

        assertFalse(result);
    }

    @Test
    public void testAddStat_success() {
        StatisticEntity expectedEntity =
                new StatisticEntity(STAT_UID_1, 1, "some title", "some substitle");

        boolean result = dbStatsService.addStat(STAT_UID_1, 1, "some title", "some substitle");

        assertTrue(result);
        verify(statisticsRepository).save(expectedEntity);
    }

    @Test
    public void testAddStat_exception_falseReturn() {
        when(statisticsRepository.save(any())).thenThrow(new RuntimeException("Some error"));

        boolean result = dbStatsService.addStat(STAT_UID_1, 1, "some title", "some substitle");

        assertFalse(result);
    }

    @Test
    public void testGetReportSuggestions_success() {
        ReportStatisticsEntity reportStatisticsEntity = getReportStatEntity(1, STAT_UID_1, true);
        StatisticEntity statisticEntity =
                new StatisticEntity(STAT_UID_1, 100, "someTitle", "someSubtitle");
        JsonStatsResponse expectedResponse =
                new JsonStatsResponse(
                        STAT_ID_1,
                        statisticEntity.getStatisticNumber(),
                        statisticEntity.getTitle(),
                        statisticEntity.getSubtitle());

        when(reportRepository.existsById(any())).thenReturn(true);
        when(reportStatisticsRepository.findByReportStatisticsId_ReportIdAndSuggestion(
                        any(), anyBoolean()))
                .thenReturn(List.of(reportStatisticsEntity));
        when(statisticsRepository.findAllById(any())).thenReturn(List.of(statisticEntity));

        List<JsonStatsResponse> actual = dbStatsService.getReportSuggestions(1);

        assertEquals(1, actual.size());
        assertEquals(expectedResponse, actual.get(0));
    }

    @Test
    public void testGetReportSuggestions_reportNotExist_emptyList() {
        when(reportRepository.existsById(any())).thenReturn(false);

        List<JsonStatsResponse> actual = dbStatsService.getReportSuggestions(1);

        assertEquals(0, actual.size());
    }

    @Test
    public void testAddReportSuggestion_success() {
        when(reportRepository.existsById(any())).thenReturn(true);
        when(statisticsRepository.existsById(any())).thenReturn(true);

        boolean actual = dbStatsService.addReportSuggestion(1, STAT_UID_1);

        assertTrue(actual);
        verify(reportStatisticsRepository)
                .save(new ReportStatisticsEntity(new ReportStatisticsId(1, STAT_UID_1), true));
    }

    @Test
    public void testAddReportSuggestion_reportDoesNotExist_falseReturn() {
        when(reportRepository.existsById(any())).thenReturn(false);

        boolean actual = dbStatsService.addReportSuggestion(1, STAT_UID_1);

        assertFalse(actual);
        verify(reportStatisticsRepository, never()).save(any());
    }

    @Test
    public void testAddReportSuggestion_statDoesNotExist_falseReturn() {
        when(reportRepository.existsById(any())).thenReturn(true);
        when(statisticsRepository.existsById(any())).thenReturn(false);

        boolean actual = dbStatsService.addReportSuggestion(1, STAT_UID_1);

        assertFalse(actual);
        verify(reportStatisticsRepository, never()).save(any());
    }

    @Test
    public void testRemoveReportSuggestion_success() {
        when(reportRepository.existsById(any())).thenReturn(true);
        when(reportStatisticsRepository
                        .existsByReportStatisticsId_ReportIdAndReportStatisticsId_StatisticIdAndSuggestion(
                                any(), any(), anyBoolean()))
                .thenReturn(true);

        boolean actual = dbStatsService.removeReportSuggestion(1, STAT_UID_1);

        assertTrue(actual);
        verify(reportStatisticsRepository).deleteById(new ReportStatisticsId(1, STAT_UID_1));
    }

    @Test
    public void testRemoveReportSuggestion_reportDoesNotExist_falseReturn() {
        when(reportRepository.existsById(any())).thenReturn(false);

        boolean actual = dbStatsService.removeReportSuggestion(1, STAT_UID_1);

        assertFalse(actual);
        verify(reportStatisticsRepository, never()).deleteById(any());
    }

    @Test
    public void testRemoveReportSuggestion_statDoesNotExist_trueReturn() {
        when(reportRepository.existsById(any())).thenReturn(true);
        when(reportStatisticsRepository
                        .existsByReportStatisticsId_ReportIdAndReportStatisticsId_StatisticIdAndSuggestion(
                                any(), any(), anyBoolean()))
                .thenReturn(false);

        boolean actual = dbStatsService.removeReportSuggestion(1, STAT_UID_1);

        assertTrue(actual);
        verify(reportStatisticsRepository, never()).deleteById(any());
    }

    private ReportStatisticsEntity getReportStatEntity(
            int report, UUID statId, boolean suggestion) {
        return new ReportStatisticsEntity(new ReportStatisticsId(report, statId), suggestion);
    }
}
