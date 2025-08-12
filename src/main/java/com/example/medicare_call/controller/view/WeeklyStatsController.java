package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.WeeklyStatsResponse;
import com.example.medicare_call.service.report.WeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
@Tag(name = "Weekly Stats", description = "주간 통계 조회 API")
public class WeeklyStatsController {

    private final WeeklyReportService weeklyReportService;

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
                schema = @Schema(implementation = WeeklyStatsResponse.class)
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
    @GetMapping("/{elderId}/weekly-stats")
    public ResponseEntity<WeeklyStatsResponse> getWeeklyStats(
        @Parameter(description = "어르신 식별자", required = true, example = "1")
        @PathVariable("elderId") Integer elderId,

        @Parameter(description = "주간 통계를 조회할 시작 날짜 (yyyy-MM-dd)", required = true, example = "2025-07-15")
        @RequestParam("startDate") LocalDate startDate
    ) {
        log.info("주간 통계 데이터 조회 요청: elderId={}, startDate={}", elderId, startDate);

        WeeklyStatsResponse response = weeklyReportService.getWeeklyReport(elderId, startDate);

        return ResponseEntity.ok(response);
    }
} 