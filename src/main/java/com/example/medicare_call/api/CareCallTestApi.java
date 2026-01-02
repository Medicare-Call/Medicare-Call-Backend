package com.example.medicare_call.api;

import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface CareCallTestApi {

    @Operation(summary = "[테스트용] 프롬프트 테스트", description = "케어콜에 전송하는 프롬프트를 직접 작성하여 전화 품질을 테스트합니다.")
    ResponseEntity<String> testCareCall(CareCallTestRequest req);
}
