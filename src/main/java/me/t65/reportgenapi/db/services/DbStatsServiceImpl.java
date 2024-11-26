package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonStatsResponse;
import me.t65.reportgenapi.db.postgres.entities.ReportStatisticsEntity;
import me.t65.reportgenapi.db.postgres.entities.StatisticEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ReportStatisticsId;
import me.t65.reportgenapi.db.postgres.repository.*;
import me.t65.reportgenapi.generators.JsonStatGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DbStatsServiceImpl implements DbStatsService {
    private final ReportRepository reportRepository;
    private final StatisticsRepository statisticsRepository;
    private final ReportStatisticsRepository reportStatisticsRepository;

    private final DbEntitiesUtils dbEntitiesUtils;
    private final JsonStatGenerator jsonStatGenerator;

    @Autowired
    public DbStatsServiceImpl(
            ReportRepository reportRepository,
            StatisticsRepository statisticsRepository,
            ReportStatisticsRepository reportStatisticsRepository,
            DbEntitiesUtils dbEntitiesUtils,
            JsonStatGenerator jsonStatGenerator) {
        this.reportRepository = reportRepository;
        this.statisticsRepository = statisticsRepository;
        this.reportStatisticsRepository = reportStatisticsRepository;
        this.dbEntitiesUtils = dbEntitiesUtils;
        this.jsonStatGenerator = jsonStatGenerator;
    }

    @Override
    public boolean addStatsToReport(int report, String[] statisticIds) {
        List<ReportStatisticsEntity> statistics = new ArrayList<>();
        for (String statisticIdsStr : statisticIds) {
            try {
                UUID statId = UUID.fromString(statisticIdsStr);
                if (statisticsRepository.existsById(statId)) {
                    ReportStatisticsId reportStatisticsId = new ReportStatisticsId(report, statId);
                    ReportStatisticsEntity reportStatisticsEntity =
                            new ReportStatisticsEntity(reportStatisticsId, false);
                    statistics.add(reportStatisticsEntity);
                } else {
                    System.out.println("Stat with ID " + statisticIdsStr + " not found");
                    return false;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format: " + statisticIdsStr);
                return false;
            }
        }
        reportStatisticsRepository.saveAll(statistics);
        return true;
    }

    @Override
    public boolean removeStatsFromReport(int report, String[] statisticIds) {
        List<ReportStatisticsId> statistics = new ArrayList<>();
        for (String statisticIdStr : statisticIds) {
            try {
                UUID statId = UUID.fromString(statisticIdStr);
                ReportStatisticsId reportStatisticsId = new ReportStatisticsId(report, statId);
                if (reportStatisticsRepository.existsById(reportStatisticsId)) {
                    statistics.add(reportStatisticsId);
                } else {
                    System.out.println(
                            "Report-Statistic association with Report ID "
                                    + report
                                    + " and StatId ID "
                                    + statisticIdStr
                                    + " not found");
                    return false;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format: " + statisticIdStr);
                return false;
            }
        }
        reportStatisticsRepository.deleteAllById(statistics);
        return true;
    }

    @Override
    public boolean editStat(UUID statisticId, int statisticNumber, String title, String subtitle) {
        StatisticEntity statisticEntity =
                new StatisticEntity(statisticId, statisticNumber, title, subtitle);
        try {
            statisticsRepository.save(statisticEntity);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean addStat(UUID statisticId, int statisticNumber, String title, String subtitle) {
        return editStat(statisticId, statisticNumber, title, subtitle);
    }

    @Override
    public List<JsonStatsResponse> getReportSuggestions(int report) {
        if (!reportRepository.existsById(report)) {
            return Collections.emptyList();
        }

        List<ReportStatisticsEntity> reportStatisticsEntities =
                reportStatisticsRepository.findByReportStatisticsId_ReportIdAndSuggestion(
                        report, true);
        Map<UUID, StatisticEntity> statisticEntityMap =
                dbEntitiesUtils.getStatIdToStatEntityMap(reportStatisticsEntities);

        return jsonStatGenerator.createJsonStatsFromStatEntities(statisticEntityMap.values());
    }

    @Override
    public boolean addReportSuggestion(int report, UUID statId) {
        if (!reportRepository.existsById(report) || !statisticsRepository.existsById(statId)) {
            return false;
        }

        reportStatisticsRepository.save(
                new ReportStatisticsEntity(new ReportStatisticsId(report, statId), true));

        return true;
    }

    @Override
    public boolean removeReportSuggestion(int report, UUID statId) {
        if (!reportRepository.existsById(report)) {
            return false;
        }

        if (!reportStatisticsRepository
                .existsByReportStatisticsId_ReportIdAndReportStatisticsId_StatisticIdAndSuggestion(
                        report, statId, true)) {
            return true;
        }

        reportStatisticsRepository.deleteById(new ReportStatisticsId(report, statId));
        return true;
    }
}
