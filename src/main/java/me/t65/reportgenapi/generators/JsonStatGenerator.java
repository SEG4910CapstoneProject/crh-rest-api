package me.t65.reportgenapi.generators;

import me.t65.reportgenapi.controller.payload.JsonStatsResponse;
import me.t65.reportgenapi.db.postgres.entities.StatisticEntity;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class JsonStatGenerator {
    public List<JsonStatsResponse> createJsonStatsFromStatEntities(
            Collection<StatisticEntity> statList) {
        if (statList == null) {
            return Collections.emptyList();
        }
        return statList.stream().map(this::createJsonStatFromStatEntity).toList();
    }

    public JsonStatsResponse createJsonStatFromStatEntity(StatisticEntity stat) {
        return new JsonStatsResponse(
                stat.getStatisticId().toString(),
                stat.getStatisticNumber(),
                stat.getTitle(),
                stat.getSubtitle());
    }
}
