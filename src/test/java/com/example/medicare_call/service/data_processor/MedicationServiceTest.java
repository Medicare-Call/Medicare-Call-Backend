package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.medicare_call.dto.report.DailyMedicationResponse;
import com.example.medicare_call.global.enums.MedicationTakenStatus;
import com.example.medicare_call.repository.ElderRepository;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class MedicationServiceTest {

    @Mock
    private MedicationTakenRecordRepository medicationTakenRecordRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private ElderRepository elderRepository;

    @InjectMocks
    private MedicationService medicationService;

    private CareCallRecord callRecord;
    private Elder elder;
    private Medication medication;
    private MedicationSchedule medicationSchedule;

    @BeforeEach
    void setUp() {
        elder = Elder.builder()
                .id(1)
                .name("테스트 어르신")
                .build();

        callRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .build();

        medication = Medication.builder()
                .id(1)
                .name("혈압약")
                .build();

        medicationSchedule = MedicationSchedule.builder()
                .id(1)
                .elder(elder)
                .medication(medication)
                .scheduleTime("MORNING")
                .build();
    }

    @Test
    @DisplayName("복약 데이터 저장 성공 - 스케줄 매칭됨")
    void saveMedicationTakenRecord_success_withScheduleMatch() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("혈압약")
                .taken("복용함")
                .takenTime("아침")
                .build();

        when(medicationRepository.findByName("혈압약")).thenReturn(Optional.of(medication));
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(Arrays.asList(medicationSchedule));
        when(medicationTakenRecordRepository.save(any(MedicationTakenRecord.class))).thenReturn(MedicationTakenRecord.builder().id(1).build());

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationRepository).findByName("혈압약");
        verify(medicationScheduleRepository).findByElder(elder);
        verify(medicationTakenRecordRepository).save(argThat(record -> 
            record.getMedication().getId().equals(1) &&
            record.getMedicationSchedule().getId().equals(1) &&
            record.getTakenStatus().name().equals("TAKEN")
        ));
    }

    @Test
    @DisplayName("복약 데이터 저장 성공 - 스케줄 매칭 안됨")
    void saveMedicationTakenRecord_success_withoutScheduleMatch() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("혈압약")
                .taken("복용함")
                .takenTime("점심")
                .build();

        when(medicationRepository.findByName("혈압약")).thenReturn(Optional.of(medication));
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(Arrays.asList(medicationSchedule));
        when(medicationTakenRecordRepository.save(any(MedicationTakenRecord.class))).thenReturn(MedicationTakenRecord.builder().id(1).build());

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationTakenRecordRepository).save(argThat(record -> 
            record.getMedication().getId().equals(1) &&
            record.getMedicationSchedule() == null &&
            record.getTakenStatus().name().equals("TAKEN")
        ));
    }

    @Test
    @DisplayName("복약 데이터 저장 성공 - 복용하지 않음")
    void saveMedicationTakenRecord_success_notTaken() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("혈압약")
                .taken("복용하지 않음")
                .takenTime("아침")
                .build();

        when(medicationRepository.findByName("혈압약")).thenReturn(Optional.of(medication));
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(Arrays.asList(medicationSchedule));
        when(medicationTakenRecordRepository.save(any(MedicationTakenRecord.class))).thenReturn(MedicationTakenRecord.builder().id(1).build());

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationTakenRecordRepository).save(argThat(record -> 
            record.getTakenStatus().name().equals("NOT_TAKEN")
        ));
    }

    @Test
    @DisplayName("복약 데이터 저장 건너뛰기 - 약 이름이 null일 경우")
    void saveMedicationTakenRecord_skip_whenMedicationTypeIsNull() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType(null)
                .taken("복용함")
                .takenTime("아침")
                .build();

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationRepository, never()).findByName(any());
        verify(medicationTakenRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("복약 데이터 저장 건너뛰기 - 약 이름이 비어있을 경우")
    void saveMedicationTakenRecord_skip_whenMedicationTypeIsEmpty() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("  ") // 공백 문자열
                .taken("복용함")
                .takenTime("아침")
                .build();

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationRepository, never()).findByName(any());
        verify(medicationTakenRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("복약 데이터 저장 실패 - 약을 찾을 수 없음")
    void saveMedicationTakenRecord_fail_medicationNotFound() {
        // given
        CareCallRecord record = new CareCallRecord();
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("존재하지 않는 약")
                .taken("복용함")
                .takenTime("아침")
                .build();

        when(medicationRepository.findByName("존재하지 않는 약")).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            medicationService.saveMedicationTakenRecord(record, medicationData);
        });
        assertEquals(ErrorCode.MEDICATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("스케줄 시간 매칭 테스트 - 아침")
    void scheduleTimeMatching_morning() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("혈압약")
                .taken("복용함")
                .takenTime("아침")
                .build();

        MedicationSchedule morningSchedule = MedicationSchedule.builder()
                .id(1)
                .elder(elder)
                .medication(medication)
                .scheduleTime("MORNING,LUNCH")
                .build();

        when(medicationRepository.findByName("혈압약")).thenReturn(Optional.of(medication));
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(Arrays.asList(morningSchedule));
        when(medicationTakenRecordRepository.save(any(MedicationTakenRecord.class))).thenReturn(MedicationTakenRecord.builder().id(1).build());

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationTakenRecordRepository).save(argThat(record -> 
            record.getMedicationSchedule() != null &&
            record.getMedicationSchedule().getId().equals(1)
        ));
    }

    @Test
    @DisplayName("스케줄 시간 매칭 테스트 - 점심")
    void scheduleTimeMatching_lunch() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("혈압약")
                .taken("복용함")
                .takenTime("점심")
                .build();

        MedicationSchedule lunchSchedule = MedicationSchedule.builder()
                .id(1)
                .elder(elder)
                .medication(medication)
                .scheduleTime("MORNING,LUNCH")
                .build();

        when(medicationRepository.findByName("혈압약")).thenReturn(Optional.of(medication));
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(Arrays.asList(lunchSchedule));
        when(medicationTakenRecordRepository.save(any(MedicationTakenRecord.class))).thenReturn(MedicationTakenRecord.builder().id(1).build());

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationTakenRecordRepository).save(argThat(record -> 
            record.getMedicationSchedule() != null &&
            record.getMedicationSchedule().getId().equals(1)
        ));
    }

    @Test
    @DisplayName("스케줄 시간 매칭 테스트 - 매칭 안됨")
    void scheduleTimeMatching_noMatch() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("혈압약")
                .taken("복용함")
                .takenTime("저녁")
                .build();

        MedicationSchedule morningSchedule = MedicationSchedule.builder()
                .id(1)
                .elder(elder)
                .medication(medication)
                .scheduleTime("MORNING,LUNCH")
                .build();

        when(medicationRepository.findByName("혈압약")).thenReturn(Optional.of(medication));
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(Arrays.asList(morningSchedule));
        when(medicationTakenRecordRepository.save(any(MedicationTakenRecord.class))).thenReturn(MedicationTakenRecord.builder().id(1).build());

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationTakenRecordRepository).save(argThat(record -> 
            record.getMedicationSchedule() == null
        ));
    }

    @Test
    @DisplayName("날짜별 복약 데이터 조회 성공")
    void getDailyMedication_Success() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);

        Elder elder = Elder.builder()
                .id(elderId)
                .name("테스트 어르신")
                .build();

        Medication medication1 = Medication.builder()
                .id(1)
                .name("당뇨약")
                .build();

        Medication medication2 = Medication.builder()
                .id(2)
                .name("혈압약")
                .build();

        MedicationSchedule schedule1 = MedicationSchedule.builder()
                .id(1)
                .elder(elder)
                .medication(medication1)
                .scheduleTime("MORNING")
                .build();

        MedicationSchedule schedule2 = MedicationSchedule.builder()
                .id(2)
                .elder(elder)
                .medication(medication1)
                .scheduleTime("LUNCH")
                .build();

        MedicationSchedule schedule3 = MedicationSchedule.builder()
                .id(3)
                .elder(elder)
                .medication(medication1)
                .scheduleTime("DINNER")
                .build();

        MedicationSchedule schedule4 = MedicationSchedule.builder()
                .id(4)
                .elder(elder)
                .medication(medication2)
                .scheduleTime("MORNING")
                .build();

        MedicationSchedule schedule5 = MedicationSchedule.builder()
                .id(5)
                .elder(elder)
                .medication(medication2)
                .scheduleTime("DINNER")
                .build();

        CareCallRecord callRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .build();

        MedicationTakenRecord takenRecord1 = MedicationTakenRecord.builder()
                .id(1)
                .careCallRecord(callRecord)
                .medicationSchedule(schedule1)
                .medication(medication1)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .build();

        MedicationTakenRecord takenRecord2 = MedicationTakenRecord.builder()
                .id(2)
                .careCallRecord(callRecord)
                .medicationSchedule(schedule2)
                .medication(medication1)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .build();

        MedicationTakenRecord takenRecord3 = MedicationTakenRecord.builder()
                .id(3)
                .careCallRecord(callRecord)
                .medicationSchedule(schedule4)
                .medication(medication2)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .build();

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(medicationTakenRecordRepository.findByElderIdAndDate(elderId, date))
                .thenReturn(Arrays.asList(takenRecord1, takenRecord2, takenRecord3));
        when(medicationScheduleRepository.findByElder(elder))
                .thenReturn(Arrays.asList(schedule1, schedule2, schedule3, schedule4, schedule5));

        // when
        DailyMedicationResponse result = medicationService.getDailyMedication(elderId, date);

        // then
        assertThat(result.getDate()).isEqualTo(date);
        assertThat(result.getMedications()).hasSize(2);

        // 당뇨약 검증
        DailyMedicationResponse.MedicationInfo diabetesMedication = result.getMedications().stream()
                .filter(med -> med.getType().equals("당뇨약"))
                .findFirst()
                .orElse(null);
        assertThat(diabetesMedication).isNotNull();
        assertThat(diabetesMedication.getGoalCount()).isEqualTo(3);
        assertThat(diabetesMedication.getTakenCount()).isEqualTo(2);
        assertThat(diabetesMedication.getTimes()).hasSize(3);

        // 혈압약 검증
        DailyMedicationResponse.MedicationInfo bloodPressureMedication = result.getMedications().stream()
                .filter(med -> med.getType().equals("혈압약"))
                .findFirst()
                .orElse(null);
        assertThat(bloodPressureMedication).isNotNull();
        assertThat(bloodPressureMedication.getGoalCount()).isEqualTo(2);
        assertThat(bloodPressureMedication.getTakenCount()).isEqualTo(1);
        assertThat(bloodPressureMedication.getTimes()).hasSize(2);
    }

    @Test
    @DisplayName("날짜별 복약 데이터 조회 실패 - 어르신을 찾을 수 없음")
    void getDailyMedication_Fail_ElderNotFound() {
        // given
        Integer elderId = 999;
        LocalDate date = LocalDate.of(2025, 7, 16);

        when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            medicationService.getDailyMedication(elderId, date);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("복약 데이터 조회 성공 - 데이터 없음")
    void getDailyMedication_ThrowsResourceNotFoundException() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(new Elder()));

        // when
        DailyMedicationResponse response = medicationService.getDailyMedication(elderId, date);

        // then
        assertThat(response).isNotNull();
        assertEquals(date, response.getDate());
        assertTrue(response.getMedications().isEmpty());
    }
} 