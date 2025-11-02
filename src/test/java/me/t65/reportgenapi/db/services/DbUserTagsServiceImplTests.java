package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbUserTagsServiceImplTests {

    @Mock private UserTagRepository userTagRepository;
    @Mock private UserTagArticleRepository userTagArticleRepository;
    @Mock private UserFavouriteRepository userFavouriteRepository;
    @Mock private DbArticlesService dbArticlesService;

    @InjectMocks private DbUserTagsServiceImpl dbUserTagsService;

    private final Long userId = 1L;
    private final Long tagId = 10L;
    private final UUID articleId = UUID.randomUUID();

    private UserTagEntity tag;

    @BeforeEach
    void setup() {
        tag = new UserTagEntity();
        tag.setUserId(userId);
        tag.setTagId(tagId);
        tag.setTagName("MyTag");
    }

    //  getTagsForUser
    @Test
    void testGetTagsForUser_returnsList() {
        List<UserTagEntity> tags = List.of(tag);
        when(userTagRepository.findByUserId(userId)).thenReturn(tags);

        List<UserTagEntity> result = dbUserTagsService.getTagsForUser(userId);

        assertEquals(1, result.size());
        verify(userTagRepository).findByUserId(userId);
    }

    //  createTag
    @Test
    void testCreateTag_success() {
        when(userTagRepository.existsByUserIdAndTagName(userId, "NewTag")).thenReturn(false);
        when(userTagRepository.save(any(UserTagEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserTagEntity result = dbUserTagsService.createTag(userId, "NewTag");

        assertEquals("NewTag", result.getTagName());
        verify(userTagRepository).save(any(UserTagEntity.class));
    }

    @Test
    void testCreateTag_alreadyExists_throwsException() {
        when(userTagRepository.existsByUserIdAndTagName(userId, "MyTag")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> dbUserTagsService.createTag(userId, "MyTag"));
        verify(userTagRepository, never()).save(any());
    }

    //  deleteTag
    @Test
    void testDeleteTag_success() {
        when(userTagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        boolean result = dbUserTagsService.deleteTag(userId, tagId);

        assertTrue(result);
        verify(userTagRepository).delete(tag);
    }

    @Test
    void testDeleteTag_notOwnedByUser_returnsFalse() {
        tag.setUserId(2L);
        when(userTagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        boolean result = dbUserTagsService.deleteTag(userId, tagId);

        assertFalse(result);
        verify(userTagRepository, never()).delete(any());
    }

    //  addArticleToTag
    @Test
    void testAddArticleToTag_success() {
        when(userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(true);
        when(userTagArticleRepository.existsById(any())).thenReturn(false);

        boolean result = dbUserTagsService.addArticleToTag(userId, tagId, articleId);

        assertTrue(result);
        verify(userTagArticleRepository).save(any(UserTagArticleEntity.class));
    }

    @Test
    void testAddArticleToTag_articleNotFavourite_throwsException() {
        when(userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> dbUserTagsService.addArticleToTag(userId, tagId, articleId));
        verify(userTagArticleRepository, never()).save(any());
    }

    // removeArticleFromTag
    @Test
    void testRemoveArticleFromTag_success() {
        boolean result = dbUserTagsService.removeArticleFromTag(userId, tagId, articleId);
        assertTrue(result);
        verify(userTagArticleRepository).deleteById(any(UserTagArticleEntity.PK.class));
    }

    // getArticlesByTag
    @Test
    void testGetArticlesByTag_success() {
        when(userTagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        UserTagArticleEntity mapping = new UserTagArticleEntity(tagId, articleId);
        when(userTagArticleRepository.findByTagId(tagId)).thenReturn(List.of(mapping));

        JsonArticleReportResponse article = mock(JsonArticleReportResponse.class);
        when(dbArticlesService.getArticleById(articleId)).thenReturn(Optional.of(article));

        List<JsonArticleReportResponse> result = dbUserTagsService.getArticlesByTag(userId, tagId);

        assertEquals(1, result.size());
        verify(dbArticlesService).getArticleById(articleId);
    }

    @Test
    void testGetArticlesByTag_notOwned_returnsEmptyList() {
        tag.setUserId(2L);
        when(userTagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        List<JsonArticleReportResponse> result = dbUserTagsService.getArticlesByTag(userId, tagId);

        assertTrue(result.isEmpty());
        verify(userTagArticleRepository, never()).findByTagId(any());
    }

    //  renameTag
    @Test
    void testRenameTag_success() {
        when(userTagRepository.findByUserIdAndTagId(userId, tagId)).thenReturn(Optional.of(tag));
        when(userTagRepository.existsByUserIdAndTagName(userId, "Updated")).thenReturn(false);
        when(userTagRepository.save(any(UserTagEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserTagEntity result = dbUserTagsService.renameTag(userId, tagId, "Updated");

        assertEquals("Updated", result.getTagName());
        verify(userTagRepository).save(tag);
    }

    @Test
    void testRenameTag_tagNotFound_throwsException() {
        when(userTagRepository.findByUserIdAndTagId(userId, tagId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> dbUserTagsService.renameTag(userId, tagId, "NewName"));
    }

    @Test
    void testRenameTag_duplicateName_throwsException() {
        when(userTagRepository.findByUserIdAndTagId(userId, tagId)).thenReturn(Optional.of(tag));
        when(userTagRepository.existsByUserIdAndTagName(userId, "DupName")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> dbUserTagsService.renameTag(userId, tagId, "DupName"));
    }

    @Test
    void testRenameTag_blankName_throwsException() {
        when(userTagRepository.findByUserIdAndTagId(userId, tagId)).thenReturn(Optional.of(tag));

        assertThrows(IllegalArgumentException.class,
                () -> dbUserTagsService.renameTag(userId, tagId, " "));
    }
}
