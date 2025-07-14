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

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> signUp(@Valid @RequestBody SignUpRequest signUpDto) {
        TokenResponse tokenResponse = authService.signUp(signUpDto);
        return ResponseEntity.ok(tokenResponse);
    }
}
