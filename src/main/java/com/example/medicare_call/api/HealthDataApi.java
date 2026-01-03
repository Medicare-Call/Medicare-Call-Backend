package com.example.medicare_call.api;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface HealthDataApi {

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
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<HealthDataExtractionResponse> extractHealthData(
        @RequestBody HealthDataExtractionRequest request
    );
}