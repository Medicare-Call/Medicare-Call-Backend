package com.example.medicare_call.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class SmsRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final String PREFIX = "sms:";
    private final int LIMIT_TIME = 3 * 60; // 3분

    // 인증번호 저장
    public void createSmsCertification(String phone, String certificationNumber) {
        stringRedisTemplate.opsForValue()
                .set(PREFIX + phone, certificationNumber, Duration.ofSeconds(LIMIT_TIME));
    }

    // 인증번호 조회
    public String getSmsCertification(String phone) {
        return stringRedisTemplate.opsForValue().get(PREFIX + phone);
    }

    // 인증번호 삭제
    public void deleteSmsCertification(String phone) {
        stringRedisTemplate.delete(PREFIX + phone);
    }

    // 키 존재 여부 확인
    public boolean hasKey(String phone) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(PREFIX + phone));
    }
}
