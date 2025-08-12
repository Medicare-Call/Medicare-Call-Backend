package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.report.DailyHealthAnalysisResponse;
import com.example.medicare_call.global.ErrorResponse;
import com.example.medicare_call.service.report.HealthAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
@Tag(name = "Health Analysis", description = "건강 징후 분석 조회 API")
public class HealthAnalysisController {
    private final HealthAnalysisService healthAnalysisService;

    @Operation(
        summary = "날짜별 건강 징후 데이터 조회",
        description = "어르신의 특정 날짜에 기록된 건강 징후 및 분석 결과를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "건강 징후 데이터 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DailyHealthAnalysisResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (날짜 형식 오류 등)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "해당 날짜에 건강 징후 데이터가 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{elderId}/health-analysis")
    public ResponseEntity<DailyHealthAnalysisResponse> getDailyHealthAnalysis(
        @Parameter(description = "어르신 식별자", required = true, example = "1")
        @PathVariable("elderId") Integer elderId,
        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true, example = "2025-07-16")
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("날짜별 건강 징후 데이터 조회 요청: elderId={}, date={}", elderId, date);
        DailyHealthAnalysisResponse response = healthAnalysisService.getDailyHealthAnalysis(elderId, date);
        return ResponseEntity.ok(response);
    }
} 