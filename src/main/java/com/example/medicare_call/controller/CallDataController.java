package com.example.medicare_call.controller;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.CallDataRequest;
import com.example.medicare_call.service.CallDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "CallData", description = "통화 데이터 수신 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/call-data")
public class CallDataController {
    private final CallDataService callDataService;

    @Operation(summary = "통화 데이터 수신", description = "외부 서버로부터 통화 데이터를 받아서 저장합니다.")
    @PostMapping
    public ResponseEntity<CareCallRecord> receiveCallData(@Valid @RequestBody CallDataRequest request) {
        log.info("통화 데이터 수신: elderId={}, settingId={}", request.getElderId(), request.getSettingId());
        CareCallRecord savedRecord = callDataService.saveCallData(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
    }
} 