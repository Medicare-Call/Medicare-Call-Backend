package com.example.medicare_call.controller;

import com.example.medicare_call.api.HealthDataApi;
import com.example.medicare_call.api.HealthDataTestApi;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.HealthDataTestRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.service.data_processor.CareCallAnalysisResultSaveService;
import com.example.medicare_call.service.data_processor.CareCallAnalysisService;

import com.example.medicare_call.util.TestDataGenerator;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/health-data")
@RequiredArgsConstructor
@Tag(name = "Health Data", description = "건강 데이터 추출 API")
public class HealthDataController implements HealthDataApi, HealthDataTestApi {

    private final CareCallAnalysisService careCallAnalysisService;
    private final TestDataGenerator testDataGenerator;
    private final CareCallAnalysisResultSaveService careCallAnalysisResultSaveService;

    @Override
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
        HealthDataExtractionResponse response = careCallAnalysisService.extractHealthData(
                request.getCallDate(),
                request.getTranscriptionText(),
                Collections.emptyList()
        );
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/extract/test")
    public ResponseEntity<CareCallRecord> saveTestHealthData(
            @RequestBody @Schema(
                    description = "건강 데이터 추출 테스트",
                    example = """
            {
              "elderId": 1,
              "settingId": 1,
              "transcriptionText": "오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요. 기분도 좋아요.",
              "callDate": "2024-01-01"
            }
            """
            ) HealthDataTestRequest request
    ) {
        log.info("건강 데이터 추출 테스트: {}", request);

        // 테스트용 CareCallRecord 조회 또는 생성
        CareCallRecord savedCallRecord = testDataGenerator.createOrGetTestCallRecord(
                request.getElderId(),
                request.getSettingId(),
                request.getTranscriptionText()
        );

        // OpenAI를 통한 건강 데이터 추출
        HealthDataExtractionResponse healthData = careCallAnalysisService.extractHealthData(
                request.getCallDate(),
                request.getTranscriptionText(),
                Collections.emptyList()
        );

        // 건강 데이터를 DB에 저장
        careCallAnalysisResultSaveService.processAndSaveHealthData(savedCallRecord, healthData);

        return ResponseEntity.ok(savedCallRecord);
    }
}