package com.example.medicare_call.service.auth;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.RefreshToken;
import com.example.medicare_call.dto.auth.TokenResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final EntityManager entityManager;
    
    @Value("${jwt.refreshTokenExpirationDays}")
    private Integer refreshTokenExpirationDays;
    
    /**
     * Refresh Token을 생성하고 DB에 저장
     */
    public RefreshToken createRefreshToken(Member member) {
        // 기존 Refresh Token이 있다면 삭제
        refreshTokenRepository.deleteByMemberId(member.getId());
        entityManager.flush(); // 삭제 후 변경사항 반영
        
        // 새로운 Refresh Token 생성 (UUID 기반)
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenExpirationDays);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(member.getId())
                .token(tokenValue)
                .expiresAt(expiresAt)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Refresh Token으로 새로운 Access Token을 발급
     */
    public TokenResponse refreshAccessToken(String refreshTokenValue) {
        // Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않은 Refresh Token 입니다."));

        // 만료시 SMS 재인증 필요
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED, "만료된 Refresh Token 입니다. 다시 로그인해 주세요");
        }
        
        // Member 조회
        Member member = memberRepository.findById(refreshToken.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "가입된 회원 정보를 찾을 수 없습니다."));
        
        // 새로운 Access Token 생성
        String newAccessToken = jwtProvider.createAccessToken(member);
        
        // 새로운 Refresh Token 생성 (Rotation)
        RefreshToken newRefreshToken = createRefreshToken(member);
        
        log.info("Refresh Token Rotation 완료 - Member ID: {}", member.getId());
        
        return TokenResponse.of(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtProvider.getAccessTokenExpirationMillis() / 1000
        );
    }
    
    /**
     * 사용자의 Refresh Token을 삭제 (로그아웃)
     */
    public void deleteRefreshToken(Integer memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
        log.info("Refresh Token 삭제 완료 - Member ID: {}", memberId);
    }
    
    /**
     * 만료된 Refresh Token들을 정리 (매일 새벽 2시 실행)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        log.info("만료된 Refresh Token 정리 완료 - {}", now);
    }
} 