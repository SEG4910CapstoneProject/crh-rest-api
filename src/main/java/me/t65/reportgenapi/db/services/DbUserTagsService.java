package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.postgres.entities.UserTagEntity;

import java.util.List;
import java.util.UUID;

public interface DbUserTagsService {

    /** Get all tags for a given user. */
    List<UserTagEntity> getTagsForUser(Long userId);

    /** Create a new tag for a user. */
    UserTagEntity createTag(Long userId, String tagName);

    /** Delete an existing tag for a user. */
    boolean deleteTag(Long userId, Long tagId);

    /** Assign a favourite article to a tag. */
    boolean addArticleToTag(Long userId, Long tagId, UUID articleId);

    /** Remove an article from a tag. */
    boolean removeArticleFromTag(Long userId, Long tagId, UUID articleId);

    /** Get all articles under a specific tag for a user. */
    List<JsonArticleReportResponse> getArticlesByTag(Long userId, Long tagId);

    UserTagEntity renameTag(Long userId, Long tagId, String newName);

}
