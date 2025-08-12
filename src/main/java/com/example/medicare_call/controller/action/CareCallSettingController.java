package com.example.medicare_call.controller.action;

import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.service.carecall.CareCallSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CareCallSetting", description = "어르신 케어콜 시간대(1차, 2차, 3차) 설정 API")
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
public class CareCallSettingController {
    private final CareCallSettingService careCallSettingService;

    @Operation(summary = "어르신 전화 시간대 등록", description = "3번의 케어콜 시간대를 저장합니다.")
    @PostMapping("/{elderId}/care-call-setting")
    public ResponseEntity<Void> settingCarCallInfo(@PathVariable Integer elderId, @Valid @RequestBody CareCallSettingRequest request) {
        careCallSettingService.settingCareCall(elderId,request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
