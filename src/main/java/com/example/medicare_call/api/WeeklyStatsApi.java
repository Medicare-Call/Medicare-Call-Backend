package com.example.medicare_call.api;

import com.example.medicare_call.dto.report.WeeklyReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Weekly Stats", description = "주간 통계 조회 API")
public interface WeeklyStatsApi {

    @Operation(
            summary = "주간 통계 데이터 조회",
            description = "어르신의 주간 통계 데이터를 조회합니다. 식사, 복약, 수면, 심리 상태, 혈당 등의 통계를 제공합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "주간 통계 데이터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WeeklyReportResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (날짜 형식 오류 등)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "어르신 정보를 찾을 수 없음"
            )
    })
    ResponseEntity<WeeklyReportResponse> getWeeklyStats(
            @Parameter(hidden = true) Integer memberId,
            @Parameter(description = "어르신 식별자", required = true, example = "1")
            @PathVariable("elderId") Integer elderId,
            @Parameter(description = "주간 통계를 조회할 시작 날짜 (yyyy-MM-dd)", required = true, example = "2025-07-15")
            @RequestParam("startDate") LocalDate startDate
    );
}
