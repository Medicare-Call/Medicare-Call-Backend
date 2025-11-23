package com.example.medicare_call.controller;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.service.carecall.CareCallRequestSenderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.example.medicare_call.service.carecall.CareCallSettingService;
import com.example.medicare_call.service.data_processor.CareCallDataProcessingService;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.global.annotation.AuthenticationArgumentResolver;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest;
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest.CareCallOption;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CareCallController.class, 
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import(TestConfig.class)
@DisplayName("즉시 케어콜 컨트롤러 테스트")
class CareCallControllerImmediateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CareCallRequestSenderService careCallRequestSenderService;

    @MockBean
    private CareCallSettingService careCallSettingService;

    @MockBean
    private CareCallDataProcessingService careCallDataProcessingService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private ElderRepository elderRepository;

    @MockBean
    private CareCallSettingRepository careCallSettingRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AuthenticationArgumentResolver authenticationArgumentResolver;

    @Autowired
    private ObjectMapper objectMapper;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1)
                .name("김보호자")
                .phone("01012345678")
                .gender((byte) 1)
                .termsAgreedAt(LocalDateTime.now())
                .plan((byte) 1)
                .build();

    }

    @Test
    @DisplayName("요청된 어르신에게 즉시 케어콜 발송 - 성공")
    void sendImmediateCareCall_Success() throws Exception {
        // given
        ImmediateCareCallRequest request = new ImmediateCareCallRequest();
        request.setElderId(1L);
        request.setCareCallOption(CareCallOption.FIRST);

        String expectedResult = "김할머니 어르신께 즉시 케어콜 발송이 완료되었습니다.";
        
        when(careCallRequestSenderService.sendImmediateCall(request.getElderId(), request.getCareCallOption()))
                .thenReturn(expectedResult);

        // when & then
        mockMvc.perform(post("/care-call/immediate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    @DisplayName("요청된 어르신이 존재하지 않는 경우")
    void sendImmediateCareCall_ElderNotFound() throws Exception {
        // given
        ImmediateCareCallRequest request = new ImmediateCareCallRequest();
        request.setElderId(999L); // 존재하지 않는 ID
        request.setCareCallOption(CareCallOption.FIRST);
        
        when(careCallRequestSenderService.sendImmediateCall(request.getElderId(), request.getCareCallOption()))
                .thenThrow(new CustomException(ErrorCode.ELDER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/care-call/immediate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
