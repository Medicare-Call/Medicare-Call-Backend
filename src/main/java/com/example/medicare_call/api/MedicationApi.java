package com.example.medicare_call.api;

import com.example.medicare_call.dto.report.DailyMedicationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

public interface MedicationApi {

    @Operation(
            summary = "날짜별 복약 데이터 조회",
            description = "어르신의 특정 날짜에 기록된 복약 내역을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "복약 데이터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DailyMedicationResponse.class)
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
    ResponseEntity<DailyMedicationResponse> getDailyMedication(
            @PathVariable("elderId") Integer elderId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}
