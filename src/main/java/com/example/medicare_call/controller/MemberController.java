package com.example.medicare_call.controller;

import com.example.medicare_call.dto.SignUpRequest;
import com.example.medicare_call.dto.TokenResponse;
import com.example.medicare_call.service.AuthService;
import com.example.medicare_call.service.MemberService;
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

@Tag(name = "Member Auth", description = "회원가입/회원등록/로그인 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class MemberController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@Valid @RequestBody SignUpRequest signUpDto) {
        Map<String, Object> response = new HashMap<>();

        try {
            TokenResponse tokenResponse = authService.signUp(signUpDto);

            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("data", tokenResponse);

            return ResponseEntity.ok(response);

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
