package me.t65.reportgenapi.config;

import me.t65.reportgenapi.reportformatter.HtmlReportFormatter;
import me.t65.reportgenapi.reportformatter.JsonReportFormatter;
import me.t65.reportgenapi.reportformatter.ReportFormatter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@lombok.Getter
public class RestApiConfig {
    @Value("classpath:emailReportTemplate/emailReport.html")
    private Resource emailReportTemplate;

    @Value("classpath:emailReportTemplate/category.html")
    private Resource categoryTemplate;

    @Value("classpath:emailReportTemplate/article.html")
    private Resource articleTemplate;

    @Value("${api.dashboard-link}")
    private String dashboardLink;

    @Value("${server.allowedOriginsPatterns}")
    private String allowedOriginsPatterns;

    @Bean
    @Qualifier("formatMapper")
    public Map<String, ReportFormatter> reportFormatterMap(
            HtmlReportFormatter htmlReportFormatter, JsonReportFormatter jsonReportFormatter) {
        HashMap<String, ReportFormatter> map = new HashMap<>();

        map.put("html", htmlReportFormatter);
        map.put("json", jsonReportFormatter);

        return map;
    }
}
