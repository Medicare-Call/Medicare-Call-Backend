package com.example.medicare_call.service;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.SignUpRequest;
import com.example.medicare_call.dto.SmsVerificationResponse;
import com.example.medicare_call.dto.TokenResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
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

    // 회원가입
    public TokenResponse signUp(SignUpRequest req) {
        // 중복 확인
        if (memberRepository.existsByPhone(req.getPhone())) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다.");
        }

        // 사용자 생성
        Member member = Member.builder()
                .phone(req.getPhone())
                .name(req.getName())
                .birthDate(req.getBirthDate())
                .gender(req.getGender().getCode())
                .plan((byte) 0)
                .termsAgreedAt(LocalDateTime.now())
                .build();

        Member savedMember = memberRepository.save(member);
        // JWT 토큰 생성
        return generateTokenResponse(savedMember);
    }

    public SmsVerificationResponse handlePhoneVerification(String phone) {
        Optional<Member> memberOpt = memberRepository.findByPhone(phone);
        log.info("기존/신규 회원 분기");
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            //기존회원
            TokenResponse tokenResponse = generateTokenResponse(member);
            return SmsVerificationResponse.forExistingMember(tokenResponse);

        } else {
            //신규회원
            log.info("신규회원");
            return SmsVerificationResponse.forNewMember();
        }


    }

    // 토큰 응답 생성
    private TokenResponse generateTokenResponse(Member member) {
        String accessToken = jwtProvider.createAccessToken(member);

        return new TokenResponse(
                accessToken,
                "Bearer",
                86400000L, // 24시간
                member.getPhone()
        );
    }

}
