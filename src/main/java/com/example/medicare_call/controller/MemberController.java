package com.example.medicare_call.controller;

import com.example.medicare_call.dto.SignUpRequest;
import com.example.medicare_call.dto.TokenResponse;
import com.example.medicare_call.service.AuthService;
import com.example.medicare_call.service.MemberService;
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

@Tag(name = "Member", description = "서비스 멤버(보호자) 등록/관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class MemberController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "회원 정보 입력", description = "인증번호 확인 후 신규 회원에 대한 정보를 입력받습니다.")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpDto) {
        Map<String, Object> response = new HashMap<>();

        try {
            TokenResponse res = authService.signUp(signUpDto);
            return ResponseEntity.ok(res);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원가입에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
