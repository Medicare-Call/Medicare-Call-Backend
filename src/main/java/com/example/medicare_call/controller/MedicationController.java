package com.example.medicare_call.controller;

import com.example.medicare_call.api.MedicationApi;
import com.example.medicare_call.dto.report.DailyMedicationResponse;
import com.example.medicare_call.service.data_processor.MedicationService;
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
import org.springframework.format.annotation.DateTimeFormat;
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
@Tag(name = "Medication", description = "복약 데이터 조회 API")
public class MedicationController implements MedicationApi {
    
    private final MedicationService medicationService;

    @GetMapping("/{elderId}/medication")
    public ResponseEntity<DailyMedicationResponse> getDailyMedication(
        @Parameter(description = "어르신 식별자", required = true, example = "1")
        @PathVariable("elderId") Integer elderId,
        
        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true, example = "2025-07-16")
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("날짜별 복약 데이터 조회 요청: elderId={}, date={}", elderId, date);
        
        DailyMedicationResponse response = medicationService.getDailyMedication(elderId, date);
        
        return ResponseEntity.ok(response);
    }
} 