package com.example.medicare_call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "어르신 정보와 건강 정보를 포함한 일괄 등록 요청")
public class ElderBulkRequest {

    @Schema(description = "어르신 기본 정보")
    @NotNull(message = "어르신 정보는 필수입니다.")
    @Valid
    private ElderRegisterRequest elder;

    @Schema(description = "어르신 건강 정보")
    @Valid
    private ElderHealthInfoCreateRequest healthInfo;
}