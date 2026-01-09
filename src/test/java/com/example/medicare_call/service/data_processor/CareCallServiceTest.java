package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.event.CareCallCompletedEvent;
import com.example.medicare_call.global.event.Events;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareCallServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private CareCallSettingRepository careCallSettingRepository;

    @InjectMocks
    private CareCallService careCallService;
    
    private MockedStatic<Events> eventsMockedStatic;

    @BeforeEach
    void setUp() {
        eventsMockedStatic = Mockito.mockStatic(Events.class);
    }
    
    @AfterEach
    void tearDown() {
        eventsMockedStatic.close();
    }

    @Test
    @DisplayName("통화 데이터 저장 성공 - 모든 필드 포함")
    void saveCallData_success_withAllFields() {
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
                .status(CareCallStatus.COMPLETED)
                .responded((byte) 1)
                .transcription(transcriptionData)
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();
        
        CareCallSetting setting = CareCallSetting.builder()
                .id(2)
                .firstCallTime(LocalTime.parse("06:00:00"))
                .secondCallTime(LocalTime.parse("08:00:00"))
                .thirdCallTime(LocalTime.parse("10:01:00"))
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
        CareCallRecord result = careCallService.saveCallData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCallStatus()).isEqualTo("completed");

        assertThat(result.getTranscriptionText()).isEqualTo("고객: 안녕하세요, 오늘 컨디션은 어떠세요?\n어르신: 네, 오늘은 컨디션이 좋아요.");
        
        verify(elderRepository).findById(1);
        verify(careCallSettingRepository).findById(2);
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
        
        // 이벤트 발생 검증
        eventsMockedStatic.verify(() -> Events.raise(any(CareCallCompletedEvent.class)), times(1));
    }

    @Test
    @DisplayName("통화 데이터 저장 성공 - 녹음 텍스트 없음")
    void saveCallData_success_withoutTranscription() {
        // given
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(1)
                .settingId(2)
                .status(CareCallStatus.FAILED)
                .responded((byte) 1)
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
        CareCallRecord result = careCallService.saveCallData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCallStatus()).isEqualTo("failed");

        assertThat(result.getTranscriptionText()).isNull();
        
        verify(elderRepository).findById(1);
        verify(careCallSettingRepository).findById(2);
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
        eventsMockedStatic.verify(() -> Events.raise(any(CareCallCompletedEvent.class)), times(1));
    }

    @Test
    @DisplayName("통화 데이터 저장 성공 - 녹음 텍스트 없음")
    void saveCallData_success_transcriptionTextOnly() {
        // given
        CareCallDataProcessRequest.TranscriptionData transcriptionData = CareCallDataProcessRequest.TranscriptionData.builder()
                .build();

        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(1)
                .settingId(2)
                .status(CareCallStatus.BUSY)
                .responded((byte) 1)
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
        CareCallRecord result = careCallService.saveCallData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCallStatus()).isEqualTo("busy");
        assertThat(result.getTranscriptionText()).isNull();
        
        verify(elderRepository).findById(1);
        verify(careCallSettingRepository).findById(2);
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
        eventsMockedStatic.verify(() -> Events.raise(any(CareCallCompletedEvent.class)), times(1));
    }

    @Test
    @DisplayName("통화 데이터 저장 실패 - 어르신을 찾을 수 없음")
    void saveCallData_fail_elderNotFound() {
        // given
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(999)
                .settingId(2)
                .status(CareCallStatus.COMPLETED)
                .responded((byte) 1)
                .build();

        when(elderRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> careCallService.saveCallData(request)
        );
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
        verify(careCallRecordRepository, never()).save(any());
        eventsMockedStatic.verify(() -> Events.raise(any(CareCallCompletedEvent.class)), never());
    }

    @Test
    @DisplayName("통화 데이터 저장 실패 - 통화 설정을 찾을 수 없음")
    void saveCallData_fail_settingNotFound() {
        // given
        CareCallDataProcessRequest request = CareCallDataProcessRequest.builder()
                .elderId(1)
                .settingId(999)
                .status(CareCallStatus.COMPLETED)
                .responded((byte) 1)
                .build();

        Elder elder = Elder.builder()
                .id(1)
                .build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(careCallSettingRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            careCallService.saveCallData(request);
        });
        assertEquals(ErrorCode.CARE_CALL_SETTING_NOT_FOUND, exception.getErrorCode());
        verify(careCallRecordRepository, never()).save(any());
        eventsMockedStatic.verify(() -> Events.raise(any(CareCallCompletedEvent.class)), never());
    }
} 
