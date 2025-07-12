package com.example.medicare_call.util;

import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
        message.setText("[회원가입] 인증번호는 [" + certificationNumber + "]입니다.");

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}

