package com.example.medicare_call.controller;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.service.carecall.CareCallRequestSenderService;
import com.example.medicare_call.service.carecall.CareCallSettingService;
import com.example.medicare_call.service.data_processor.CareCallDataProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "CareCall", description = "케어콜 설정, 데이터 수신, 테스트 API")
@RestController
@RequiredArgsConstructor
public class CareCallController {

    private final CareCallSettingService careCallSettingService;
    private final CareCallDataProcessingService careCallDataProcessingService;
    private final CareCallRequestSenderService careCallRequestSenderService;

    // From CareCallSettingController
    @Operation(summary = "어르신 전화 시간대 등록", description = "3번의 케어콜 시간대를 저장합니다.")
    @PostMapping("/elders/{elderId}/care-call-setting")
    public ResponseEntity<Void> settingCarCallInfo(@PathVariable Integer elderId, @Valid @RequestBody CareCallSettingRequest request) {
        careCallSettingService.settingCareCall(elderId,request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // From CallDataController
    @Operation(summary = "통화 데이터 수신", description = "외부 서버로부터 통화 데이터를 받아서 저장합니다.")
    @PostMapping("/call-data")
    public ResponseEntity<CareCallRecord> receiveCallData(@Valid @RequestBody CareCallDataProcessRequest request) {
        log.info("통화 데이터 수신: elderId={}, settingId={}", request.getElderId(), request.getSettingId());
        CareCallRecord savedRecord = careCallDataProcessingService.saveCallData(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // From TestCareCallController
    //TODO: 개발 완료 후 삭제
    @Operation(summary = "[테스트용] 프롬프트 테스트", description = "케어콜에 전송하는 프롬프트를 직접 작성하여 전화 품질을 테스트합니다.")
    @PostMapping("/test-care-call")
    public ResponseEntity<String> testCareCall(@Valid @RequestBody CareCallTestRequest req) {
        careCallRequestSenderService.sendTestCall(req);
        return ResponseEntity.ok(req.prompt());
    }
}
