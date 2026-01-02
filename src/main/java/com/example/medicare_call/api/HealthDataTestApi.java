package com.example.medicare_call.api;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.HealthDataTestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface HealthDataTestApi {

    @Operation(
            summary = "[개발자용] 건강 데이터 DB 저장 테스트",
            description = "건강 데이터를 DB에 저장하는 기능을 테스트합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "건강 데이터 DB 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CareCallRecord.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<CareCallRecord> saveHealthDataToDatabase(
            @RequestBody HealthDataTestRequest request
    );

    @Operation(
            summary = "[개발자용] 건강 데이터 DB 저장 테스트 (수면 데이터)",
            description = "수면 데이터가 포함된 건강 데이터를 DB에 저장하는 기능을 테스트합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "건강 데이터 DB 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CareCallRecord.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<CareCallRecord> saveSleepDataToDatabase(
            @RequestBody HealthDataTestRequest request
    );

    @Operation(
            summary = "[개발자용] 건강 데이터 DB 저장 테스트 (복약 데이터)",
            description = "복약 데이터가 포함된 건강 데이터를 DB에 저장하는 기능을 테스트합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "건강 데이터 DB 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CareCallRecord.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<CareCallRecord> saveMedicationDataToDatabase(
            @RequestBody HealthDataTestRequest request
    );
}
