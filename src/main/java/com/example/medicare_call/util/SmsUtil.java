package com.example.medicare_call.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsUtil {

    @Value("${spring.coolsms.api.key}")
    private String apiKey;

    @Value("${spring.coolsms.api.secret}")
    private String apiSecret;

    @Value("${spring.coolsms.api.number}")
    private String fromNumber;

    public void sendSMS(String to, String certificationNumber) {
        DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");

        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to);
        message.setText("[메디케어콜]\n인증번호는 [" + certificationNumber + "]입니다.");

        try {
            messageService.send(message);
            log.info("SMS 전송 성공 - To: {}", to);
        } catch (NurigoMessageNotReceivedException exception) {
            log.error("메시지 전송 실패 - To: {}", to, exception);
            log.error("실패한 메시지 목록: {}", exception.getFailedMessageList());
            log.error("에러 메시지: {}", exception.getMessage());
        } catch (Exception exception) {
            log.error("SMS 전송 중 예상치 못한 오류 발생 - To: {}", to, exception);
            log.error("에러 타입: {}", exception.getClass().getSimpleName());
            log.error("에러 메시지: {}", exception.getMessage());
        }
    }
}

