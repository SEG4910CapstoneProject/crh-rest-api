package me.t65.reportgenapi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.postgres.entities.UserTagEntity;
import me.t65.reportgenapi.db.services.DbUserTagsService;
import me.t65.reportgenapi.utils.CurrentUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class UserTagsApiControllerTests {

    @Mock private DbUserTagsService dbUserTagsService;
    @Mock private CurrentUser currentUser;

    @InjectMocks private UserTagsApiController userTagsApiController;

    private UserEntity mockUser;

    @BeforeEach
    void setup() {
        mockUser = new UserEntity();
        mockUser.setUserId(1L);
        when(currentUser.requireUser()).thenReturn(mockUser);
    }

    //  getMyTags
    @Test
    void testGetMyTags_success() {
        List<UserTagEntity> tags = List.of(new UserTagEntity());
        when(dbUserTagsService.getTagsForUser(1L)).thenReturn(tags);

        ResponseEntity<List<UserTagEntity>> actual = userTagsApiController.getMyTags();

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(tags, actual.getBody());
        verify(dbUserTagsService, times(1)).getTagsForUser(1L);
    }

    //  createTag
    @Test
    void testCreateTag_success() {
        Map<String, String> body = Map.of("tagName", "Tech News");
        UserTagEntity newTag = new UserTagEntity();
        newTag.setTagName("Tech News");

        when(dbUserTagsService.createTag(1L, "Tech News")).thenReturn(newTag);

        ResponseEntity<?> actual = userTagsApiController.createTag(body);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(newTag, actual.getBody());
    }

    @Test
    void testCreateTag_missingName_returnsBadRequest() {
        Map<String, String> body = Map.of(); // no name

        ResponseEntity<?> actual = userTagsApiController.createTag(body);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertTrue(((Map<?, ?>) actual.getBody()).containsKey("error"));
        verify(dbUserTagsService, never()).createTag(anyLong(), anyString());
    }

    //  deleteTag
    @Test
    void testDeleteTag_success() {
        ResponseEntity<Void> actual = userTagsApiController.deleteTag(99L);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        verify(dbUserTagsService, times(1)).deleteTag(1L, 99L);
    }

    //  addArticleToTag
    @Test
    void testAddArticleToTag_success() {
        UUID articleId = UUID.randomUUID();

        ResponseEntity<Void> actual = userTagsApiController.addArticleToTag(3L, articleId);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        verify(dbUserTagsService, times(1)).addArticleToTag(1L, 3L, articleId);
    }

    //  removeArticleFromTag
    @Test
    void testRemoveArticleFromTag_success() {
        UUID articleId = UUID.randomUUID();

        ResponseEntity<Void> actual = userTagsApiController.removeArticleFromTag(4L, articleId);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        verify(dbUserTagsService, times(1)).removeArticleFromTag(1L, 4L, articleId);
    }

    //  getArticlesByTag
    @Test
    void testGetArticlesByTag_success() {
        List<JsonArticleReportResponse> articles = List.of(mock(JsonArticleReportResponse.class));
        when(dbUserTagsService.getArticlesByTag(1L, 5L)).thenReturn(articles);

        ResponseEntity<List<JsonArticleReportResponse>> actual =
                userTagsApiController.getArticlesByTag(5L);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(articles, actual.getBody());
        verify(dbUserTagsService, times(1)).getArticlesByTag(1L, 5L);
    }

    //  renameTag
    @Test
    void testRenameTag_success() {
        Map<String, String> body = Map.of("tagName", "Updated Tag");
        UserTagEntity updated = new UserTagEntity();
        updated.setTagName("Updated Tag");

        when(dbUserTagsService.renameTag(1L, 8L, "Updated Tag")).thenReturn(updated);

        ResponseEntity<UserTagEntity> actual = userTagsApiController.renameTag(8L, body);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(updated, actual.getBody());
    }

    @Test
    void testRenameTag_missingName_returnsBadRequest() {
        Map<String, String> body = Map.of();

        ResponseEntity<UserTagEntity> actual = userTagsApiController.renameTag(8L, body);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertNull(actual.getBody());
        verify(dbUserTagsService, never()).renameTag(anyLong(), anyLong(), anyString());
    }
}
