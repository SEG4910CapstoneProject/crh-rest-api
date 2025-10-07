package me.t65.reportgenapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.services.DbArticlesService;
import me.t65.reportgenapi.utils.CurrentUser;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Endpoints for managing per-user article favourites. */
@Tag(name = "Favourites")
@RestController
@RequestMapping("/api/v1/favourites")
@CrossOrigin(origins = "http://localhost:4200")
public class FavouritesApiController {

    private final DbArticlesService dbArticlesService;
    private final CurrentUser currentUser;

    public FavouritesApiController(DbArticlesService dbArticlesService, CurrentUser currentUser) {
        this.dbArticlesService = dbArticlesService;
        this.currentUser = currentUser;
    }

    // get all favourites for current user
    @GetMapping
    public ResponseEntity<List<JsonArticleReportResponse>> getMyFavourites() {
        UserEntity user = currentUser.requireUser();
        return ResponseEntity.ok(dbArticlesService.getFavouritesForUser(user.getUserId()));
    }

    // add article to favourites
    @PostMapping("/{articleId}")
    public ResponseEntity<?> addFavourite(@PathVariable UUID articleId) {
        UserEntity user = currentUser.requireUser();
        boolean added = dbArticlesService.addFavourite(user.getUserId(), articleId);
        return added ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // remove article from favourites
    @DeleteMapping("/{articleId}")
    public ResponseEntity<?> removeFavourite(@PathVariable UUID articleId) {
        UserEntity user = currentUser.requireUser();
        dbArticlesService.removeFavourite(user.getUserId(), articleId);
        return ResponseEntity.noContent().build();
    }
}
