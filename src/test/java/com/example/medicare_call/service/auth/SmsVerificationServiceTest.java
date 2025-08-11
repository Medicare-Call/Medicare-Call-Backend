package com.example.medicare_call.service.auth;

import com.example.medicare_call.repository.SmsRepository;
import com.example.medicare_call.util.SmsUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsVerificationServiceTest {

    @Mock
    private SmsRepository smsRepository;

    @Mock
    private SmsUtil smsUtil;

    @InjectMocks
    private SmsVerificationService smsVerificationService;

    @Test
    @DisplayName("SMS 인증번호 발송 성공")
    void sendCertificationNumber_success() {
        String phone = "01012345678";

        doNothing().when(smsUtil).sendSMS(anyString(), anyString());
        doNothing().when(smsRepository).createSmsCertification(anyString(), anyString());

        smsVerificationService.sendCertificationNumber(phone);

        verify(smsUtil, times(1)).sendSMS(eq(phone), anyString());
        verify(smsRepository, times(1)).createSmsCertification(eq(phone), anyString());
    }

    @Test
    @DisplayName("SMS 인증번호 검증 성공")
    void verifyCertificationNumber_success() {
        String phone = "01012345678";
        String certificationNumber = "123456";

        when(smsRepository.hasKey(phone)).thenReturn(true);
        when(smsRepository.getSmsCertification(phone)).thenReturn(certificationNumber);
        doNothing().when(smsRepository).deleteSmsCertification(phone);

        boolean result = smsVerificationService.verifyCertificationNumber(phone, certificationNumber);

        assertThat(result).isTrue();
        verify(smsRepository, times(1)).hasKey(phone);
        verify(smsRepository, times(1)).getSmsCertification(phone);
        verify(smsRepository, times(1)).deleteSmsCertification(phone);
    }

    @Test
    @DisplayName("SMS 인증번호 검증 실패 - 키 존재하지 않음")
    void verifyCertificationNumber_fail_noKey() {
        String phone = "01012345678";
        String certificationNumber = "123456";

        when(smsRepository.hasKey(phone)).thenReturn(false);

        boolean result = smsVerificationService.verifyCertificationNumber(phone, certificationNumber);

        assertThat(result).isFalse();
        verify(smsRepository, times(1)).hasKey(phone);
        verify(smsRepository, never()).getSmsCertification(phone);
        verify(smsRepository, never()).deleteSmsCertification(phone);
    }

    @Test
    @DisplayName("SMS 인증번호 검증 실패 - 인증번호 불일치")
    void verifyCertificationNumber_fail_wrongCode() {
        String phone = "01012345678";
        String certificationNumber = "123456";
        String wrongCode = "654321";

        when(smsRepository.hasKey(phone)).thenReturn(true);
        when(smsRepository.getSmsCertification(phone)).thenReturn(certificationNumber);

        boolean result = smsVerificationService.verifyCertificationNumber(phone, wrongCode);

        assertThat(result).isFalse();
        verify(smsRepository, times(1)).hasKey(phone);
        verify(smsRepository, times(1)).getSmsCertification(phone);
        verify(smsRepository, never()).deleteSmsCertification(phone);
    }

    @Test
    @DisplayName("SMS 인증번호 검증 실패 - 저장된 인증번호 null")
    void verifyCertificationNumber_fail_nullStoredNumber() {
        String phone = "01012345678";
        String certificationNumber = "123456";

        when(smsRepository.hasKey(phone)).thenReturn(true);
        when(smsRepository.getSmsCertification(phone)).thenReturn(null);

        boolean result = smsVerificationService.verifyCertificationNumber(phone, certificationNumber);

        assertThat(result).isFalse();
        verify(smsRepository, times(1)).hasKey(phone);
        verify(smsRepository, times(1)).getSmsCertification(phone);
        verify(smsRepository, never()).deleteSmsCertification(phone);
    }
}