package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.JsonArticleReportResponseWithTypeIncluded;
import me.t65.reportgenapi.db.postgres.dto.MonthlyArticleDTO;
import me.t65.reportgenapi.db.postgres.entities.CategoryEntity;
import me.t65.reportgenapi.db.postgres.entities.IOCEntity;
import me.t65.reportgenapi.db.postgres.entities.MonthlyArticlesEntity;

import java.time.Instant;
import java.util.*;

public interface DbArticlesService {
    Optional<JsonArticleReportResponse> getArticleById(UUID articleId);

    Optional<JsonArticleReportResponse> getArticleByLink(String link);

    boolean addArticlesToReport(int report, String[] articleIds);

    boolean removeArticlesFromReport(int report, String[] articleIds);

    boolean editArticleInReport(
            String articleId, String title, String link, String description, Instant publishDate);

    List<JsonArticleReportResponse> getReportSuggestions(int report);

    boolean addReportSuggestion(int report, UUID articleId);

    boolean removeReportSuggestion(int report, UUID articleId);

    Map<UUID, List<IOCEntity>> getArticleToIocEntityListMap(Collection<UUID> articleIds);

    void addNewArticle(
            UUID articleId, String title, String link, String description, Instant publishDate);

    Map<UUID, CategoryEntity> getArticleToCategoryEntityMap(Collection<UUID> articleIds);

    public List<JsonArticleReportResponse> getArticlesByType(String type);

    public Map<String, List<JsonArticleReportResponse>> getAllArticleTypesWithArticles(int days);

    public Optional<JsonArticleReportResponseWithTypeIncluded> getArticleByIdTypeIncluded(
            UUID articleId);

    public List<JsonArticleReportResponseWithTypeIncluded> getAllArticlesWithTypes(int days);

    public List<MonthlyArticleDTO> getTop10Articles();

    public Optional<MonthlyArticlesEntity> toggleArticleOfNote(UUID articleId);

    public Optional<MonthlyArticlesEntity> incrementViewCount(UUID articleId);

    List<JsonArticleReportResponse> getArticlesOfNote();

    /** Add an article to a user's favourites. Returns true if successful. */
    boolean addFavourite(Long userId, UUID articleId);

    /** Remove an article from a user's favourites. */
    void removeFavourite(Long userId, UUID articleId);

    /** Get all favourite articles for a user. */
    List<JsonArticleReportResponse> getFavouritesForUser(Long userId);

    /** Ingest a new article from URL */
    boolean ingestFromUrl(String link, String title, String description);

    /** Retrieve manually added articles by user */
    List<JsonArticleReportResponse> getManualArticles();

    /** Delete articles manually added by users */
    boolean deleteManualArticle(UUID articleId);

    /** Update an existing manually added article */
    boolean updateManualArticle(UUID articleId, String title, String link, String description);

}
