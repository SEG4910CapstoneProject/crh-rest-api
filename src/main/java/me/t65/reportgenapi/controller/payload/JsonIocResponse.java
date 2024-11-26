package me.t65.reportgenapi.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
public class JsonIocResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("iocId")
    private int iocId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("iocTypeId")
    private int iocTypeId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("iocTypeName")
    private String iocTypeName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("value")
    private String value;
}
