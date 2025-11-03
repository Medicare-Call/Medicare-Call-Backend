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
@Schema(description = "어르신 건강정보 일괄 생성 요청")
public class BulkElderHealthInfoCreateRequest {

    @NotEmpty(message = "어르신 건강정보 목록은 비어있을 수 없습니다")
    @Schema(description = "어르신 건강정보 일괄 생성 목록", required = true)
    private List<@Valid @NotNull ElderHealthInfoCreateRequestWithElderId> healthInfos;
}