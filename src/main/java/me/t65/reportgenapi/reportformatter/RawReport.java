package me.t65.reportgenapi.reportformatter;

import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.postgres.entities.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@lombok.Getter
@lombok.AllArgsConstructor
public class RawReport {
    private ReportEntity report;
    private List<ReportArticlesEntity> reportArticles;
    private Map<UUID, ArticlesEntity> articles;
    private Map<UUID, ArticleContentEntity> articleContent;
    private Map<UUID, List<IOCEntity>> articleIOCs;
    private Map<Integer, String> iocTypeIdToTypeString;
    private Map<Integer, List<StatisticEntity>> reportStats;
    private Map<UUID, CategoryEntity> categoryEntityMap;
}
