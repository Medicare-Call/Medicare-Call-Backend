package com.example.medicare_call.service;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.RefreshToken;
import com.example.medicare_call.dto.TokenResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationDays", 30);
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void createRefreshToken_success() {
        // given
        Member member = Member.builder()
                .id(1)
                .name("테스트")
                .phone("01012345678")
                .build();

        RefreshToken savedToken = RefreshToken.builder()
                .id(1L)
                .memberId(1)
                .token("test-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        doNothing().when(refreshTokenRepository).deleteByMemberId(1);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        // when
        RefreshToken result = refreshTokenService.createRefreshToken(member);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(1);
        assertThat(result.getToken()).isEqualTo("test-refresh-token");
        assertThat(result.isExpired()).isFalse();

        verify(refreshTokenRepository).deleteByMemberId(1);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Access Token 갱신 성공")
    void refreshAccessToken_success() {
        // given
        String refreshTokenValue = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        RefreshToken existingToken = RefreshToken.builder()
                .id(1L)
                .memberId(1)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        Member member = Member.builder()
                .id(1)
                .name("테스트")
                .phone("01012345678")
                .build();

        RefreshToken newToken = RefreshToken.builder()
                .id(2L)
                .memberId(1)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(existingToken));
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
        when(jwtProvider.createAccessToken(member)).thenReturn(newAccessToken);
        when(jwtProvider.getAccessTokenExpirationMillis()).thenReturn(3600000L); // 1시간
        doNothing().when(refreshTokenRepository).deleteByMemberId(1);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);

        // when
        TokenResponse result = refreshTokenService.refreshAccessToken(refreshTokenValue);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(result.getExpiresIn()).isEqualTo(3600L);

        verify(refreshTokenRepository).findByToken(refreshTokenValue);
        verify(memberRepository).findById(1);
        verify(jwtProvider).createAccessToken(member);
    }

    @Test
    @DisplayName("Access Token 갱신 실패 - 유효하지 않은 Refresh Token")
    void refreshAccessToken_fail_invalidToken() {
        // given
        String invalidToken = "invalid-refresh-token";
        when(refreshTokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(invalidToken))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("유효하지 않은 Refresh Token입니다.");

        verify(refreshTokenRepository).findByToken(invalidToken);
        verify(memberRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Access Token 갱신 실패 - 만료된 Refresh Token")
    void refreshAccessToken_fail_expiredToken() {
        // given
        String expiredToken = "expired-refresh-token";
        RefreshToken expiredRefreshToken = RefreshToken.builder()
                .id(1L)
                .memberId(1)
                .token(expiredToken)
                .expiresAt(LocalDateTime.now().minusDays(1)) // 만료된 토큰
                .build();

        when(refreshTokenRepository.findByToken(expiredToken)).thenReturn(Optional.of(expiredRefreshToken));

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(expiredToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("만료된 Refresh Token입니다.");

        verify(refreshTokenRepository).findByToken(expiredToken);
        verify(refreshTokenRepository).delete(expiredRefreshToken);
        verify(memberRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Refresh Token 삭제 성공")
    void deleteRefreshToken_success() {
        // given
        Integer memberId = 1;
        doNothing().when(refreshTokenRepository).deleteByMemberId(memberId);

        // when
        refreshTokenService.deleteRefreshToken(memberId);

        // then
        verify(refreshTokenRepository).deleteByMemberId(memberId);
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 성공")
    void getMemberIdFromToken_success() {
        // given
        String token = "valid-token";
        when(jwtProvider.getMemberId(token)).thenReturn(1L);

        // when
        Long result = refreshTokenService.getMemberIdFromToken(token);

        // then
        assertThat(result).isEqualTo(1L);
        verify(jwtProvider).getMemberId(token);
    }
} 