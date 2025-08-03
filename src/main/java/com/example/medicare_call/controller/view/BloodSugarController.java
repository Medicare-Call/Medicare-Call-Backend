package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.WeeklyBloodSugarResponse;
import com.example.medicare_call.service.WeeklyBloodSugarService;
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
@Tag(name = "Blood Sugar", description = "혈당 데이터 조회 API")
public class BloodSugarController {

    private final WeeklyBloodSugarService weeklyBloodSugarService;

    @Operation(
        summary = "주간 혈당 데이터 조회",
        description = "어르신의 주간 혈당 기록을 식사 시간대(공복/식후) 기준으로 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주간 혈당 데이터 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WeeklyBloodSugarResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (날짜 형식 오류, 잘못된 타입 등)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "어르신 정보를 찾을 수 없음"
        )
    })
    @GetMapping("/{elderId}/blood-sugar/weekly")
    public ResponseEntity<WeeklyBloodSugarResponse> getWeeklyBloodSugar(
        @Parameter(description = "어르신 식별자", required = true, example = "1")
        @PathVariable("elderId") Integer elderId,

        @Parameter(description = "주간 조회 시작일 (yyyy-MM-dd)", required = true, example = "2025-07-09")
        @RequestParam("startDate") String startDate,

        @Parameter(description = "식사 시간대", required = true, example = "BEFORE_MEAL", 
                  schema = @Schema(allowableValues = {"BEFORE_MEAL", "AFTER_MEAL"}))
        @RequestParam("type") String type
    ) {
        log.info("주간 혈당 데이터 조회 요청: elderId={}, startDate={}, type={}", elderId, startDate, type);

        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, startDate, type);

        return ResponseEntity.ok(response);
    }
} 