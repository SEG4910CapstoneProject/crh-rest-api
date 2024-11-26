package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.controller.payload.JsonStatsResponse;

import java.util.List;
import java.util.UUID;

public interface DbStatsService {
    boolean addStatsToReport(int report, String[] statisticIds);

    boolean removeStatsFromReport(int report, String[] statisticIds);

    boolean editStat(UUID statisticId, int statisticNumber, String title, String subtitle);

    boolean addStat(UUID statisticId, int statisticNumber, String title, String subtitle);

    List<JsonStatsResponse> getReportSuggestions(int report);

    boolean addReportSuggestion(int report, UUID statId);

    boolean removeReportSuggestion(int report, UUID statId);
}
