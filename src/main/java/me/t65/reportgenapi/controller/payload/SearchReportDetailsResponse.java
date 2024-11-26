package me.t65.reportgenapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Builder
@lombok.ToString
@lombok.EqualsAndHashCode
public class SearchReportDetailsResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("reportId")
    private int reportId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("reportType")
    private String reportType;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("generatedDate")
    private LocalDate generatedDate;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("lastModified")
    private LocalDateTime lastModified;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("emailStatus")
    private Boolean emailStatus;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "List of article titles in the report")
    @JsonProperty("articleTitles")
    private List<String> articleTitles;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("iocs")
    private List<JsonIocResponse> iocs;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("stats")
    private List<JsonStatsResponse> stats;
}
