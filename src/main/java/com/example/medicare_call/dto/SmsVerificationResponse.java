package com.example.medicare_call.dto;

import com.example.medicare_call.global.enums.MemberStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsVerificationResponse {
    private boolean verified;
    private String message;
    private MemberStatus memberStatus;
    private String nextAction;
    private String token; // 기존 회원인 경우



    public static SmsVerificationResponse forExistingMember(String accessToken) {
        return SmsVerificationResponse.builder()
                .verified(true)
                .message("인증이 완료되었습니다. 로그인되었습니다.")
                .memberStatus(MemberStatus.EXISTING_MEMBER)
                .nextAction("HOME")
                .token(accessToken)
                .build();
    }

    public static SmsVerificationResponse forNewMember(String phoneVerificationToken) {
        return SmsVerificationResponse.builder()
                .verified(true)
                .message("인증이 완료되었습니다. 회원 정보를 입력해주세요.")
                .memberStatus(MemberStatus.NEW_MEMBER)
                .nextAction("REGISTER")
                .token(phoneVerificationToken)
                .build();
    }
}