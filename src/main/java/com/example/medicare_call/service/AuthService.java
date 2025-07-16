package com.example.medicare_call.service;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.RegisterRequest;
import com.example.medicare_call.dto.SmsVerificationResponse;
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

    public String register(String phone, RegisterRequest req) {

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
        return generateAccessTokenResponse(savedMember);
    }

    public SmsVerificationResponse handlePhoneVerification(String phone) {
        Optional<Member> memberOpt = memberRepository.findByPhone(phone);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            log.info("기존회원");
            //기존회원
            String accessToken = generateAccessTokenResponse(member);
            return SmsVerificationResponse.forExistingMember(accessToken);

        } else {
            //신규회원
            log.info("신규회원");
            String phoneVerificationToken = generatePhoneVerificationToken(phone);
            return SmsVerificationResponse.forNewMember(phoneVerificationToken);
        }


    }

    // 토큰 응답 생성
    private String generateAccessTokenResponse(Member member) {
        return jwtProvider.createAccessToken(member);
    }

    private String generatePhoneVerificationToken(String phone) {
        return jwtProvider.createPhoneVerificationToken(phone);
    }

}