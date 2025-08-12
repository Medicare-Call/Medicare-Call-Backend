package com.example.medicare_call.controller.action;

import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.service.carecall.CareCallRequestSenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//TODO: 개발 완료 후 삭제
@Tag(name = "TestCareCall", description = "[테스트용]프롬프트 테스트 API")
@RestController
@RequestMapping("/test-care-call")
@RequiredArgsConstructor
public class TestCareCallController {
    private final CareCallRequestSenderService careCallRequestSenderService;

    @Operation(summary = "프롬프트 테스트", description = "케어콜에 전송하는 프롬프트를 직접 작성하여 전화 품질을 테스트합니다.")
    @PostMapping("")
    public ResponseEntity<String> testCareCall(@Valid @RequestBody CareCallTestRequest req) {
        careCallRequestSenderService.sendTestCall(req);
        return ResponseEntity.ok(req.prompt());
    }

}
