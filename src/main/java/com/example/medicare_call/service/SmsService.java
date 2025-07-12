package com.example.medicare_call.service;

import com.example.medicare_call.repository.SmsRepository;
import com.example.medicare_call.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class SmsService {

    private final SmsRepository smsRepository;
    private final SmsUtil smsUtil;

    // 인증번호 발송
    public void sendCertificationNumber(String phone) {
        // 6자리 랜덤 인증번호 생성
        String certificationNumber = String.format("%06d", new Random().nextInt(999999));

        // SMS 발송
        smsUtil.sendSMS(phone, certificationNumber);

        // Redis에 저장
        smsRepository.createSmsCertification(phone, certificationNumber);
    }

    // 인증번호 검증
    public boolean verifyCertificationNumber(String phone, String certificationNumber) {
        if (!smsRepository.hasKey(phone)) {
            return false;
        }

        String storedNumber = smsRepository.getSmsCertification(phone);
        if (storedNumber != null && storedNumber.equals(certificationNumber)) {
            // 인증 성공 시 Redis에서 삭제
            smsRepository.deleteSmsCertification(phone);
            return true;
        }

        return false;
    }
}

