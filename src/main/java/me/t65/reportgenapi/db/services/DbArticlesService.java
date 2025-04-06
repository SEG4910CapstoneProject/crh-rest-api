package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.postgres.dto.MonthlyArticleDTO;
import me.t65.reportgenapi.db.postgres.entities.CategoryEntity;
import me.t65.reportgenapi.db.postgres.entities.IOCEntity;
import me.t65.reportgenapi.db.postgres.entities.MonthlyArticlesEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public List<MonthlyArticleDTO> getTop10Articles();

    public Optional<MonthlyArticlesEntity> toggleArticleOfNote(UUID articleId);

    public Optional<MonthlyArticlesEntity> incrementViewCount(UUID articleId);

    public List<MonthlyArticleDTO> getArticlesOfNote();

    }
