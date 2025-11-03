package com.example.medicare_call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "어르신 일괄 등록 요청")
public class BulkElderRegisterRequest {

    @NotEmpty(message = "어르신 목록은 비어있을 수 없습니다")
    @Schema(description = "어르신 일괄 등록 목록", required = true)
    private List<@Valid @NotNull ElderRegisterRequest> elders;
}