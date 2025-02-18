package me.t65.reportgenapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
public class JsonArticleReportResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("articleId")
    private String articleId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("title")
    private String title;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("description")
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("category")
    private String category;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("link")
    private String link;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("iocs")
    private List<JsonIocResponse> iocs;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("publishDate")
    private LocalDate publishDate;
}
