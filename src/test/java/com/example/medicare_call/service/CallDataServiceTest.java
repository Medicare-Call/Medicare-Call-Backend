package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.CallDataRequest;
import com.example.medicare_call.dto.HealthDataExtractionRequest;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallDataServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private CareCallSettingRepository careCallSettingRepository;

    @Mock
    private OpenAiHealthDataService openAiHealthDataService;

    @InjectMocks
    private CallDataService callDataService;

    @Test
    @DisplayName("통화 데이터 저장 성공 - 모든 필드 포함")
    void saveCallData_success_withAllFields() {
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
                .transcription(transcriptionData)
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();
        
        CareCallSetting setting = CareCallSetting.builder()
                .id(2)
                .build();
        
        CareCallRecord expectedRecord = CareCallRecord.builder()
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

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(careCallSettingRepository.findById(2)).thenReturn(Optional.of(setting));
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataService.saveCallData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCallStatus()).isEqualTo("completed");

        assertThat(result.getTranscriptionText()).isEqualTo("고객: 안녕하세요, 오늘 컨디션은 어떠세요?\n어르신: 네, 오늘은 컨디션이 좋아요.");
        
        verify(elderRepository).findById(1);
        verify(careCallSettingRepository).findById(2);
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
        verify(openAiHealthDataService).extractHealthData(argThat(healthRequest -> 
            healthRequest.getTranscriptionText().equals("고객: 안녕하세요, 오늘 컨디션은 어떠세요?\n어르신: 네, 오늘은 컨디션이 좋아요.") &&
            healthRequest.getCallDate().equals("2025-01-27")
        ));
    }

    @Test
    @DisplayName("통화 데이터 저장 성공 - 녹음 텍스트 없음")
    void saveCallData_success_withoutTranscription() {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(2)
                .status("failed")
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();
        
        CareCallSetting setting = CareCallSetting.builder()
                .id(2)
                .build();
        
        CareCallRecord expectedRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .callStatus("failed")
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(careCallSettingRepository.findById(2)).thenReturn(Optional.of(setting));
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataService.saveCallData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCallStatus()).isEqualTo("failed");

        assertThat(result.getTranscriptionText()).isNull();
        
        verify(elderRepository).findById(1);
        verify(careCallSettingRepository).findById(2);
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
        verify(openAiHealthDataService, never()).extractHealthData(any());
    }

    @Test
    @DisplayName("통화 데이터 저장 성공 - 녹음 텍스트 없음")
    void saveCallData_success_transcriptionTextOnly() {
        // given
        CallDataRequest.TranscriptionData transcriptionData = CallDataRequest.TranscriptionData.builder()
                .build();

        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(2)
                .status("busy")
                .transcription(transcriptionData)
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();
        
        CareCallSetting setting = CareCallSetting.builder()
                .id(2)
                .build();
        
        CareCallRecord expectedRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .callStatus("busy")
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(careCallSettingRepository.findById(2)).thenReturn(Optional.of(setting));
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataService.saveCallData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCallStatus()).isEqualTo("busy");
        assertThat(result.getTranscriptionText()).isNull();
        
        verify(elderRepository).findById(1);
        verify(careCallSettingRepository).findById(2);
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
        verify(openAiHealthDataService, never()).extractHealthData(any());
    }

    @Test
    @DisplayName("통화 데이터 저장 실패 - 어르신을 찾을 수 없음")
    void saveCallData_fail_elderNotFound() {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .elderId(999)
                .settingId(2)
                .status("completed")
                .build();

        when(elderRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> callDataService.saveCallData(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("어르신을 찾을 수 없습니다: 999");
        
        verify(elderRepository).findById(999);
        verify(careCallSettingRepository, never()).findById(any());
        verify(openAiHealthDataService, never()).extractHealthData(any());
    }

    @Test
    @DisplayName("통화 데이터 저장 실패 - 통화 설정을 찾을 수 없음")
    void saveCallData_fail_settingNotFound() {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(999)
                .status("completed")
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(careCallSettingRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> callDataService.saveCallData(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("통화 설정을 찾을 수 없습니다: 999");
        
        verify(elderRepository).findById(1);
        verify(careCallSettingRepository).findById(999);
        verify(openAiHealthDataService, never()).extractHealthData(any());
    }

    @Test
    @DisplayName("통화 데이터 저장 성공 - 시간 정보 없음")
    void saveCallData_success_noTimeInfo() {
        // given
        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(2)
                .status("no-answer")
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();
        
        CareCallSetting setting = CareCallSetting.builder()
                .id(2)
                .build();
        
        CareCallRecord expectedRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .callStatus("no-answer")
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(careCallSettingRepository.findById(2)).thenReturn(Optional.of(setting));
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataService.saveCallData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCallStatus()).isEqualTo("no-answer");
        assertThat(result.getStartTime()).isNull();
        assertThat(result.getEndTime()).isNull();
        
        verify(elderRepository).findById(1);
        verify(careCallSettingRepository).findById(2);
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
        verify(openAiHealthDataService, never()).extractHealthData(any());
    }

    @Test
    @DisplayName("통화 데이터 저장 시 OpenAI 건강 데이터 추출 서비스가 호출된다")
    void saveCallData_callsOpenAiHealthDataService() {
        // given
        CallDataRequest.TranscriptionData.TranscriptionSegment segment = CallDataRequest.TranscriptionData.TranscriptionSegment.builder()
                .speaker("어르신")
                .text("오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요.")
                .build();

        CallDataRequest.TranscriptionData transcriptionData = CallDataRequest.TranscriptionData.builder()
                .fullText(Arrays.asList(segment))
                .build();

        CallDataRequest request = CallDataRequest.builder()
                .elderId(1)
                .settingId(2)
                .startTime(Instant.parse("2025-01-27T10:00:00Z"))
                .status("completed")
                .transcription(transcriptionData)
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();
        
        CareCallSetting setting = CareCallSetting.builder()
                .id(2)
                .build();
        
        CareCallRecord expectedRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .startTime(LocalDateTime.parse("2025-01-27T10:00:00"))
                .callStatus("completed")
                .transcriptionText("어르신: 오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요.")
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(careCallSettingRepository.findById(2)).thenReturn(Optional.of(setting));
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataService.saveCallData(request);

        // then
        assertThat(result).isNotNull();
        verify(openAiHealthDataService).extractHealthData(argThat(healthRequest -> 
            healthRequest.getTranscriptionText().equals("어르신: 오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요.") &&
            healthRequest.getCallDate().equals("2025-01-27")
        ));
    }
} 