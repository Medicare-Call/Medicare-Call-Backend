package com.example.medicare_call.service.auth;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.RefreshToken;
import com.example.medicare_call.dto.auth.MemberRegisterRequest;
import com.example.medicare_call.dto.auth.SmsVerificationResponse;
import com.example.medicare_call.dto.auth.TokenResponse;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.MemberStatus;
import com.example.medicare_call.global.enums.SubscriptionPlan;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.MemberRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공 - 새로운 전화번호")
    void register_success() {
        String phone = "01012345678";
        MemberRegisterRequest request = new MemberRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setGender(Gender.MALE);

        Member savedMember = Member.builder()
                .id(1)
                .phone(phone)
                .name("홍길동")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.FEMALE)
                .plan(SubscriptionPlan.STANDARD)
                .termsAgreedAt(LocalDateTime.now())
                .build();

        when(memberRepository.existsByPhone(phone)).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(jwtProvider.createAccessToken(savedMember)).thenReturn("sample-access-token");
        when(jwtProvider.getAccessTokenExpirationMillis()).thenReturn(3600000L);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .memberId(1)
                .token("sample-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        when(refreshTokenService.createRefreshToken(savedMember)).thenReturn(refreshToken);

        TokenResponse result = authService.register(phone, request);

        assertThat(result.getAccessToken()).isEqualTo("sample-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("sample-refresh-token");
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 등록된 전화번호")
    void register_fail_duplicatePhone() {
        String phone = "01012345678";
        MemberRegisterRequest request = new MemberRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setGender(Gender.MALE);

        when(memberRepository.existsByPhone(phone)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(phone, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 전화번호입니다.");
    }

    @Test
    @DisplayName("전화번호 인증 처리 - 기존 회원")
    void handlePhoneVerification_existingMember() {
        String phone = "01012345678";
        Member existingMember = Member.builder()
                .id(1)
                .phone(phone)
                .name("김철수")
                .birthDate(LocalDate.of(1980, 5, 15))
                .gender(Gender.MALE)
                .plan(SubscriptionPlan.STANDARD)
                .termsAgreedAt(LocalDateTime.now())
                .build();

        when(memberRepository.findByPhone(phone)).thenReturn(Optional.of(existingMember));
        when(jwtProvider.createAccessToken(existingMember)).thenReturn("sample-access-token");
        when(jwtProvider.getAccessTokenExpirationMillis()).thenReturn(3600000L);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .memberId(1)
                .token("sample-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        when(refreshTokenService.createRefreshToken(existingMember)).thenReturn(refreshToken);

        SmsVerificationResponse result = authService.handlePhoneVerification(phone);

        assertThat(result.isVerified()).isTrue();
        assertThat(result.getMemberStatus()).isEqualTo(MemberStatus.EXISTING_MEMBER);
        assertThat(result.getAccessToken()).isEqualTo("sample-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("sample-refresh-token");
        assertThat(result.getMessage()).isEqualTo("인증이 완료되었습니다. 로그인되었습니다.");
    }

    @Test
    @DisplayName("전화번호 인증 처리 - 신규 회원")
    void handlePhoneVerification_newMember() {
        String phone = "01098765432";

        when(memberRepository.findByPhone(phone)).thenReturn(Optional.empty());
        when(jwtProvider.createPhoneVerificationToken(phone)).thenReturn("sample-phone-token");

        SmsVerificationResponse result = authService.handlePhoneVerification(phone);

        assertThat(result.isVerified()).isTrue();
        assertThat(result.getMemberStatus()).isEqualTo(MemberStatus.NEW_MEMBER);
        assertThat(result.getToken()).isEqualTo("sample-phone-token");
        assertThat(result.getMessage()).isEqualTo("인증이 완료되었습니다. 회원 정보를 입력해주세요.");
    }
}
