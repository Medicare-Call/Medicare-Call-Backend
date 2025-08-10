package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.ElderSettingRequest;
import com.example.medicare_call.dto.ElderSettingResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.ElderSettingService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
@Tag(name = "Elder Setting", description = "어르신 개인정보 설정 API")
public class EldersSettingController {
    private final ElderSettingService elderSettingService;

    @GetMapping("/settings")
    public ResponseEntity<List<ElderSettingResponse>> getElderSettingInfo(@Parameter(hidden = true) @AuthUser Long memberId){
        log.info("어르신 설정 정보 조회 요청: memberId={}", memberId);
        List<ElderSettingResponse> body = elderSettingService.getElderSetting(memberId.intValue());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{elderId}/settings")
    public ResponseEntity<ElderSettingResponse> updateElderSettingInfo(
            @Parameter(hidden = true) @AuthUser Long memberId,
            @PathVariable Integer elderId,
            ElderSettingRequest req
    ){
        log.info("어르신 설정 정보 수정 요청: memberId={}, elderId={}, name={}, birthDate={}, gender={}, phone={}, relationship={}, residenceType={}",
                memberId, elderId, req.name(), req.birthDate(), req.gender(), req.phone(), req.relationship(), req.residenceType());

        ElderSettingResponse res = elderSettingService.updateElderSetting(memberId.intValue(), elderId, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{elderId}/settings")
    public ResponseEntity<Void> deleteElderSettingInfo(
            @Parameter(hidden = true) @AuthUser Long memberId,
            @PathVariable Integer elderId
    ){
        log.info("어르신 설정 정보 삭제 요청: memberId={}, elderId={}", memberId, elderId);
        elderSettingService.deleteElderSetting(memberId.intValue(), elderId);
        return ResponseEntity.noContent().build();
    }
}
