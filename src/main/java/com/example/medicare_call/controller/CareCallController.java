package com.example.medicare_call.controller;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.dto.carecall.CareCallSettingResponse;
import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.carecall.CareCallRequestSenderService;
import com.example.medicare_call.service.carecall.CareCallSettingService;
import com.example.medicare_call.service.data_processor.CareCallDataProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "어르신 전화 시간대 등록 및 수정", description = "3번의 케어콜 시간대를 저장 및 수정합니다.")
    @PostMapping("/elders/{elderId}/care-call-setting")
    public ResponseEntity<Void> upsertCareCallSetting(@Parameter(hidden = true) @AuthUser Long memberId, @PathVariable Integer elderId, @Valid @RequestBody CareCallSettingRequest request) {
        careCallSettingService.upsertCareCallSetting(memberId.intValue(), elderId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "어르신 전화 시간대 조회", description = "등록된 케어콜 시간대를 조회합니다.")
    @GetMapping("/elders/{elderId}/care-call-setting")
    public ResponseEntity<CareCallSettingResponse> getCareCallSetting(@Parameter(hidden = true) @AuthUser Long memberId, @PathVariable Integer elderId) {
        CareCallSettingResponse response = careCallSettingService.getCareCallSetting(memberId.intValue(), elderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "통화 데이터 수신", description = "외부 서버로부터 통화 데이터를 받아서 저장합니다.")
    @PostMapping("/call-data")
    public ResponseEntity<CareCallRecord> receiveCallData(@Valid @RequestBody CareCallDataProcessRequest request) {
        log.info("통화 데이터 수신: elderId={}, settingId={}", request.getElderId(), request.getSettingId());
        CareCallRecord savedRecord = careCallDataProcessingService.saveCallData(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // TODO [DEMO] 데모데이 시연용 임시 코드 → 정식 버전 구현 시 제거 필요
    @Operation(summary = "즉시 케어콜 발송", description = "memberId를 통해 해당 보호자의 첫 번째 어르신에게 즉시 케어콜을 발송합니다.")
    @PostMapping("/care-call/immediate")
    public ResponseEntity<String> sendImmediateCareCall(@Valid @RequestBody ImmediateCareCallRequest request) {
        String result = careCallRequestSenderService.sendImmediateCall(request.getElderId(), request.getCareCallOption());
        return ResponseEntity.ok(result);
    }

    //TODO: 개발 완료 후 삭제
    @Operation(summary = "[테스트용] 프롬프트 테스트", description = "케어콜에 전송하는 프롬프트를 직접 작성하여 전화 품질을 테스트합니다.")
    @PostMapping("/test-care-call")
    public ResponseEntity<String> testCareCall(@Valid @RequestBody CareCallTestRequest req) {
        careCallRequestSenderService.sendTestCall(req);
        return ResponseEntity.ok(req.prompt());
    }
}
