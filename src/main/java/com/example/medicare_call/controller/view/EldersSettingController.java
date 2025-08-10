package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.ElderSettingResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.ElderSettingService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        log.info("memberId from @AuthUser = {}", memberId);
        List<ElderSettingResponse> body = elderSettingService.getElderSetting(memberId.intValue());
        return ResponseEntity.ok(body);
    }
}
