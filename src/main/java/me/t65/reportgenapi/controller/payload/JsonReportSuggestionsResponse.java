package me.t65.reportgenapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
public class JsonReportSuggestionsResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("articles")
    private List<JsonArticleReportResponse> articles;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("stats")
    private List<JsonStatsResponse> stats;
}
