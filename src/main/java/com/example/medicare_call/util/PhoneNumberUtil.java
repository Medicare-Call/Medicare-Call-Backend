package com.example.medicare_call.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhoneNumberUtil {

    public static String normalizeKoreanPhoneNumber(String phoneNumber) {
        // 010으로 시작하는 경우 +82로 변환
        if (phoneNumber.startsWith("010")) {
            return "+82" + phoneNumber.substring(1);  // "01012345678" → "+821012345678"
        }

        // 이미 +82로 시작하면 그대로 반환
        if (phoneNumber.startsWith("+82")) {
            return phoneNumber;
        }

        log.warn("전화번호 형식이 예상과 다릅니다. 입력값: {}", phoneNumber);
        return phoneNumber;
    }
}
