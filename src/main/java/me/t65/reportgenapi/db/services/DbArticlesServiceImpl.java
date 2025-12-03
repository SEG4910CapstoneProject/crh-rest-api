package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.JsonArticleReportResponseWithTypeIncluded;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.mongo.repository.ArticleContentRepository;
import me.t65.reportgenapi.db.postgres.dto.MonthlyArticleDTO;
import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.entities.id.ArticleTypeEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.generators.JsonArticleGenerator;
import me.t65.reportgenapi.utils.DateService;
import me.t65.reportgenapi.utils.NormalizeLinks;
import me.t65.reportgenapi.utils.StreamUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
    private final ArticleTypeRepository articleTypeRepository;
    private final MonthlyArticlesRepository monthlyArticlesRepository;
    private final JsonArticleGenerator jsonArticleGenerator;
    private final DbEntitiesUtils dbEntitiesUtils;
    private final DateService dateService;
    private final MongoTemplate mongoTemplate;

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
            ArticleTypeRepository articleTypeRepository,
            MonthlyArticlesRepository monthlyArticlesRepository,
            JsonArticleGenerator jsonArticleGenerator,
            DbEntitiesUtils dbEntitiesUtils,
            DateService dateService,
            MongoTemplate mongoTemplate) {
        this.streamUtils = streamUtils;
        this.reportRepository = reportRepository;
        this.articlesRepository = articlesRepository;
        this.reportArticlesRepository = reportArticlesRepository;
        this.articleContentRepository = articleContentRepository;
        this.iocArticlesEntityRepository = iocArticlesEntityRepository;
        this.articleCategoryRepository = articleCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.articleTypeRepository = articleTypeRepository;
        this.monthlyArticlesRepository = monthlyArticlesRepository;
        this.jsonArticleGenerator = jsonArticleGenerator;
        this.dbEntitiesUtils = dbEntitiesUtils;
        this.dateService = dateService;
        this.mongoTemplate = mongoTemplate;
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
    public Optional<JsonArticleReportResponseWithTypeIncluded> getArticleByIdTypeIncluded(
            UUID articleId) {
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

        Optional<ArticleTypeEntity> type = articleTypeRepository.findById(articleId);

        return Optional.of(
                jsonArticleGenerator.createJsonArticleFromArticleEntityTypeIncluded(
                        articleId,
                        articleContentEntity.get(),
                        articlesEntity.get(),
                        articleIOCs,
                        dbEntitiesUtils.getIocTypeIdToNameMap(),
                        categoryEntity,
                        type.get()));
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
        ArticlesEntity articlesEntity =
                new ArticlesEntity(
                        articleId,
                        99, // We are indicating that all manually added articles have a
                        // source_id: "Manually Added"
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

    @Override
    public List<JsonArticleReportResponse> getManualArticles() {
        List<ArticlesEntity> manualArticles = articlesRepository.findBySourceId(99);
        if (manualArticles.isEmpty()) return Collections.emptyList();

        List<JsonArticleReportResponse> responses = new ArrayList<>();
        for (ArticlesEntity entity : manualArticles) {
            getArticleById(entity.getArticleId()).ifPresent(responses::add);
        }
        return responses;
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

    @Override
    public List<JsonArticleReportResponse> getArticlesByType(String type) {
        List<ArticleTypeEntity> articleTypeEntities = articleTypeRepository.findByArticleType(type);

        if (articleTypeEntities.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> articleIds =
                articleTypeEntities.stream()
                        .map(ArticleTypeEntity::getArticleId)
                        .collect(Collectors.toList());

        List<JsonArticleReportResponse> articleResponses = new ArrayList<>();
        for (UUID articleId : articleIds) {
            Optional<JsonArticleReportResponse> articleResponse = getArticleById(articleId);
            articleResponse.ifPresent(articleResponses::add);
        }

        return articleResponses;
    }

    @Override
    public Map<String, List<JsonArticleReportResponse>> getAllArticleTypesWithArticles(int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days);

        List<ArticleTypeEntity> articleTypeEntities = articleTypeRepository.findAll();

        if (articleTypeEntities.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<String> uniqueArticleTypes =
                articleTypeEntities.stream()
                        .map(ArticleTypeEntity::getArticleType)
                        .collect(Collectors.toSet());

        Map<String, List<JsonArticleReportResponse>> articleTypeMap = new HashMap<>();

        for (String articleType : uniqueArticleTypes) {
            List<JsonArticleReportResponse> articlesForType = getArticlesByType(articleType);
            List<JsonArticleReportResponse> filteredArticles =
                    articlesForType.stream()
                            .filter(article -> article.getPublishDate().isAfter(startDate))
                            .sorted(
                                    Comparator.comparing(JsonArticleReportResponse::getPublishDate)
                                            .reversed())
                            .collect(Collectors.toList());
            articleTypeMap.put(articleType, filteredArticles);
        }
        return articleTypeMap;
    }

    @Override
    public List<JsonArticleReportResponseWithTypeIncluded> getAllArticlesWithTypes(int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days);

        List<UUID> article_ids =
                articlesRepository.findAllArticleIdAfterDate(
                        startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<JsonArticleReportResponseWithTypeIncluded> result = new ArrayList<>();
        for (UUID id : article_ids) {
            try {
                JsonArticleReportResponseWithTypeIncluded element =
                        getArticleByIdTypeIncluded(id).get();
                result.add(element);
            } catch (Exception e) {
                LOGGER.error(
                        "For id {}, the article details couldn't be fetched. \n{}",
                        id,
                        e.getMessage());
            }
        }
        return result;
    }

    public Optional<MonthlyArticlesEntity> incrementViewCount(UUID articleId) {

        // Get the article from the main table
        // Optional<ArticlesEntity> articleOpt = articlesRepository.findById(articleId);
        Optional<JsonArticleReportResponse> articleOpt = getArticleById(articleId);
        if (articleOpt.isEmpty()) {
            return Optional.empty(); // Article not found
        }

        LocalDate datePublished = articleOpt.get().getPublishDate();
        String title = articleOpt.get().getTitle();

        // Check if the article exists in the monthly table
        MonthlyArticlesEntity monthlyArticle =
                monthlyArticlesRepository
                        .findByArticleId(articleId)
                        .orElseGet(
                                () -> {
                                    // Create a new entry if it doesn't exist
                                    MonthlyArticlesEntity newEntry = new MonthlyArticlesEntity();
                                    newEntry.setArticleId(articleId);
                                    newEntry.setDatePublished(datePublished);
                                    newEntry.setViewCount(0);
                                    newEntry.setArticleOfNote(false);
                                    newEntry.setTitle(title);
                                    return monthlyArticlesRepository.save(newEntry);
                                });

        // Increment view count
        monthlyArticle.incrementViewCount();
        return Optional.of(monthlyArticlesRepository.save(monthlyArticle));
    }

    public Optional<MonthlyArticlesEntity> toggleArticleOfNote(UUID articleId) {

        Optional<JsonArticleReportResponse> articleOpt = getArticleById(articleId);

        if (articleOpt.isEmpty()) {
            return Optional.empty(); // Article not found
        }

        LocalDate datePublished = articleOpt.get().getPublishDate();
        String title = articleOpt.get().getTitle();

        // Check if the article exists in the monthly table
        MonthlyArticlesEntity monthlyArticle =
                monthlyArticlesRepository
                        .findByArticleId(articleId)
                        .orElseGet(
                                () -> {
                                    // Create a new entry if it doesn't exist
                                    MonthlyArticlesEntity newEntry = new MonthlyArticlesEntity();
                                    newEntry.setArticleId(articleId);
                                    newEntry.setDatePublished(datePublished);
                                    newEntry.setViewCount(0);
                                    newEntry.setArticleOfNote(false);
                                    newEntry.setTitle(title);
                                    return monthlyArticlesRepository.save(newEntry);
                                });

        // Toggle the article of note
        monthlyArticle.setArticleOfNote(!monthlyArticle.isArticleOfNote());
        return Optional.of(monthlyArticlesRepository.save(monthlyArticle));
    }

    public List<MonthlyArticleDTO> getTop10Articles() {
        // Get the top 10 articles sorted by view count
        List<MonthlyArticlesEntity> topArticles =
                monthlyArticlesRepository.findTop10ByOrderByViewCountDesc();

        // List to store the final responses
        List<MonthlyArticleDTO> articleResponses = new ArrayList<>();

        for (MonthlyArticlesEntity monthlyArticle : topArticles) {
            // Fetch the article details by articleId using getArticleById
            Optional<JsonArticleReportResponse> articleResponse =
                    getArticleById(monthlyArticle.getArticleId());

            // If the article is found, add it to the list
            articleResponse.ifPresent(
                    response -> {
                        // Create a new MonthlyArticleResponse that includes the URL and view count
                        // (optional)
                        MonthlyArticleDTO finalResponse =
                                new MonthlyArticleDTO(
                                        response.getLink(), // URL
                                        Optional.of(
                                                monthlyArticle
                                                        .getViewCount()), // View count wrapped in
                                        response.getTitle(),
                                        UUID.fromString(response.getArticleId()));
                        articleResponses.add(finalResponse);
                    });
        }

        return articleResponses;
    }

    @Override
    public List<JsonArticleReportResponse> getArticlesOfNote() {
        // Get the "articles of note" from the monthly table
        List<MonthlyArticlesEntity> articlesOfNote =
                monthlyArticlesRepository.findByIsArticleOfNoteTrue();

        if (articlesOfNote.isEmpty()) {
            return Collections.emptyList();
        }

        List<JsonArticleReportResponse> responses = new ArrayList<>();

        for (MonthlyArticlesEntity monthlyArticle : articlesOfNote) {
            // Fetch full article details
            getArticleById(monthlyArticle.getArticleId()).ifPresent(responses::add);
        }

        return responses;
    }

    @Autowired private UserFavouriteRepository userFavouriteRepository;

    @Override
    public boolean addFavourite(Long userId, UUID articleId) {
        // Check article exists
        if (!articlesRepository.existsById(articleId)) {
            return false;
        }

        // Prevent duplicate entries
        if (userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId)) {
            return true;
        }

        UserFavouriteEntity favourite = new UserFavouriteEntity(userId, articleId, Instant.now());
        userFavouriteRepository.save(favourite);
        return true;
    }

    @Override
    @Transactional
    public void removeFavourite(Long userId, UUID articleId) {
        if (userFavouriteRepository.existsByUserIdAndArticleId(userId, articleId)) {
            userFavouriteRepository.deleteByUserIdAndArticleId(userId, articleId);
        }
    }

    @Override
    public List<JsonArticleReportResponse> getFavouritesForUser(Long userId) {
        List<UserFavouriteEntity> favourites = userFavouriteRepository.findByUserId(userId);

        if (favourites.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> articleIds = favourites.stream().map(UserFavouriteEntity::getArticleId).toList();

        List<JsonArticleReportResponse> result = new ArrayList<>();
        for (UUID articleId : articleIds) {
            getArticleById(articleId).ifPresent(result::add);
        }

        return result;
    }

    @Override
    public boolean ingestFromUrl(String link, String title, String description) {
        LOGGER.info("Starting ingestion: link='{}'", link);
        try {
            if (link == null || link.isBlank()) {
                LOGGER.warn("Link is null or blank, aborting ingestion.");
                return false;
            }

            Optional<JsonArticleReportResponse> existing = getArticleByLink(link);
            if (existing.isPresent()) {
                LOGGER.info("Article already exists: {}", link);
                return false;
            }

            String safeTitle = (title == null || title.isBlank()) ? "Untitled Article" : title;
            String safeDescription =
                    (description == null || description.isBlank())
                            ? "No description provided."
                            : description;
            LocalDate publishDate = LocalDate.now();

            LOGGER.debug("Cleaned title='{}', desc length={}", safeTitle, safeDescription.length());

            UUID uuid = UUID.randomUUID();
            LOGGER.debug("Generated UUID {}", uuid);

            addNewArticle(
                    uuid,
                    safeTitle,
                    link,
                    safeDescription,
                    publishDate.atStartOfDay().toInstant(ZoneOffset.UTC));

            LOGGER.info("Successfully ingested and saved article: {}", link);
            return true;

        } catch (Exception e) {
            LOGGER.error("Ingestion failed for link '{}': {}", link, e.getMessage(), e);
            throw new RuntimeException("Error ingesting article: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteManualArticle(UUID articleId) {
        Optional<ArticlesEntity> articleOpt = articlesRepository.findById(articleId);
        if (articleOpt.isEmpty() || articleOpt.get().getSourceId() != 99) {
            return false; // not a manual article
        }
        articlesRepository.deleteById(articleId);
        articleContentRepository.deleteById(articleId);
        return true;
    }

    @Override
    public boolean updateManualArticle(
            UUID articleId, String title, String link, String description, Instant publishDate) {
        Optional<ArticlesEntity> articleOpt = articlesRepository.findById(articleId);
        if (articleOpt.isEmpty() || articleOpt.get().getSourceId() != 99) {
            return false; // not a manual article
        }

        try {
            // Check for duplicate link
            Optional<JsonArticleReportResponse> existing = getArticleByLink(link);
            if (existing.isPresent()
                    && !existing.get().getArticleId().equals(articleId.toString())) {
                return false; // another article already uses this link
            }

            // Update database
            ArticleContentEntity content =
                    articleContentRepository
                            .findById(articleId)
                            .orElseThrow(() -> new RuntimeException("Article content not found"));

            content.setName(title);
            content.setLink(link);
            content.setDescription(description);
            articleContentRepository.save(content);

            ArticlesEntity meta = articleOpt.get();
            meta.setHashlink(NormalizeLinks.normalizeAndHashLink(link));
            meta.setDatePublished(publishDate);
            articlesRepository.save(meta);

            return true;
        } catch (Exception e) {
            LOGGER.error("Error updating manual article {}: {}", articleId, e.getMessage());
            throw new RuntimeException("Error updating manual article: " + e.getMessage());
        }
    }

    /**
     * Performs a vector search using manual Cosine Similarity ($cosSim) aggregation, since
     * $vectorSearch is unavailable in MongoDB Community Edition.
     *
     * @param queryEmbedding The vector embedding of the user's query.
     * @param topK The number of top articles to retrieve.
     * @return A list of the top matching ArticleContentEntity objects.
     */
    public List<ArticleContentEntity> findRelatedArticlesByVector(
            List<Double> queryEmbedding, int topK) {
        // Fetch candidate articles
        List<ArticleContentEntity> candidates =
                mongoTemplate.find(new Query(), ArticleContentEntity.class);

        // Compute cosine similarity for each candidate
        List<Pair<ArticleContentEntity, Double>> scored =
                candidates.stream()
                        .map(
                                article ->
                                        Pair.of(
                                                article,
                                                cosineSimilarity(
                                                        article.getEmbedding(), queryEmbedding)))
                        .sorted(
                                (p1, p2) ->
                                        Double.compare(p2.getRight(), p1.getRight())) // descending
                        .collect(Collectors.toList());

        // Return top K articles
        return scored.stream().limit(topK).map(Pair::getLeft).collect(Collectors.toList());
    }

    // Helper method to compute cosine similarity
    private double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) return 0.0;

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            double a = vec1.get(i);
            double b = vec2.get(i);
            dot += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // delete articles related to a report in the report article mappings
    public boolean deleteReportArticles(int reportId) {
        try {
            if (reportArticlesRepository.existsByReportArticlesIdReportId(reportId)) {
                reportArticlesRepository.deleteAllByReportId(reportId);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("an exception occured in deleteReportArticles: {}", e.toString());

            return false;
        }
    }
}
