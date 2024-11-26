package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.mongo.repository.ArticleContentRepository;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.generators.JsonArticleGenerator;
import me.t65.reportgenapi.utils.DateService;
import me.t65.reportgenapi.utils.NormalizeLinks;
import me.t65.reportgenapi.utils.StreamUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DbArticlesServiceImpl implements DbArticlesService {
    private final Logger LOGGER = LoggerFactory.getLogger(DbArticlesServiceImpl.class);

    private final StreamUtils streamUtils;

    private final ReportRepository reportRepository;
    private final ArticlesRepository articlesRepository;
    private final ReportArticlesRepository reportArticlesRepository;
    private final ArticleContentRepository articleContentRepository;
    private final IOCArticlesEntityRepository iocArticlesEntityRepository;
    private final ArticleCategoryRepository articleCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final JsonArticleGenerator jsonArticleGenerator;
    private final DbEntitiesUtils dbEntitiesUtils;
    private final DateService dateService;

    @Autowired
    public DbArticlesServiceImpl(
            StreamUtils streamUtils,
            ReportRepository reportRepository,
            ArticlesRepository articlesRepository,
            ReportArticlesRepository reportArticlesRepository,
            ArticleContentRepository articleContentRepository,
            IOCArticlesEntityRepository iocArticlesEntityRepository,
            ArticleCategoryRepository articleCategoryRepository,
            CategoryRepository categoryRepository,
            JsonArticleGenerator jsonArticleGenerator,
            DbEntitiesUtils dbEntitiesUtils,
            DateService dateService) {
        this.streamUtils = streamUtils;
        this.reportRepository = reportRepository;
        this.articlesRepository = articlesRepository;
        this.reportArticlesRepository = reportArticlesRepository;
        this.articleContentRepository = articleContentRepository;
        this.iocArticlesEntityRepository = iocArticlesEntityRepository;
        this.articleCategoryRepository = articleCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.jsonArticleGenerator = jsonArticleGenerator;
        this.dbEntitiesUtils = dbEntitiesUtils;
        this.dateService = dateService;
    }

    @Override
    public Optional<JsonArticleReportResponse> getArticleById(UUID articleId) {
        Optional<ArticlesEntity> articlesEntity = articlesRepository.findById(articleId);

        if (articlesEntity.isEmpty()) {
            return Optional.empty();
        }

        Optional<ArticleContentEntity> articleContentEntity =
                articleContentRepository.findById(articleId);
        if (articleContentEntity.isEmpty()) {
            return Optional.empty();
        }

        List<IOCEntity> articleIOCs =
                getArticleToIocEntityListMap(List.of(articleId)).get(articleId);

        CategoryEntity categoryEntity =
                getArticleToCategoryEntityMap(List.of(articleId)).get(articleId);

        return Optional.of(
                jsonArticleGenerator.createJsonArticleFromArticleEntity(
                        articleId,
                        articleContentEntity.get(),
                        articlesEntity.get(),
                        articleIOCs,
                        dbEntitiesUtils.getIocTypeIdToNameMap(),
                        categoryEntity));
    }

    @Override
    public Optional<JsonArticleReportResponse> getArticleByLink(String link) {
        String normalizedLink = NormalizeLinks.normalizeLink(link);
        long linkHash = NormalizeLinks.normalizeAndHashLink(link);

        // Get matching hashes
        Map<UUID, ArticlesEntity> articlesEntities =
                streamUtils.getIdObjectMap(
                        articlesRepository.findByHashlink(linkHash), ArticlesEntity::getArticleId);
        Set<UUID> articleIds =
                articlesEntities.values().stream()
                        .map(ArticlesEntity::getArticleId)
                        .collect(Collectors.toSet());

        if (articleIds.isEmpty()) {
            return Optional.empty();
        }

        List<ArticleContentEntity> articleContentEntities =
                articleContentRepository.findAllById(articleIds);

        Optional<ArticleContentEntity> foundEntity =
                articleContentEntities.stream()
                        .filter(
                                articleContent ->
                                        normalizedLink.equals(
                                                NormalizeLinks.normalizeLink(
                                                        articleContent.getLink())))
                        .findFirst();

        if (foundEntity.isEmpty()) {
            return Optional.empty();
        }

        UUID articleId = foundEntity.get().getId();
        List<IOCEntity> articleIOCs =
                getArticleToIocEntityListMap(List.of(articleId)).get(articleId);

        Map<UUID, CategoryEntity> articleIDToCategoryEntityMap =
                getArticleToCategoryEntityMap(articleIds);

        return Optional.of(
                jsonArticleGenerator.createJsonArticleFromArticleEntity(
                        articleId,
                        foundEntity.get(),
                        articlesEntities.get(articleId),
                        articleIOCs,
                        dbEntitiesUtils.getIocTypeIdToNameMap(),
                        articleIDToCategoryEntityMap.get(articleId)));
    }

    @Override
    public boolean addArticlesToReport(int report, String[] articleIds) {
        int totalArticleCount =
                reportArticlesRepository.countByReportArticlesId_ReportIdAndSuggestion(
                        report, false);

        AtomicInteger rankCounter = new AtomicInteger(totalArticleCount);
        List<ReportArticlesEntity> articles = new ArrayList<>();
        for (String articleIdStr : articleIds) {
            try {
                UUID articleId = UUID.fromString(articleIdStr);
                if (articlesRepository.existsById(articleId)) {
                    ReportArticlesId reportArticlesId = new ReportArticlesId(report, articleId);
                    ReportArticlesEntity reportArticlesEntity =
                            new ReportArticlesEntity(
                                    reportArticlesId, (short) rankCounter.getAndIncrement(), false);
                    articles.add(reportArticlesEntity);
                } else {
                    LOGGER.warn("Article with ID {} not found", articleId);
                    return false;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid+ UUID format: {}", articleIdStr);
                return false;
            }
        }
        reportArticlesRepository.saveAll(articles);
        return true;
    }

    @Override
    @Transactional
    public boolean removeArticlesFromReport(int report, String[] articleIds) {
        List<ReportArticlesEntity> articles = new ArrayList<>();
        for (String articleIdStr : articleIds) {
            try {
                UUID articleId = UUID.fromString(articleIdStr);
                ReportArticlesId reportArticlesId = new ReportArticlesId(report, articleId);

                Optional<ReportArticlesEntity> reportArticleOpt =
                        reportArticlesRepository.findById(reportArticlesId);
                if (reportArticleOpt.isPresent()) {
                    articles.add(reportArticleOpt.get());
                } else {
                    LOGGER.warn(
                            "Report-Article association with Report ID {} and Article ID {} not"
                                    + " found",
                            report,
                            articleId);
                    return false;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid+ UUID format: {}", articleIdStr);
                return false;
            }
        }
        reportArticlesRepository.deleteAll(articles);

        // re-rank all other articles
        List<ReportArticlesEntity> reportArticlesEntities =
                reportArticlesRepository
                        .findByReportArticlesId_ReportIdAndSuggestionOrderByArticleRankAsc(
                                report, false);

        short rank = 0;
        for (ReportArticlesEntity articlesEntity : reportArticlesEntities) {
            articlesEntity.setArticleRank(rank++);
        }

        reportArticlesRepository.saveAll(reportArticlesEntities);

        return true;
    }

    @Override
    public boolean editArticleInReport(
            String articleId, String title, String link, String description, Instant publishDate) {
        UUID articleID = UUID.fromString(articleId);
        Optional<ArticleContentEntity> optionalArticle =
                articleContentRepository.findById(articleID);

        if (optionalArticle.isPresent()) {
            ArticleContentEntity articleContentEntity = optionalArticle.get();
            articleContentEntity.setName(title);
            articleContentEntity.setLink(link);
            articleContentEntity.setDescription(description);
            articleContentEntity.setDate(publishDate);

            articleContentRepository.save(articleContentEntity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<JsonArticleReportResponse> getReportSuggestions(int report) {
        if (!reportRepository.existsById(report)) {
            return Collections.emptyList();
        }

        List<ReportArticlesEntity> reportArticlesEntities =
                reportArticlesRepository.findByReportArticlesId_ReportIdAndSuggestion(report, true);
        Set<UUID> articleIds =
                dbEntitiesUtils.getArticleIdFromReportArticleEntities(reportArticlesEntities);
        Map<UUID, ArticlesEntity> articlesEntities =
                dbEntitiesUtils.getArticleIdToArticleEntityMapping(
                        articlesRepository.findAllById(articleIds));
        List<IOCArticlesEntity> iocArticlesEntities =
                iocArticlesEntityRepository.findByIocArticlesId_ArticleIdIn(articleIds);
        Map<UUID, List<IOCEntity>> articleIdToIocEntity =
                dbEntitiesUtils.getArticleIdToIocEntityListMapping(articleIds, iocArticlesEntities);
        Map<UUID, ArticleContentEntity> articleIdToConcentsMap =
                streamUtils.getIdObjectMap(
                        articleContentRepository.findAllById(articleIds),
                        ArticleContentEntity::getId);
        Map<UUID, CategoryEntity> articleIDToCategoryEntityMap =
                getArticleToCategoryEntityMap(articleIds);

        return articleIds.stream()
                .map(
                        articleId ->
                                jsonArticleGenerator.createJsonArticleFromArticleEntity(
                                        articleId,
                                        articleIdToConcentsMap.get(articleId),
                                        articlesEntities.get(articleId),
                                        articleIdToIocEntity.get(articleId),
                                        dbEntitiesUtils.getIocTypeIdToNameMap(),
                                        articleIDToCategoryEntityMap.get(articleId)))
                .toList();
    }

    @Override
    public boolean addReportSuggestion(int report, UUID articleId) {
        if (!reportRepository.existsById(report) || !articlesRepository.existsById(articleId)) {
            return false;
        }

        int totalArticleCount =
                reportArticlesRepository.countByReportArticlesId_ReportIdAndSuggestion(
                        report, true);

        reportArticlesRepository.save(
                new ReportArticlesEntity(
                        new ReportArticlesId(report, articleId), (short) totalArticleCount, true));

        return true;
    }

    @Override
    @Transactional
    public boolean removeReportSuggestion(int report, UUID articleId) {
        if (!reportRepository.existsById(report)) {
            return false;
        }

        if (!reportArticlesRepository
                .existsByReportArticlesId_ReportIdAndReportArticlesId_ArticleIdAndSuggestion(
                        report, articleId, true)) {
            return true;
        }

        List<ReportArticlesEntity> reportArticlesEntities =
                reportArticlesRepository
                        .findByReportArticlesId_ReportIdAndSuggestionOrderByArticleRankAsc(
                                report, true)
                        .stream()
                        .filter(
                                reportArticleEntity ->
                                        !reportArticleEntity
                                                .getReportArticlesId()
                                                .getArticleId()
                                                .equals(articleId))
                        .toList();

        short rank = 0;
        for (ReportArticlesEntity articlesEntity : reportArticlesEntities) {
            articlesEntity.setArticleRank(rank++);
        }

        reportArticlesRepository.deleteById(new ReportArticlesId(report, articleId));
        reportArticlesRepository.saveAll(reportArticlesEntities);
        return true;
    }

    @Override
    public Map<UUID, List<IOCEntity>> getArticleToIocEntityListMap(Collection<UUID> articleIds) {
        List<IOCArticlesEntity> iocArticlesEntities =
                iocArticlesEntityRepository.findByIocArticlesId_ArticleIdIn(articleIds);

        return dbEntitiesUtils.getArticleIdToIocEntityListMapping(articleIds, iocArticlesEntities);
    }

    @Override
    public void addNewArticle(
            UUID articleId, String title, String link, String description, Instant publishDate) {
        // Source 0 is manual entry
        ArticlesEntity articlesEntity =
                new ArticlesEntity(
                        articleId,
                        0,
                        dateService.getCurrentInstant(),
                        publishDate,
                        false,
                        false,
                        NormalizeLinks.normalizeAndHashLink(link));
        ArticleContentEntity articleContentEntity =
                new ArticleContentEntity(articleId, link, title, publishDate, description);

        articlesRepository.save(articlesEntity);
        articleContentRepository.save(articleContentEntity);
    }

    public Map<UUID, CategoryEntity> getArticleToCategoryEntityMap(Collection<UUID> articleIds) {
        Map<UUID, ArticleCategoryEntity> articleIdToCategoryEntityMap =
                streamUtils.getIdObjectMap(
                        articleCategoryRepository.findByArticleCategoryId_ArticleIdIn(articleIds),
                        (articleCategoryEntity ->
                                articleCategoryEntity.getArticleCategoryId().getArticleId()));
        Map<Integer, CategoryEntity> categoryIdToCategoryEntityMap =
                streamUtils.getIdObjectMap(
                        categoryRepository.findAllById(
                                articleIdToCategoryEntityMap.values().stream()
                                        .map(
                                                articleCategoryEntity ->
                                                        articleCategoryEntity
                                                                .getArticleCategoryId()
                                                                .getCategoryId())
                                        .collect(Collectors.toSet())),
                        CategoryEntity::getCategoryId);

        return articleIdToCategoryEntityMap.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey(),
                                entry ->
                                        categoryIdToCategoryEntityMap.get(
                                                entry.getValue()
                                                        .getArticleCategoryId()
                                                        .getCategoryId())));
    }
}
