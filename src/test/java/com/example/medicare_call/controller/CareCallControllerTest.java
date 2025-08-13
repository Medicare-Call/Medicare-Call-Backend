package com.example.medicare_call.controller;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.service.carecall.CareCallRequestSenderService;
import com.example.medicare_call.service.carecall.CareCallSettingService;
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
import java.time.LocalTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.medicare_call.global.ResourceNotFoundException;

@WebMvcTest(CareCallController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CareCallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CareCallDataProcessingService careCallDataProcessingService;
    @MockBean
    private CareCallSettingService careCallSettingService;
    @MockBean
    private CareCallRequestSenderService careCallRequestSenderService;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private MemberRepository memberRepository;

    @Test
    @DisplayName("통화 데이터 수신 성공")
    void receiveCallData_success() throws Exception {
        // given
        CareCallDataProcessRequest.TranscriptionData.TranscriptionSegment segment1 = CareCallDataProcessRequest.TranscriptionData.TranscriptionSegment.builder()
                .speaker("고객")
                .text("안녕하세요, 오늘 컨디션은 어떠세요?")
                .build();

        CareCallDataProcessRequest.TranscriptionData.TranscriptionSegment segment2 = CareCallDataProcessRequest.TranscriptionData.TranscriptionSegment.builder()
                .speaker("어르신")
                .text("네, 오늘은 컨디션이 좋아요.")
                .build();

        CareCallDataProcessRequest.TranscriptionData transcriptionData = CareCallDataProcessRequest.TranscriptionData.builder()
                .language("ko")
                .fullText(Arrays.asList(segment1, segment2))
                .build();

        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(1)
                .settingId(2)
                .startTime(Instant.parse("2025-01-27T10:00:00Z"))
                .endTime(Instant.parse("2025-01-27T10:15:00Z"))
                .status("completed")
                .responded((byte)1)
                .transcription(transcriptionData)
                .build();

        Elder elder = Elder.builder().id(1).build();
        CareCallSetting setting = CareCallSetting.builder().id(2).build();

        CareCallRecord savedRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .startTime(LocalDateTime.parse("2025-01-27T10:00:00"))
                .endTime(LocalDateTime.parse("2025-01-27T10:15:00"))
                .callStatus("completed")
                .transcriptionText("고객: 안녕하세요, 오늘 컨디션은 어떠세요?\n어르신: 네, 오늘은 컨디션이 좋아요.")
                .build();

        when(careCallDataProcessingService.saveCallData(any(CareCallDataProcessRequest.class))).thenReturn(savedRecord);

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
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
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
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
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
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
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
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
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
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(1)
                .settingId(2)
                .status("completed")
                .responded((byte)1)
                .build();

        Elder elder = Elder.builder().id(1).build();
        CareCallSetting setting = CareCallSetting.builder().id(2).build();

        CareCallRecord savedRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .callStatus("completed")
                .build();

        when(careCallDataProcessingService.saveCallData(any(CareCallDataProcessRequest.class))).thenReturn(savedRecord);

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("통화 데이터 수신 실패 (없는 어르신 ID)")
    void receiveCallData_elderNotFound() throws Exception {
        // given
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(999) // 존재하지 않는 어르신 ID
                .settingId(2)
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(60))
                .status("completed")
                .responded((byte) 1)
                .build();

        when(careCallDataProcessingService.saveCallData(any(CareCallDataProcessRequest.class)))
                .thenThrow(new ResourceNotFoundException("해당 어르신을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("통화 데이터 수신 실패 (없는 설정 ID)")
    void receiveCallData_settingNotFound() throws Exception {
        // given
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(1)
                .settingId(999) // 존재하지 않는 설정 ID
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(60))
                .status("completed")
                .responded((byte) 1)
                .build();
        
        when(careCallDataProcessingService.saveCallData(any(CareCallDataProcessRequest.class)))
                .thenThrow(new ResourceNotFoundException("해당 케어콜 설정을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("통화 데이터 수신 실패 (데이터 처리 실패)")
    void receiveCallData_processingFailed() throws Exception {
        // given
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(1)
                .settingId(2)
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(60))
                .status("completed")
                .responded((byte) 1)
                .build();

        when(careCallDataProcessingService.saveCallData(any(CareCallDataProcessRequest.class)))
                .thenThrow(new RuntimeException("데이터 처리 실패"));

        // when & then
        mockMvc.perform(post("/call-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // From CareCallSettingController
    @Test
    @DisplayName("케어콜 시간 설정 성공")
    void settingCarCallInfo_success() throws Exception {
        // given
        CareCallSettingRequest request = new CareCallSettingRequest(
                LocalTime.of(9, 0),
                LocalTime.of(14, 0),
                LocalTime.of(19, 0)
        );

        doNothing().when(careCallSettingService).settingCareCall(any(Integer.class), any(CareCallSettingRequest.class));

        // when & then
        mockMvc.perform(post("/elders/1/care-call-setting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // From TestCareCallController
    @Test
    @DisplayName("테스트 케어콜 발송 성공")
    void testCareCall_success() throws Exception {
        // given
        CareCallTestRequest request = new CareCallTestRequest("01012345678", "테스트 프롬프트입니다.");

        doNothing().when(careCallRequestSenderService).sendTestCall(any(CareCallTestRequest.class));

        // when & then
        mockMvc.perform(post("/test-care-call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(request.prompt()));
    }
}
