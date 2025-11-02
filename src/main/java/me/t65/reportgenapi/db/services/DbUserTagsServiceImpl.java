package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.postgres.entities.UserFavouriteEntity;
import me.t65.reportgenapi.db.postgres.entities.UserTagArticleEntity;
import me.t65.reportgenapi.db.postgres.entities.UserTagEntity;
import me.t65.reportgenapi.db.postgres.repository.UserFavouriteRepository;
import me.t65.reportgenapi.db.postgres.repository.UserTagArticleRepository;
import me.t65.reportgenapi.db.postgres.repository.UserTagRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DbUserTagsServiceImpl implements DbUserTagsService {

    private final UserTagRepository userTagRepository;
    private final UserTagArticleRepository userTagArticleRepository;
    private final UserFavouriteRepository userFavouriteRepository;
    private final DbArticlesService dbArticlesService; // for retrieving article info

    @Autowired
    public DbUserTagsServiceImpl(
            UserTagRepository userTagRepository,
            UserTagArticleRepository userTagArticleRepository,
            UserFavouriteRepository userFavouriteRepository,
            DbArticlesService dbArticlesService) {
        this.userTagRepository = userTagRepository;
        this.userTagArticleRepository = userTagArticleRepository;
        this.userFavouriteRepository = userFavouriteRepository;
        this.dbArticlesService = dbArticlesService;
    }

    @Override
    public List<UserTagEntity> getTagsForUser(Long userId) {
        return userTagRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public UserTagEntity createTag(Long userId, String tagName) {
        if (userTagRepository.existsByUserIdAndTagName(userId, tagName)) {
            throw new IllegalArgumentException("Tag already exists");
        }
        UserTagEntity tag = new UserTagEntity();
        tag.setUserId(userId);
        tag.setTagName(tagName);
        return userTagRepository.save(tag);
    }

    @Override
    @Transactional
    public boolean deleteTag(Long userId, Long tagId) {
        Optional<UserTagEntity> tagOpt = userTagRepository.findById(tagId);
        if (tagOpt.isEmpty() || !tagOpt.get().getUserId().equals(userId)) {
            return false;
        }

        userTagRepository.delete(tagOpt.get());
        return true;
    }

    @Override
    @Transactional
    public boolean addArticleToTag(Long userId, Long tagId, UUID articleId) {
        // Ensure the article is already a favourite
        boolean isFav = userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId);
        if (!isFav) {
            throw new IllegalArgumentException("Article must be a favourite to tag");
        }

        UserTagArticleEntity.PK pk = new UserTagArticleEntity.PK(tagId, articleId);
        if (userTagArticleRepository.existsById(pk)) {
            return true; // already added
        }

        userTagArticleRepository.save(new UserTagArticleEntity(tagId, articleId));
        return true;
    }

    @Override
    @Transactional
    public boolean removeArticleFromTag(Long userId, Long tagId, UUID articleId) {
        userTagArticleRepository.deleteById(new UserTagArticleEntity.PK(tagId, articleId));
        return true;
    }

    @Override
    public List<JsonArticleReportResponse> getArticlesByTag(Long userId, Long tagId) {
        // Verify tag belongs to user
        Optional<UserTagEntity> tagOpt = userTagRepository.findById(tagId);
        if (tagOpt.isEmpty() || !tagOpt.get().getUserId().equals(userId)) {
            return Collections.emptyList();
        }

        List<UserTagArticleEntity> mappings = userTagArticleRepository.findByTagId(tagId);
        List<UUID> articleIds =
                mappings.stream().map(UserTagArticleEntity::getArticleId).collect(Collectors.toList());

        List<JsonArticleReportResponse> result = new ArrayList<>();
        for (UUID id : articleIds) {
            dbArticlesService.getArticleById(id).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public UserTagEntity renameTag(Long userId, Long tagId, String newName) {
        // Fetch the tag that belongs to this user
        UserTagEntity tag = userTagRepository.findByUserIdAndTagId(userId, tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found for user: " + userId));

        // Validate new name
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New tag name cannot be empty");
        }

        // Prevent duplicate tag names for the same user
        if (userTagRepository.existsByUserIdAndTagName(userId, newName.trim())) {
            throw new IllegalArgumentException("A tag with this name already exists");
        }

        // Update and save
        tag.setTagName(newName.trim());
        return userTagRepository.save(tag);
    }

}
