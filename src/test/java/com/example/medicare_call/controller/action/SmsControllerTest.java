package com.example.medicare_call.controller.action;

import com.example.medicare_call.dto.SmsRequest;
import com.example.medicare_call.dto.SmsVerificationResponse;
import com.example.medicare_call.dto.SmsVerifyDto;
import com.example.medicare_call.global.enums.MemberStatus;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.AuthService;
import com.example.medicare_call.service.SmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SmsController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class SmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SmsService smsService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtProvider jwtProvider;


    @Test
    @DisplayName("SMS 발송 실패 - 전화번호 누락")
    void sendSms_fail_validation() throws Exception {
        SmsRequest request = new SmsRequest();
        request.setPhone(""); // 전화번호 누락

        mockMvc.perform(post("/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SMS 인증 성공 - 기존 회원")
    void verifySms_success_existingMember() throws Exception {
        SmsVerifyDto request = new SmsVerifyDto();
        request.setPhone("01012345678");
        request.setCertificationCode("123456");

        when(smsService.verifyCertificationNumber(anyString(), anyString())).thenReturn(true);
        when(authService.handlePhoneVerification(anyString())).thenReturn(
                SmsVerificationResponse.builder()
                        .verified(true)
                        .message("인증이 완료되었습니다. 로그인되었습니다.")
                        .memberStatus(MemberStatus.EXISTING_MEMBER)
                        .nextAction("HOME")
                        .token("sample-access-token")
                        .build()
        );

        mockMvc.perform(post("/verifications/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.memberStatus").value("EXISTING_MEMBER"))
                .andExpect(jsonPath("$.nextAction").value("HOME"));
    }

    @Test
    @DisplayName("SMS 인증 성공 - 신규 회원")
    void verifySms_success_newMember() throws Exception {
        SmsVerifyDto request = new SmsVerifyDto();
        request.setPhone("01098765432");
        request.setCertificationCode("654321");

        when(smsService.verifyCertificationNumber(anyString(), anyString())).thenReturn(true);
        when(authService.handlePhoneVerification(anyString())).thenReturn(
                SmsVerificationResponse.builder()
                        .verified(true)
                        .message("인증이 완료되었습니다. 회원 정보를 입력해주세요.")
                        .memberStatus(MemberStatus.NEW_MEMBER)
                        .nextAction("REGISTER")
                        .token("sample-phone-token")
                        .build()
        );

        mockMvc.perform(post("/verifications/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.memberStatus").value("NEW_MEMBER"))
                .andExpect(jsonPath("$.nextAction").value("REGISTER"));
    }

    @Test
    @DisplayName("SMS 인증 실패 - 잘못된 인증번호")
    void verifySms_fail_wrongCode() throws Exception {
        SmsVerifyDto request = new SmsVerifyDto();
        request.setPhone("01012345678");
        request.setCertificationCode("000000");

        when(smsService.verifyCertificationNumber(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/verifications/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.message").value("인증번호가 올바르지 않거나 만료되었습니다."));
    }

}