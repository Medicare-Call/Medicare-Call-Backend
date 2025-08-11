package com.example.medicare_call.service.auth;

import com.example.medicare_call.repository.SmsRepository;
import com.example.medicare_call.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SmsVerificationService {

    private final SmsRepository smsRepository;
    private final SmsUtil smsUtil;

    public void sendCertificationNumber(String phone) {
        String certificationNumber = String.format("%06d", new Random().nextInt(999999));
//        log.info("인증번호는: " + certificationNumber); //로컬 테스트용
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

