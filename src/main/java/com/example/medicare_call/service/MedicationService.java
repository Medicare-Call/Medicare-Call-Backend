package com.example.medicare_call.service;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.enums.MedicationTakenStatus;
import com.example.medicare_call.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationService {
    private final MedicationTakenRecordRepository medicationTakenRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationRepository medicationRepository;

    @Transactional
    public void saveMedicationTakenRecord(CareCallRecord callRecord, HealthDataExtractionResponse.MedicationData medicationData) {
        // 약 이름으로 Medication 찾기
        Optional<Medication> medicationOpt = medicationRepository.findByName(medicationData.getMedicationType());
        if (medicationOpt.isEmpty()) {
            log.warn("약을 찾을 수 없습니다: {}", medicationData.getMedicationType());
            return;
        }

        Medication medication = medicationOpt.get();

        // 해당 어르신의 복약 스케줄에서 매칭되는 것 찾기
        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElder(callRecord.getElder());
        MedicationSchedule matchedSchedule = null;
        
        for (MedicationSchedule schedule : schedules) {
            if (schedule.getMedication().getId().equals(medication.getId()) &&
                isScheduleTimeMatched(schedule.getScheduleTime(), medicationData.getTakenTime())) {
                matchedSchedule = schedule;
                break;
            }
        }
        
        // takenStatus 결정
        MedicationTakenStatus takenStatus = MedicationTakenStatus.fromDescription(medicationData.getTaken());
        
        MedicationTakenRecord medicationRecord = MedicationTakenRecord.builder()
                .careCallRecord(callRecord)
                .medicationSchedule(matchedSchedule) // 매칭되는 스케줄이 있으면 설정, 없으면 null
                .medication(medication)
                .takenStatus(takenStatus)
                .responseSummary(String.format("복용시간: %s, 복용여부: %s", 
                    medicationData.getTakenTime(), medicationData.getTaken()))
                .recordedAt(LocalDateTime.now())
                .build();
        
        medicationTakenRecordRepository.save(medicationRecord);
        log.info("복약 데이터 저장 완료: medication={}, taken={}, scheduleMatched={}", 
            medicationData.getMedicationType(), medicationData.getTaken(), matchedSchedule != null);
    }

    /**
     * 스케줄 시간과 복용 시간을 매칭하는 메서드
     * DB에 저장된 스케줄 시간(예: "MORNING,LUNCH")과 프롬프트에서 나온 복용 시간(예: "아침")을 매칭
     */
    private boolean isScheduleTimeMatched(String scheduleTime, String takenTime) {
        if (scheduleTime == null || takenTime == null) {
            return false;
        }

        // 프롬프트에서 나오는 시간을 enum으로 변환
        MedicationScheduleTime takenTimeEnum = MedicationScheduleTime.fromDescription(takenTime);
        if (takenTimeEnum == null) {
            return false;
        }

        // DB에 저장된 스케줄 시간에 포함되어 있는지 확인
        return scheduleTime.contains(takenTimeEnum.name());
    }
} 