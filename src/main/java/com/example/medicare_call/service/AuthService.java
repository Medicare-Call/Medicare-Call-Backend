package com.example.medicare_call.service;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.SignUpRequest;
import com.example.medicare_call.dto.TokenResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
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
                .build();

        Member savedMember = memberRepository.save(member);
        // JWT 토큰 생성
        return generateTokenResponse(savedMember);
    }

//    // 로그인
//    public TokenResponse login(LoginDto loginDto) {
//        try {
//            // 인증 시도
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            loginDto.getPhone(),
//                            loginDto.getPassword()
//                    )
//            );
//
//            // 인증 성공 시 사용자 정보 조회
//            User user = userRepository.findByPhone(loginDto.getPhone())
//                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
//            // JWT 토큰 생성
//            return generateTokenResponse(user);
//
//        } catch (AuthenticationException e) {
//            throw new IllegalArgumentException("전화번호 또는 비밀번호가 올바르지 않습니다.");
//        }
//    }

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
