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

    public void sendCertificationNumber(String phone) {
        String certificationNumber = String.format("%06d", new Random().nextInt(999999));
        smsUtil.sendSMS(phone, certificationNumber);
        //redis에 저장
        smsRepository.createSmsCertification(phone, certificationNumber);
    }

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

