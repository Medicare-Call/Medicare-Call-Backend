package com.example.medicare_call.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsVerificationResponse {
    private boolean verified;
    private String message;
    private MemberStatus memberStatus;
    private String nextAction;
    private TokenResponse tokenResponse; // 기존 회원인 경우

    public enum MemberStatus {
        EXISTING_MEMBER, //기존 회원
        NEW_MEMBER //신규 회원
    }

    public static SmsVerificationResponse forExistingMember(TokenResponse tokenResponse) {
        return SmsVerificationResponse.builder()
                .verified(true)
                .message("인증이 완료되었습니다. 로그인되었습니다.")
                .memberStatus(MemberStatus.EXISTING_MEMBER)
                .nextAction("HOME")
                .tokenResponse(tokenResponse)
                .build();
    }

    public static SmsVerificationResponse forNewMember() {
        return SmsVerificationResponse.builder()
                .verified(true)
                .message("인증이 완료되었습니다. 회원 정보를 입력해주세요.")
                .memberStatus(MemberStatus.NEW_MEMBER)
                .nextAction("REGISTER")
                .tokenResponse(null)
                .build();
    }
}
