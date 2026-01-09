package com.example.medicare_call.controller;

import com.example.medicare_call.api.CareCallApi;
import com.example.medicare_call.api.CareCallBetaTestApi;
import com.example.medicare_call.api.CareCallTestApi;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.dto.carecall.CareCallSettingResponse;
import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest;
import com.example.medicare_call.dto.data_processor.CallDataUploadRequest;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.carecall.CareCallSettingService;
import com.example.medicare_call.service.carecall.CareCallTestService;
import com.example.medicare_call.service.data_processor.CareCallService;
import com.example.medicare_call.service.data_processor.CareCallUploadService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "CareCall", description = "케어콜 설정, 데이터 수신, 테스트 API")
public class CareCallController implements CareCallApi, CareCallBetaTestApi, CareCallTestApi {

    private final CareCallSettingService careCallSettingService;
    private final CareCallService careCallService;
    private final CareCallTestService careCallTestService;
    private final CareCallUploadService careCallUploadService;


    @Override
    @PostMapping("/elders/{elderId}/care-call-setting")
    public ResponseEntity<Void> upsertCareCallSetting(@Parameter(hidden = true) @AuthUser Integer memberId, @PathVariable Integer elderId, @Valid @RequestBody CareCallSettingRequest request) {
        careCallSettingService.upsertCareCallSetting(memberId, elderId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    @GetMapping("/elders/{elderId}/care-call-setting")
    public ResponseEntity<CareCallSettingResponse> getCareCallSetting(@Parameter(hidden = true) @AuthUser Integer memberId, @PathVariable Integer elderId) {
        CareCallSettingResponse response = careCallSettingService.getCareCallSetting(memberId, elderId);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/call-data")
    public ResponseEntity<CareCallRecord> receiveCallData(@Valid @RequestBody CareCallDataProcessRequest request) {
        log.info("통화 데이터 수신: elderId={}, settingId={}", request.getElderId(), request.getSettingId());
        careCallService.saveCallData(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PostMapping("/care-call/immediate")
    public ResponseEntity<String> sendImmediateCareCall(@Valid @RequestBody ImmediateCareCallRequest request) {
        String result = careCallTestService.sendImmediateCall(request.getElderId(), request.getCareCallOption());
        return ResponseEntity.ok(result);
    }

    //TODO: 커스텀 프롬프트 케어콜 테스트용, 개발 완료 후 삭제
    @Override
    @PostMapping("/care-call/test")
    public ResponseEntity<String> testCareCall(@Valid @RequestBody CareCallTestRequest req) {
        careCallTestService.sendTestCall(req);
        return ResponseEntity.ok(req.prompt());
    }

    //TODO: 베타테스트용 API. 삭제 필요
    @Override
    @PostMapping(value = "/call-data/beta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CareCallRecord> uploadAndProcessCallData(
            @ModelAttribute @Valid CallDataUploadRequest request
    ) {
        log.info("베타 테스트용 전화 데이터 업로드 요청 수신: elderId={}", request.getElderId());
        CareCallRecord savedRecord = careCallUploadService.processUploadedCallData(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
    }
}

