package com.example.medicare_call.dto.auth;

import com.example.medicare_call.global.enums.MemberStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsVerificationResponse {
    private boolean verified;
    private String message;
    private MemberStatus memberStatus;
    private String token; // 신규 회원인 경우 phone verification token
    private String accessToken; // 기존 회원인 경우 access token
    private String refreshToken; // 기존 회원인 경우 refresh token



    public static SmsVerificationResponse forExistingMember(String accessToken, String refreshToken) {
        return SmsVerificationResponse.builder()
                .verified(true)
                .message("인증이 완료되었습니다. 로그인되었습니다.")
                .memberStatus(MemberStatus.EXISTING_MEMBER)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static SmsVerificationResponse forNewMember(String phoneVerificationToken) {
        return SmsVerificationResponse.builder()
                .verified(true)
                .message("인증이 완료되었습니다. 회원 정보를 입력해주세요.")
                .memberStatus(MemberStatus.NEW_MEMBER)
                .token(phoneVerificationToken)
                .build();
    }
}