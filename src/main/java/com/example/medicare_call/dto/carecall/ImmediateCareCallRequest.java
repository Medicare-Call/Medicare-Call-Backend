package com.example.medicare_call.dto.carecall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "즉시 케어콜 요청 DTO")
public class ImmediateCareCallRequest {

    @NotNull
    @Schema(description = "어르신 ID", example = "1")
    private Long elderId;

    @NotNull
    @Schema(description = "케어콜 옵션", example = "FIRST")
    private CareCallOption careCallOption;

    public enum CareCallOption {
        FIRST, SECOND, THIRD
    }
}
