package com.example.medicare_call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bulk 처리 응답")
public class ElderBulkResponse {

    @Schema(description = "전체 처리된 어르신 수", example = "10")
    private int totalCount;

    @Schema(description = "성공한 어르신들의 정보")
    private List<ElderResult> successResults;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "성공한 어르신 정보")
    public static class ElderResult {

        @Schema(description = "어르신 ID", example = "1")
        private Integer elderId;

        @Schema(description = "어르신 이름", example = "홍길동")
        private String elderName;
    }
}