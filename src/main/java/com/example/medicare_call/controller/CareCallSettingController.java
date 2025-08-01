package com.example.medicare_call.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "CareCallSetting", description = "전화 시간대 설정 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/elders/{elderId}/care-call-setting")
public class CareCallSettingController {
}
