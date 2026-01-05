package com.example.medicare_call.api;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.HealthDataTestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface HealthDataTestApi {

    @Operation(
        summary = "[개발자용] 건강 데이터 추출 테스트",
        description = "건강 데이터를 추출하여 DB에 저장하는 기능을 테스트합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "건강 데이터 추출 및 저장 성공",
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
    public ResponseEntity<CareCallRecord> saveTestHealthData(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "General Health",
                        value = """
                            {
                              "elderId": 1,
                              "settingId": 1,
                              "transcriptionText": "오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요. 기분도 좋아요.",
                              "callDate": "2024-01-01"
                            }
                        """
                    ),
                    @ExampleObject(
                        name = "Sleep",
                        value = """
                            {
                              "elderId": 1,
                              "settingId": 1,
                              "transcriptionText": "어제 밤 10시에 잠들어서 오늘 아침 6시에 일어났어요. 8시간 잘 잤어요.",
                              "callDate": "2024-01-01"
                            }
                        """
                    ),
                    @ExampleObject(
                        name = "Medication",
                        value = """
                            {
                              "elderId": 1,
                              "settingId": 1,
                              "transcriptionText": "혈압약을 아침에 복용했어요. 오늘은 머리가 좀 아파요.",
                              "callDate": "2024-01-01"
                            }
                        """
                    )
                }
            )
        ) @RequestBody HealthDataTestRequest request
    );
}
