package com.example.medicare_call.controller;

import com.example.medicare_call.dto.SmsRequest;
import com.example.medicare_call.dto.SmsVerifyDto;
import com.example.medicare_call.service.MemberService;
import com.example.medicare_call.service.SmsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Sms", description = "전화번호 인증 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/members/sms")
public class SmsController {

    private final SmsService smsService;
//    private final MemberService memberService;

    // 인증번호 발송
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendSms(@Valid @RequestBody SmsRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
//            // 이미 가입된 전화번호인지 확인
//            if (memberService.isPhoneExists(request.getPhone())) {
//                response.put("message", "이미 가입된 전화번호입니다.");
//                return ResponseEntity.badRequest().body(response);
//            }

            smsService.sendCertificationNumber(request.getPhone());
            response.put("message", "인증번호가 발송되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "인증번호 발송에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifySms(@Valid @RequestBody SmsVerifyDto request) {
        Map<String, Object> response = new HashMap<>();

        boolean isVerified = smsService.verifyCertificationNumber(request.getPhone(), request.getCertificationCode());

        if (isVerified) {
            response.put("verified", true);
            response.put("message", "인증이 완료되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("verified", false);
            response.put("message", "인증번호가 올바르지 않거나 만료되었습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }


}
