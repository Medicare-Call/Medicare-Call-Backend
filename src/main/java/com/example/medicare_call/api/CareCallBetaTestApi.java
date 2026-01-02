package com.example.medicare_call.api;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.CallDataUploadRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public interface CareCallBetaTestApi {

    @Operation(
            summary = "[베타테스트용] 전화 데이터 처리",
            description = "전화 녹음본을 업로드 하면 STT 후 분석 데이터가 저장됩니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CallDataUploadRequest.class)
                    )
            )
    )
    ResponseEntity<CareCallRecord> uploadAndProcessCallData(CallDataUploadRequest request);
}
