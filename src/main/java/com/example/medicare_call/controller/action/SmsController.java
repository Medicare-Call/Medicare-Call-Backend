package com.example.medicare_call.controller.action;

import com.example.medicare_call.dto.auth.SmsSendRequest;
import com.example.medicare_call.dto.auth.SmsVerificationResponse;
import com.example.medicare_call.dto.auth.SmsVerificationRequest;
import com.example.medicare_call.service.auth.AuthService;
import com.example.medicare_call.service.auth.SmsVerificationService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/verifications")
public class SmsController {

    private final SmsVerificationService smsVerificationService;
    private final AuthService authService;

    // 인증번호 발송
    @PostMapping("")
    @Operation(summary = "인증번호 발송", description = "입력된 전화번호로 6자리 인증번호를 발송합니다.")
    public ResponseEntity<Map<String, String>> sendSms(@Valid @RequestBody SmsSendRequest request) {
        Map<String, String> response = new HashMap<>();

        try {

            smsVerificationService.sendCertificationNumber(request.getPhone());
            response.put("message", "인증번호가 발송되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "인증번호 발송에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/confirmation")
    @Operation(summary = "SMS 인증 및 회원 상태 확인", description = "인증번호 검증 후 회원 상태에 따른 다음 단계를 안내합니다.")
    public ResponseEntity<SmsVerificationResponse> verifySms(@Valid @RequestBody SmsVerificationRequest request) {

        boolean isVerified = smsVerificationService.verifyCertificationNumber(request.getPhone(), request.getCertificationCode());

        if (isVerified) {
            SmsVerificationResponse res = authService.handlePhoneVerification(request.getPhone());
            return ResponseEntity.ok(res);
        } else {
            SmsVerificationResponse errorResponse = SmsVerificationResponse.builder()
                    .verified(false)
                    .message("인증번호가 올바르지 않거나 만료되었습니다.")
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


}
