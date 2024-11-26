package me.t65.reportgenapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
public class ArticleByLinkRequest {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("link")
    private String link;
}
