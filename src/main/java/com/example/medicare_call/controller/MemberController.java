package com.example.medicare_call.controller;

import com.example.medicare_call.dto.RegisterRequest;
import com.example.medicare_call.global.annotation.AuthPhone;
import com.example.medicare_call.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member Auth", description = "회원가입/회원등록/로그인 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class MemberController {

    private final AuthService authService;

    @PostMapping("")
    public ResponseEntity<String> register(@Parameter(hidden = true) @AuthPhone String phone,
                                           @Valid @RequestBody RegisterRequest signUpDto) {
        String accessTokenResponse = authService.register(phone,signUpDto);
        return ResponseEntity.ok(accessTokenResponse);
    }
}
