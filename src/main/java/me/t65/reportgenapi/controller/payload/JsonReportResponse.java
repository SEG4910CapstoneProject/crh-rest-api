package me.t65.reportgenapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@lombok.Builder
@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
public class JsonReportResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("reportId")
    private int reportId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("reportType")
    private String reportType;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("generatedDate")
    private Instant generatedDate;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("lastModified")
    private LocalDateTime lastModified;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("emailStatus")
    private Boolean emailStatus;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("articles")
    private List<JsonArticleReportResponse> articles;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("stats")
    private List<JsonStatsResponse> stats;
}
