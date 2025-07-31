package com.example.medicare_call.controller;

import com.example.medicare_call.dto.HealthDataExtractionRequest;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.service.OpenAiHealthDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/health-data")
@RequiredArgsConstructor
@Tag(name = "Health Data", description = "건강 데이터 추출 API")
public class HealthDataController {
    
    private final OpenAiHealthDataService openAiHealthDataService;

    @Operation(
        summary = "건강 데이터 추출",
        description = "통화 내용에서 건강 관련 데이터를 추출합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "건강 데이터 추출 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HealthDataExtractionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @PostMapping("/extract")
    public ResponseEntity<HealthDataExtractionResponse> extractHealthData(
        @RequestBody @Schema(
            description = "건강 데이터 추출 요청",
            example = """
            {
              "transcriptionText": "오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요.",
              "callDate": "2024-01-01"
            }
            """
        ) HealthDataExtractionRequest request
    ) {
        log.info("건강 데이터 추출 요청: {}", request);
        HealthDataExtractionResponse response = openAiHealthDataService.extractHealthData(request);
        return ResponseEntity.ok(response);
    }
} 