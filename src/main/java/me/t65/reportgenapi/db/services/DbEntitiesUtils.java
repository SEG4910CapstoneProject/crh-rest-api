package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.db.postgres.entities.*;
import me.t65.reportgenapi.db.postgres.repository.IOCEntityRepository;
import me.t65.reportgenapi.db.postgres.repository.IOCTypeEntityRepository;
import me.t65.reportgenapi.db.postgres.repository.StatisticsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbEntitiesUtils {

    private final StatisticsRepository statisticsRepository;
    private final IOCTypeEntityRepository iocTypeEntityRepository;
    private final IOCEntityRepository iocEntityRepository;

    @Autowired
    public DbEntitiesUtils(
            IOCTypeEntityRepository iocTypeEntityRepository,
            IOCEntityRepository iocEntityRepository,
            StatisticsRepository statisticsRepository) {
        this.iocEntityRepository = iocEntityRepository;
        this.iocTypeEntityRepository = iocTypeEntityRepository;
        this.statisticsRepository = statisticsRepository;
    }

    public Map<Integer, String> getIocTypeIdToNameMap() {
        return iocTypeEntityRepository.findAll().stream()
                .collect(Collectors.toMap(IOCTypeEntity::getIocTypeId, IOCTypeEntity::getName));
    }

    public Set<UUID> getArticleIdFromReportArticleEntities(
            List<ReportArticlesEntity> reportArticlesEntities) {
        return reportArticlesEntities.stream()
                .map(reportArticle -> reportArticle.getReportArticlesId().getArticleId())
                .collect(Collectors.toSet());
    }

    public Set<UUID> getArticleIdFromReportArticleEntities(
            List<ReportArticlesEntity> reportArticlesEntities, int reportId) {
        return reportArticlesEntities.stream()
                .filter(
                        reportArticle ->
                                reportArticle.getReportArticlesId().getReportId() == reportId)
                .map(reportArticle -> reportArticle.getReportArticlesId().getArticleId())
                .collect(Collectors.toSet());
    }

    public Map<UUID, ArticlesEntity> getArticleIdToArticleEntityMapping(
            List<ArticlesEntity> articlesEntities) {
        return articlesEntities.stream()
                .collect(Collectors.toMap(ArticlesEntity::getArticleId, entry -> entry));
    }

    public Map<UUID, List<IOCEntity>> getArticleIdToIocEntityListMapping(
            Collection<UUID> articleIds, List<IOCArticlesEntity> iocArticlesEntities) {
        Map<UUID, List<Integer>> articleToIocListMapping =
                getArticleToIocListMapping(iocArticlesEntities);
        Map<Integer, IOCEntity> iocIdToIocEntityMap = getIocIdToIocEntityMap(iocArticlesEntities);

        return articleToIocListMapping.entrySet().stream()
                // Only take iocs for this report
                .filter(entry -> articleIds.contains(entry.getKey()))
                // Convert id to entities
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry ->
                                        entry.getValue().stream()
                                                .map(iocIdToIocEntityMap::get)
                                                .toList()));
    }

    public Map<Integer, List<UUID>> getReportToStatListMapping(
            List<ReportStatisticsEntity> statReportEntities) {
        return statReportEntities.stream()
                .collect(
                        Collectors.groupingBy(
                                // Key is the UUID of the article
                                statReportEntity ->
                                        statReportEntity.getReportStatisticsId().getReportId(),
                                // Value is a list containing the ioc ids
                                Collectors.mapping(
                                        entity -> entity.getReportStatisticsId().getStatisticId(),
                                        Collectors.toList())));
    }

    public Map<UUID, StatisticEntity> getStatIdToStatEntityMap(
            List<ReportStatisticsEntity> statReportEntities) {
        List<UUID> statIds =
                statReportEntities.stream()
                        .map(statReport -> statReport.getReportStatisticsId().getStatisticId())
                        .distinct()
                        .toList();
        return statisticsRepository.findAllById(statIds).stream()
                .collect(Collectors.toMap(StatisticEntity::getStatisticId, entity -> entity));
    }

    private Map<Integer, IOCEntity> getIocIdToIocEntityMap(
            List<IOCArticlesEntity> iocArticlesEntities) {
        List<Integer> iocIds =
                iocArticlesEntities.stream()
                        .map(iocArticle -> iocArticle.getIocArticlesId().getIocID())
                        .distinct()
                        .toList();
        return iocEntityRepository.findAllById(iocIds).stream()
                .collect(Collectors.toMap(IOCEntity::getIocID, entity -> entity));
    }

    private Map<UUID, List<Integer>> getArticleToIocListMapping(
            List<IOCArticlesEntity> iocArticlesEntities) {
        return iocArticlesEntities.stream()
                .collect(
                        Collectors.groupingBy(
                                // Key is the UUID of the article
                                iocArticlesEntity ->
                                        iocArticlesEntity.getIocArticlesId().getArticleId(),
                                // Value is a list containing the ioc ids
                                Collectors.mapping(
                                        entity -> entity.getIocArticlesId().getIocID(),
                                        Collectors.toList())));
    }
}
