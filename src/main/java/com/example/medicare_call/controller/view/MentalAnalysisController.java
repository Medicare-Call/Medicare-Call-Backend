package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.DailyMentalAnalysisResponse;
import com.example.medicare_call.service.MentalAnalysisService;
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

@Slf4j
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
@Tag(name = "Mental Analysis", description = "심리 상태 분석 조회 API")
public class MentalAnalysisController {
    
    private final MentalAnalysisService mentalAnalysisService;
    
    @Operation(
        summary = "날짜별 심리 상태 데이터 조회",
        description = "어르신의 특정 날짜에 기록된 심리 상태 요약을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "심리 상태 데이터 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DailyMentalAnalysisResponse.class)
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
    @GetMapping("/{elderId}/mental-analysis")
    public ResponseEntity<DailyMentalAnalysisResponse> getDailyMentalAnalysis(
        @Parameter(description = "어르신 식별자", required = true, example = "1")
        @PathVariable("elderId") Integer elderId,
        
        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true, example = "2025-07-16")
        @RequestParam("date") String date
    ) {
        log.info("날짜별 심리 상태 데이터 조회 요청: elderId={}, date={}", elderId, date);
        
        DailyMentalAnalysisResponse response = mentalAnalysisService.getDailyMentalAnalysis(elderId, date);
        
        return ResponseEntity.ok(response);
    }
} 