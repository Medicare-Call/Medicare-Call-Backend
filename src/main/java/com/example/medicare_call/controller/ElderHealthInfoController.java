package com.example.medicare_call.controller;

import com.example.medicare_call.dto.ElderHealthInfoResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.dto.ElderHealthInfoCreateRequest;
import com.example.medicare_call.service.report.ElderHealthInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ElderHealthInfo", description = "어르신 건강정보(질환, 복약주기, 특이사항) 등록 API")
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
public class ElderHealthInfoController {
    private final ElderHealthInfoService elderHealthInfoService;

    @Operation(summary = "어르신 건강정보 등록 및 수정", description = "질환, 복약주기, 특이사항을 등록 및 수정합니다.")
    @PostMapping("/{elderId}/health-info")
    public ResponseEntity<Void> upsertElderHealthInfo(@PathVariable Integer elderId, @Valid @RequestBody ElderHealthInfoCreateRequest request) {
        elderHealthInfoService.upsertElderHealthInfo(elderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "어르신 건강정보 조회", description = "elderId와 이름을 포함한 건강 정보를 조회합니다.")
    @GetMapping("/health-info")
    public ResponseEntity<List<ElderHealthInfoResponse>> getElderHealthInfo(@Parameter(hidden = true) @AuthUser Long memberId) {
        return ResponseEntity.ok(elderHealthInfoService.getElderHealth(memberId.intValue()));
    }
}