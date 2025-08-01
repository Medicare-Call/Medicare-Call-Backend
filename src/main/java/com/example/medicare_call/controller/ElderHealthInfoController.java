package com.example.medicare_call.controller;

import com.example.medicare_call.dto.CareCallSettingRequest;
import com.example.medicare_call.dto.ElderHealthRegisterRequest;
import com.example.medicare_call.service.CareCallSettingService;
import com.example.medicare_call.service.ElderHealthInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ElderHealthInfo", description = "어르신 건강정보(질환, 복약주기, 특이사항) 등록 API")
@RestController
@RequestMapping("/elders/{elderId}")
@RequiredArgsConstructor
public class ElderHealthInfoController {
    private final ElderHealthInfoService elderHealthInfoService;
    private final CareCallSettingService careCallSettingService;

    @Operation(summary = "어르신 건강정보 등록", description = "질환, 복약주기, 특이사항을 등록합니다.")
    @PostMapping("/health-info")
    public ResponseEntity<Void> registerElderHealthInfo(@PathVariable Integer elderId, @Valid @RequestBody ElderHealthRegisterRequest request) {
        elderHealthInfoService.registerElderHealthInfo(elderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "어르신 전화 시간대 등록", description = "3번의 케어콜 시간대를 저장합니다.")
    @PostMapping("/care-call-setting")
    public ResponseEntity<Void> settingCarCallInfo(@PathVariable Integer elderId, @Valid @RequestBody CareCallSettingRequest request) {
        careCallSettingService.settingCareCall(elderId,request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
} 