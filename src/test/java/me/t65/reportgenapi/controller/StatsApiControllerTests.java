package me.t65.reportgenapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import me.t65.reportgenapi.controller.payload.UidResponse;
import me.t65.reportgenapi.db.services.DbStatsService;
import me.t65.reportgenapi.utils.IdGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class StatsApiControllerTests {

    private final String CUSTOM_ID_1 = "53d890f0-c160-4da4-9056-cb71d4a0e7fb";
    private final UUID CUSTOM_UID_1 = UUID.fromString(CUSTOM_ID_1);

    @Mock private DbStatsService dbStatsService;
    @Mock private IdGenerator idGenerator;

    private StatsApiController statsApiController;

    @BeforeEach
    public void beforeEach() {
        statsApiController = new StatsApiController(dbStatsService, idGenerator);
    }

    @Test
    public void testEditStat_success() {
        Integer number = 123;
        String title = "New Title";
        String subtitle = "New Subtitle";

        when(dbStatsService.editStat(eq(CUSTOM_UID_1), eq(number), eq(title), eq(subtitle)))
                .thenReturn(true);

        ResponseEntity<?> responseEntity =
                statsApiController.editStat(CUSTOM_ID_1, number, title, subtitle);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(dbStatsService, times(1))
                .editStat(eq(CUSTOM_UID_1), eq(number), eq(title), eq(subtitle));
    }

    @Test
    public void testEditStat_notFound() {
        Integer number = 123;
        String title = "New Title";
        String subtitle = "New Subtitle";

        when(dbStatsService.editStat(eq(CUSTOM_UID_1), eq(number), eq(title), eq(subtitle)))
                .thenReturn(false);

        ResponseEntity<?> responseEntity =
                statsApiController.editStat(CUSTOM_ID_1, number, title, subtitle);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(dbStatsService, times(1))
                .editStat(eq(CUSTOM_UID_1), eq(number), eq(title), eq(subtitle));
    }

    @Test
    public void testAddStat_success() {
        Integer number = 123;
        String title = "New Title";
        String subtitle = "New Subtitle";

        when(dbStatsService.addStat(eq(CUSTOM_UID_1), eq(number), eq(title), eq(subtitle)))
                .thenReturn(true);
        when(idGenerator.generateId()).thenReturn(CUSTOM_UID_1);

        ResponseEntity<?> responseEntity = statsApiController.addStat(number, title, subtitle);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(new UidResponse(CUSTOM_ID_1), responseEntity.getBody());
        verify(dbStatsService, times(1))
                .addStat(eq(CUSTOM_UID_1), eq(number), eq(title), eq(subtitle));
    }

    @Test
    public void testAddStat_serverError() {
        Integer number = 123;
        String title = "New Title";
        String subtitle = "New Subtitle";

        when(dbStatsService.addStat(eq(CUSTOM_UID_1), eq(number), eq(title), eq(subtitle)))
                .thenReturn(false);
        when(idGenerator.generateId()).thenReturn(CUSTOM_UID_1);

        ResponseEntity<?> responseEntity = statsApiController.addStat(number, title, subtitle);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        verify(dbStatsService, times(1))
                .addStat(eq(CUSTOM_UID_1), eq(number), eq(title), eq(subtitle));
    }
}
