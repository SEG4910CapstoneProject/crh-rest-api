package me.t65.reportgenapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
public class JsonStatsResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("statisticId")
    private String statisticId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("statisticNumber")
    private int statisticNumber;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("title")
    private String title;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("subtitle")
    private String subtitle;
}
