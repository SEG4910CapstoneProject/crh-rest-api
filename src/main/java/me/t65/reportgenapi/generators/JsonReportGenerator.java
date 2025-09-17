package me.t65.reportgenapi.generators;

import me.t65.reportgenapi.controller.payload.*;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.postgres.entities.ArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.IOCEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportEntity;
import me.t65.reportgenapi.reportformatter.RawReport;
import me.t65.reportgenapi.utils.DateUtils;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class JsonReportGenerator {

    private final JsonArticleGenerator jsonArticleGenerator;
    private final JsonStatGenerator jsonStatGenerator;

    public JsonReportGenerator(
            JsonArticleGenerator jsonArticleGenerator, JsonStatGenerator jsonStatGenerator) {
        this.jsonArticleGenerator = jsonArticleGenerator;
        this.jsonStatGenerator = jsonStatGenerator;
    }

    public JsonReportResponse generateFromRawReport(RawReport rawReport) {
        List<ReportArticlesEntity> reportArticles = new ArrayList<>(rawReport.getReportArticles());
        reportArticles.sort(Comparator.comparingInt(ReportArticlesEntity::getArticleRank));

        List<JsonArticleReportResponse> articles =
                reportArticles.stream()
                        .sorted(Comparator.comparing(o -> o.getReportArticlesId().getArticleId()))
                        .map(
                                reportArticle -> {
                                    UUID articleID =
                                            reportArticle.getReportArticlesId().getArticleId();
                                    ArticlesEntity articlesEntity =
                                            rawReport
                                                    .getArticles()
                                                    .get(
                                                            reportArticle
                                                                    .getReportArticlesId()
                                                                    .getArticleId());
                                    return jsonArticleGenerator.createJsonArticleFromArticleEntity(
                                            articleID,
                                            rawReport.getArticleContent().get(articleID),
                                            articlesEntity,
                                            rawReport.getArticleIOCs().get(articleID),
                                            rawReport.getIocTypeIdToTypeString(),
                                            rawReport.getCategoryEntityMap().get(articleID));
                                })
                        .toList();

        return JsonReportResponse.builder()
                .reportId(rawReport.getReport().getReportId())
                .reportType(rawReport.getReport().getReportType().toString())
                .emailStatus(rawReport.getReport().getEmailStatus())
                .lastModified(
                        DateUtils.getLastModifiedFromInstant(
                                rawReport.getReport().getLastModified()))
                .generatedDate(rawReport.getReport().getGenerateDate())
                .articles(articles)
                .stats(
                        jsonStatGenerator.createJsonStatsFromStatEntities(
                                rawReport
                                        .getReportStats()
                                        .get(rawReport.getReport().getReportId())))
                .build();
    }

    public SearchReportDetailsResponse generateShortDetails(
            ReportEntity reportEntity,
            List<ArticleContentEntity> articleContentEntities,
            List<IOCEntity> iocEntities,
            Map<Integer, String> iocTypeIdToTypeString) {
        List<String> articleTitles =
                articleContentEntities.stream().map(ArticleContentEntity::getName).toList();

        return SearchReportDetailsResponse.builder()
                .reportId(reportEntity.getReportId())
                .reportType(reportEntity.getReportType().toString())
                .emailStatus(reportEntity.getEmailStatus())
                .generatedDate(LocalDateTime.ofInstant(reportEntity.getGenerateDate(), ZoneOffset.UTC))
                .lastModified(DateUtils.getLastModifiedFromInstant(reportEntity.getLastModified()))
                .iocs(
                        jsonArticleGenerator.createJsonIocFromIocEntity(
                                iocEntities, iocTypeIdToTypeString))
                .articleTitles(articleTitles)
                .build();
    }
}
