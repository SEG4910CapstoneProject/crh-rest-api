package me.t65.reportgenapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.postgres.entities.UserTagEntity;
import me.t65.reportgenapi.db.services.DbUserTagsService;
import me.t65.reportgenapi.utils.CurrentUser;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "User Tags")
@RestController
@RequestMapping("/api/v1/tags")
@CrossOrigin(origins = "http://localhost:4200")
public class UserTagsApiController {

    private final DbUserTagsService dbUserTagsService;
    private final CurrentUser currentUser;

    public UserTagsApiController(DbUserTagsService dbUserTagsService, CurrentUser currentUser) {
        this.dbUserTagsService = dbUserTagsService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<UserTagEntity>> getMyTags() {
        UserEntity user = currentUser.requireUser();
        return ResponseEntity.ok(dbUserTagsService.getTagsForUser(user.getUserId()));
    }

    @PostMapping
    public ResponseEntity<?> createTag(@RequestBody Map<String, String> body) {
        UserEntity user = currentUser.requireUser();
        String name = body.get("tagName");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tag name is required"));
        }

        UserTagEntity created = dbUserTagsService.createTag(user.getUserId(), name.trim());
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        UserEntity user = currentUser.requireUser();
        dbUserTagsService.deleteTag(user.getUserId(), tagId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tagId}/articles/{articleId}")
    public ResponseEntity<Void> addArticleToTag(
            @PathVariable Long tagId, @PathVariable UUID articleId) {
        UserEntity user = currentUser.requireUser();
        dbUserTagsService.addArticleToTag(user.getUserId(), tagId, articleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tagId}/articles/{articleId}")
    public ResponseEntity<Void> removeArticleFromTag(
            @PathVariable Long tagId, @PathVariable UUID articleId) {
        UserEntity user = currentUser.requireUser();
        dbUserTagsService.removeArticleFromTag(user.getUserId(), tagId, articleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tagId}/articles")
    public ResponseEntity<List<JsonArticleReportResponse>> getArticlesByTag(
            @PathVariable Long tagId) {
        UserEntity user = currentUser.requireUser();
        return ResponseEntity.ok(dbUserTagsService.getArticlesByTag(user.getUserId(), tagId));
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<UserTagEntity> renameTag(
            @PathVariable Long tagId, @RequestBody Map<String, String> body) {

        UserEntity user = currentUser.requireUser();
        String newName = body.get("tagName");

        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        UserTagEntity updated =
                dbUserTagsService.renameTag(user.getUserId(), tagId, newName.trim());
        return ResponseEntity.ok(updated);
    }
}
