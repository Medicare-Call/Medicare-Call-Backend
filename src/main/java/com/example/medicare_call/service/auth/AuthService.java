package com.example.medicare_call.service.auth;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.RefreshToken;
import com.example.medicare_call.dto.auth.MemberRegisterRequest;
import com.example.medicare_call.dto.auth.SmsVerificationResponse;
import com.example.medicare_call.dto.auth.TokenResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public TokenResponse register(String phone, MemberRegisterRequest req) {

        if (memberRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다.");
        }

        Member member = Member.builder()
                .phone(phone)
                .name(req.getName())
                .birthDate(req.getBirthDate())
                .gender(req.getGender().getCode())
                .plan((byte) 0)
                .termsAgreedAt(LocalDateTime.now())
                .build();

        Member savedMember = memberRepository.save(member);
        return generateTokenResponse(savedMember);
    }

    public SmsVerificationResponse handlePhoneVerification(String phone) {
        Optional<Member> memberOpt = memberRepository.findByPhone(phone);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            log.info("기존회원");
            //기존회원
            TokenResponse tokenResponse = generateTokenResponse(member);
            return SmsVerificationResponse.forExistingMember(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
        } else {
            //신규회원
            log.info("신규회원");
            String phoneVerificationToken = generatePhoneVerificationToken(phone);
            return SmsVerificationResponse.forNewMember(phoneVerificationToken);
        }
    }

    // 토큰 응답 생성
    private TokenResponse generateTokenResponse(Member member) {
        String accessToken = jwtProvider.createAccessToken(member);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(member);
        
        return TokenResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtProvider.getAccessTokenExpirationMillis() / 1000
        );
    }

    private String generatePhoneVerificationToken(String phone) {
        return jwtProvider.createPhoneVerificationToken(phone);
    }

}