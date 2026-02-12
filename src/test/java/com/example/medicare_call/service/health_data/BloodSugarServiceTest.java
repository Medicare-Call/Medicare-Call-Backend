package com.example.medicare_call.service.health_data;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.enums.CareCallResponseStatus;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodSugarServiceTest {

    @Mock
    private BloodSugarRecordRepository bloodSugarRecordRepository;

    @InjectMocks
    private BloodSugarService bloodSugarService;

    private CareCallRecord testCallRecord;
    private HealthDataExtractionResponse.BloodSugarData testBloodSugarData;

    @BeforeEach
    void setUp() {
        testCallRecord = CareCallRecord.builder()
                .id(1)
                .elder(null)
                .setting(null)
                .calledAt(LocalDateTime.now())
                .responded(CareCallResponseStatus.RESPONDED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(15))
                .callStatus("completed")
                .transcriptionText("테스트 통화 내용")
                .build();

        testBloodSugarData = HealthDataExtractionResponse.BloodSugarData.builder()
                .measurementTime("아침")
                .mealTime("식후")
                .bloodSugarValue(120)
                .status("NORMAL")
                .build();
    }

    @Test
    @DisplayName("혈당 데이터 저장 성공 - 정상 상태")
    void saveBloodSugarData_성공_정상상태() {
        // given
        when(bloodSugarRecordRepository.save(any(BloodSugarRecord.class)))
                .thenReturn(new BloodSugarRecord());

        // when
        bloodSugarService.saveBloodSugarData(testCallRecord, List.of(testBloodSugarData));

        // then
        ArgumentCaptor<BloodSugarRecord> captor = ArgumentCaptor.forClass(BloodSugarRecord.class);
        verify(bloodSugarRecordRepository).save(captor.capture());

        BloodSugarRecord savedRecord = captor.getValue();
        assertThat(savedRecord.getCareCallRecord()).isEqualTo(testCallRecord);
        assertThat(savedRecord.getBlood_sugar_value()).isEqualTo(BigDecimal.valueOf(120));
        assertThat(savedRecord.getMeasurementType()).isEqualTo(BloodSugarMeasurementType.AFTER_MEAL);
        assertThat(savedRecord.getStatus()).isEqualTo(BloodSugarStatus.NORMAL);
        assertThat(savedRecord.getResponseSummary()).contains("측정시각: 아침, 식전/식후: 식후");
    }

    @Test
    @DisplayName("혈당 데이터 저장 성공 - 고혈당 상태")
    void saveBloodSugarData_성공_고혈당상태() {
        // given
        testBloodSugarData = HealthDataExtractionResponse.BloodSugarData.builder()
                .measurementTime("저녁")
                .mealTime("식후")
                .bloodSugarValue(200)
                .status("HIGH")
                .build();

        when(bloodSugarRecordRepository.save(any(BloodSugarRecord.class)))
                .thenReturn(new BloodSugarRecord());

        // when
        bloodSugarService.saveBloodSugarData(testCallRecord, List.of(testBloodSugarData));

        // then
        ArgumentCaptor<BloodSugarRecord> captor = ArgumentCaptor.forClass(BloodSugarRecord.class);
        verify(bloodSugarRecordRepository).save(captor.capture());

        BloodSugarRecord savedRecord = captor.getValue();
        assertThat(savedRecord.getStatus()).isEqualTo(BloodSugarStatus.HIGH);
        assertThat(savedRecord.getBlood_sugar_value()).isEqualTo(BigDecimal.valueOf(200));
    }

    @Test
    @DisplayName("혈당 데이터 저장 성공 - 저혈당 상태")
    void saveBloodSugarData_성공_저혈당상태() {
        // given
        testBloodSugarData = HealthDataExtractionResponse.BloodSugarData.builder()
                .measurementTime("아침")
                .mealTime("식전")
                .bloodSugarValue(70)
                .status("LOW")
                .build();

        when(bloodSugarRecordRepository.save(any(BloodSugarRecord.class)))
                .thenReturn(new BloodSugarRecord());

        // when
        bloodSugarService.saveBloodSugarData(testCallRecord, List.of(testBloodSugarData));

        // then
        ArgumentCaptor<BloodSugarRecord> captor = ArgumentCaptor.forClass(BloodSugarRecord.class);
        verify(bloodSugarRecordRepository).save(captor.capture());

        BloodSugarRecord savedRecord = captor.getValue();
        assertThat(savedRecord.getStatus()).isEqualTo(BloodSugarStatus.LOW);
        assertThat(savedRecord.getMeasurementType()).isEqualTo(BloodSugarMeasurementType.BEFORE_MEAL);
    }

    @Test
    @DisplayName("혈당 데이터 저장 성공 - status가 null인 경우")
    void saveBloodSugarData_성공_status_null() {
        // given
        testBloodSugarData = HealthDataExtractionResponse.BloodSugarData.builder()
                .measurementTime("아침")
                .mealTime("식후")
                .bloodSugarValue(120)
                .status(null)
                .build();

        when(bloodSugarRecordRepository.save(any(BloodSugarRecord.class)))
                .thenReturn(new BloodSugarRecord());

        // when
        bloodSugarService.saveBloodSugarData(testCallRecord, List.of(testBloodSugarData));

        // then
        ArgumentCaptor<BloodSugarRecord> captor = ArgumentCaptor.forClass(BloodSugarRecord.class);
        verify(bloodSugarRecordRepository).save(captor.capture());

        BloodSugarRecord savedRecord = captor.getValue();
        assertThat(savedRecord.getStatus()).isNull();
    }

    @Test
    @DisplayName("혈당 데이터 저장 실패 - 혈당 값이 null인 경우")
    void saveBloodSugarData_실패_혈당값_null() {
        // given
        testBloodSugarData = HealthDataExtractionResponse.BloodSugarData.builder()
                .measurementTime("아침")
                .mealTime("식후")
                .bloodSugarValue(null)
                .status("NORMAL")
                .build();

        // when
        bloodSugarService.saveBloodSugarData(testCallRecord, List.of(testBloodSugarData));

        // then
        // 혈당 값이 null이면 저장하지 않음
        verify(bloodSugarRecordRepository, org.mockito.Mockito.never()).save(any(BloodSugarRecord.class));
    }
}