package me.t65.reportgenapi.reportformatter;

import me.t65.reportgenapi.config.RestApiConfig;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportArticlesEntity;
import me.t65.reportgenapi.utils.ResourceReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class HtmlReportFormatter implements ReportFormatter {

    private final RestApiConfig restApiConfig;
    private final ResourceReader resourceReader;

    @Autowired
    public HtmlReportFormatter(RestApiConfig restApiConfig, ResourceReader resourceReader) {
        this.restApiConfig = restApiConfig;
        this.resourceReader = resourceReader;
    }

    @Override
    public ResponseEntity<?> format(RawReport rawReport) {
        String emailReportTemplateStr;
        String categoryTemplateStr;
        String articleTemplateStr;
        try {
            emailReportTemplateStr =
                    resourceReader.readResourceAsString(restApiConfig.getEmailReportTemplate());
            categoryTemplateStr =
                    resourceReader.readResourceAsString(restApiConfig.getCategoryTemplate());
            articleTemplateStr =
                    resourceReader.readResourceAsString(restApiConfig.getArticleTemplate());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        List<ReportArticlesEntity> reportArticles = new ArrayList<>(rawReport.getReportArticles());
        reportArticles.sort(Comparator.comparingInt(ReportArticlesEntity::getArticleRank));

        Instant lastModifDate = rawReport.getReport().getLastModified();
        String reportType = rawReport.getReport().getReportType().toString();

        // String result = addDashboardLink(emailReportTemplateStr);
        String result = addReportDetails(emailReportTemplateStr, lastModifDate, reportType);
        result = addStatsToTemplate(result);
        result =
                addArticlesToTemplate(
                        result, categoryTemplateStr, articleTemplateStr, rawReport, reportArticles);

        return ResponseEntity.ok(result);
    }

    private String addReportDetails(String template, Instant lastModifDate, String reportType) {
        ZonedDateTime zdt = lastModifDate.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d,yyyy");
        String formattedDate = zdt.format(formatter);
        template = template.replace("{{REPORT-LAST-MODIF-DATE}}", formattedDate);
        return template.replace(
                "{{REPORT-TYPE}}",
                reportType.substring(0, 1).toUpperCase() + reportType.substring(1));
    }

    private String addDashboardLink(String template) {
        return template.replace("{{LINK-TO-DASHBOARD}}", restApiConfig.getDashboardLink());
    }

    // TODO IMPLEMENT STATS WHEN AVAILABLE
    private String addStatsToTemplate(String template) {
        return template.replace("{{STAT-LIST}}", "");
    }

    private String addArticlesToTemplate(
            String reportTemplate,
            String categoryTemplateStr,
            String articleTemplateStr,
            RawReport rawReport,
            List<ReportArticlesEntity> reportArticles) {

        String categoryContent = categoryTemplateStr.replace("{{CATEGORY-TITLE}}", "News Articles");
        categoryContent =
                categoryContent.replace(
                        "{{ARTICLE-LIST}}",
                        generateArticleList(articleTemplateStr, rawReport, reportArticles));

        return reportTemplate.replace("{{CATEGORY-LIST}}", categoryContent);
    }

    private String generateArticleList(
            String template, RawReport rawReport, List<ReportArticlesEntity> reportArticles) {
        StringBuilder result = new StringBuilder();

        for (ReportArticlesEntity reportArticleEntity : reportArticles) {
            UUID articleId = reportArticleEntity.getReportArticlesId().getArticleId();
            result.append(generateArticle(template, rawReport.getArticleContent().get(articleId)));
            result.append("\n");
        }

        return result.toString();
    }

    private String generateArticle(String template, ArticleContentEntity articleContentEntity) {
        String result = template.replace("{{ARTICLE-TITLE}}", articleContentEntity.getName());
        result = result.replace("{{ARTICLE-INFO}}", articleContentEntity.getDescription());
        result = result.replace("{{ARTICLE-LINK}}", articleContentEntity.getLink());

        return result;
    }
}
