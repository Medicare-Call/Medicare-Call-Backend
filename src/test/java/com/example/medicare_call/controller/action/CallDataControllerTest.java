package com.example.medicare_call.controller.action;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.CallDataRequest;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.service.data_processor.CareCallDataProcessingService;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CallDataController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class CallDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CareCallDataProcessingService careCallDataProcessingService;

    @MockBean
    private ElderRepository elderRepository;

    @MockBean
    private CareCallSettingRepository careCallSettingRepository;

    @MockBean
    private CareCallRecordRepository careCallRecordRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private MemberRepository memberRepository;

    @Test
    @DisplayName("통화 데이터 수신 성공")
    void receiveCallData_success() throws Exception {
        // given
        CallDataRequest.TranscriptionData.TranscriptionSegment segment1 = CallDataRequest.TranscriptionData.TranscriptionSegment.builder()
                .speaker("고객")
                .text("안녕하세요, 오늘 컨디션은 어떠세요?")
                .build();

        CallDataRequest.TranscriptionData.TranscriptionSegment segment2 = CallDataRequest.TranscriptionData.TranscriptionSegment.builder()
                .speaker("어르신")
                .text("네, 오늘은 컨디션이 좋아요.")
                .build();

        CallDataRequest.TranscriptionData transcriptionData = CallDataRequest.TranscriptionData.builder()
                .language("ko")
                .fullText(Arrays.asList(segment1, segment2))
                .build();

        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(2)
                .startTime(Instant.parse("2025-01-27T10:00:00Z"))
                .endTime(Instant.parse("2025-01-27T10:15:00Z"))
                .status("completed")
                .responded((byte)1)
                .transcription(transcriptionData)
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();

        CareCallSetting setting = CareCallSetting.builder()
                .id(2)
                .build();

        CareCallRecord savedRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .startTime(LocalDateTime.parse("2025-01-27T10:00:00"))
                .endTime(LocalDateTime.parse("2025-01-27T10:15:00"))
                .callStatus("completed")
                .transcriptionText("고객: 안녕하세요, 오늘 컨디션은 어떠세요?\n어르신: 네, 오늘은 컨디션이 좋아요.")
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();

        when(careCallDataProcessingService.saveCallData(any(CallDataRequest.class))).thenReturn(savedRecord);

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("통화 데이터 수신 실패 - 어르신 ID 누락")
    void receiveCallData_fail_missingElderId() throws Exception {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .settingId(2)
                .status("completed")
                .responded((byte)1)
                .build();

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통화 데이터 수신 실패 - 통화 설정 ID 누락")
    void receiveCallData_fail_missingSettingId() throws Exception {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .status("completed")
                .build();

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통화 데이터 수신 실패 - 통화 상태 누락")
    void receiveCallData_fail_missingStatus() throws Exception {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(2)
                .build();

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통화 데이터 수신 실패 - 잘못된 통화 상태")
    void receiveCallData_fail_invalidStatus() throws Exception {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(2)
                .status("invalid-status")
                .build();

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("통화 데이터 수신 성공 - 녹음 텍스트 없음")
    void receiveCallData_success_noTranscription() throws Exception {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(2)
                .status("completed")
                .responded((byte)1)
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();

        CareCallSetting setting = CareCallSetting.builder()
                .id(2)
                .build();

        CareCallRecord savedRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .callStatus("completed")
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();

        when(careCallDataProcessingService.saveCallData(any(CallDataRequest.class))).thenReturn(savedRecord);

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().string(""));
    }
}