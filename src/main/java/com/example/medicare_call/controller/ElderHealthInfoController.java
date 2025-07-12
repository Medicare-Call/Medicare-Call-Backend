package com.example.medicare_call.controller;

import com.example.medicare_call.dto.ElderHealthRegisterRequest;
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
@RequestMapping("/elders/{elderId}/health-info")
@RequiredArgsConstructor
public class ElderHealthInfoController {
    private final ElderHealthInfoService elderHealthInfoService;

    @Operation(summary = "어르신 건강정보 등록", description = "질환, 복약주기, 특이사항을 등록합니다.")
    @PostMapping
    public ResponseEntity<Void> registerElderHealthInfo(@PathVariable Integer elderId, @Valid @RequestBody ElderHealthRegisterRequest request) {
        elderHealthInfoService.registerElderHealthInfo(elderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
} 