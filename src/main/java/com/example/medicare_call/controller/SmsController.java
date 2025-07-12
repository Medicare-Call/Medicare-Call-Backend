package com.example.medicare_call.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sms", description = "전화번호 인증 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/members/sms")
public class SmsController {

}
