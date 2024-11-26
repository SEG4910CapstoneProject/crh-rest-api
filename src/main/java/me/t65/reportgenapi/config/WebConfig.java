package me.t65.reportgenapi.config;

import me.t65.reportgenapi.config.converters.*;
import me.t65.reportgenapi.db.postgres.entities.ReportType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired private RestApiConfig restApiConfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(restApiConfig.getAllowedOriginsPatterns().split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "OPTIONS", "DELETE");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addParser(new CaseInsensitiveEnumParser<ReportType>(ReportType.class));
    }
}
