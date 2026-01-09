package com.example.medicare_call.controller;

import com.example.medicare_call.dto.auth.TokenResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.auth.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "토큰 관리", description = "토큰 관련 API")
public class TokenController {
    
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    
    @PostMapping("/refresh")
    @Operation(summary = "Access Token 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    public ResponseEntity<TokenResponse> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("Access Token 갱신 요청: {}", refreshToken);
        
        TokenResponse tokenResponse = refreshTokenService.refreshAccessToken(refreshToken);
        
        return ResponseEntity.ok(tokenResponse);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자의 Refresh Token을 삭제합니다.")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        // Authorization 헤더에서 토큰 추출 (Bearer 제거)
        String token = authorization.replace("Bearer ", "");
        
        // 토큰에서 사용자 ID 추출
        Integer memberId = jwtProvider.getMemberId(token);
        
        // Refresh Token 삭제
        refreshTokenService.deleteRefreshToken(memberId);
        
        log.info("로그아웃 완료 - Member ID: {}", memberId);
        
        return ResponseEntity.ok().build();
    }
} 