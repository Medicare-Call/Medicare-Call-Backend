package com.example.medicare_call.service;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.repository.MedicationRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.repository.MedicationTakenRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationTakenRecordRepository medicationTakenRecordRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private MedicationRepository medicationRepository;

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
    @DisplayName("복약 데이터 저장 실패 - 약을 찾을 수 없음")
    void saveMedicationTakenRecord_fail_medicationNotFound() {
        // given
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder()
                .medicationType("존재하지 않는 약")
                .taken("복용함")
                .takenTime("아침")
                .build();

        when(medicationRepository.findByName("존재하지 않는 약")).thenReturn(Optional.empty());

        // when
        medicationService.saveMedicationTakenRecord(callRecord, medicationData);

        // then
        verify(medicationRepository).findByName("존재하지 않는 약");
        verify(medicationScheduleRepository, never()).findByElder(any());
        verify(medicationTakenRecordRepository, never()).save(any());
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
} 