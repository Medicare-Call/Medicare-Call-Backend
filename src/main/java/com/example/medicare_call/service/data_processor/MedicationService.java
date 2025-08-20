package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.DailyHealthAnalysisResponse;
import com.example.medicare_call.dto.report.DailyMedicationResponse;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.enums.MedicationTakenStatus;
import com.example.medicare_call.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;


@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationService {
    private final MedicationTakenRecordRepository medicationTakenRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final ElderRepository elderRepository;

    @Transactional
    public void saveMedicationTakenRecord(CareCallRecord callRecord, List<HealthDataExtractionResponse.MedicationData> medicationDataList) {

        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElder(callRecord.getElder());

        for (HealthDataExtractionResponse.MedicationData medicationData : medicationDataList) {
            if (medicationData.getMedicationType() == null || medicationData.getMedicationType().trim().isEmpty()) {
                MedicationService.log.warn("약 이름이 누락되어 복약 데이터를 저장하지 않습니다. medicationData={}", medicationData);
                continue;
            }

            MedicationTakenRecord.MedicationTakenRecordBuilder recordBuilder = MedicationTakenRecord.builder()
                    .careCallRecord(callRecord)
                    .name(medicationData.getMedicationType())
                    .takenStatus(MedicationTakenStatus.fromDescription(medicationData.getTaken()))
                    .responseSummary(String.format("복용시간: %s, 복용여부: %s",
                            medicationData.getTakenTime(), medicationData.getTaken()))
                    .recordedAt(LocalDateTime.now());

            MedicationSchedule schedule = null;

            if (medicationData.getTakenTime() != null && !medicationData.getTakenTime().isBlank()) {
                MedicationScheduleTime takenTimeEnum = MedicationScheduleTime.fromDescription(medicationData.getTakenTime().trim());
                recordBuilder.takenTime(takenTimeEnum);

                // 복용 시간과 약 이름이 일치하는 스케줄 찾기
                schedule = schedules.stream()
                    .filter(s -> s.getName().equals(medicationData.getMedicationType()) &&
                        s.getScheduleTime() == takenTimeEnum)
                    .findFirst()
                    .orElse(null);
            }


            MedicationTakenRecord record = recordBuilder
                    .medicationSchedule(schedule)
                    .build();

            medicationTakenRecordRepository.save(record);
            MedicationService.log.info("복약 데이터 저장 완료: medication={}, taken={}, scheduleMatched={}",
                    medicationData.getMedicationType(), medicationData.getTaken(), schedule != null);
        }
    }

    public DailyMedicationResponse getDailyMedication(Integer elderId, LocalDate date) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        List<MedicationTakenRecord> takenRecords = medicationTakenRecordRepository.findByElderIdAndDate(elderId, date);
        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElder(elder);

        if (takenRecords.isEmpty()) {
            throw new CustomException(ErrorCode.NO_DATA_FOR_TODAY);
        }

        // 약 종류별 스케줄
        Map<String, List<MedicationSchedule>> schedulesByName = schedules.stream()
                .collect(Collectors.groupingBy(
                        MedicationSchedule::getName
                ));

        // 약 종류와 복용 시간(MedicationScheduleTime)을 키로 하는 복용 기록 맵
        Map<String, Map<MedicationScheduleTime, MedicationTakenRecord>> takenRecordsByMedAndTime = takenRecords.stream()
            .filter(r -> r.getName() != null && r.getTakenTime() != null)
            .collect(Collectors.groupingBy(
                MedicationTakenRecord::getName,
                Collectors.toMap(
                    MedicationTakenRecord::getTakenTime,
                    r -> r,
                    (existing, replacement) -> existing // 중복 시 기존 값 유지
                )
            ));

        List<DailyMedicationResponse.MedicationInfo> medicationInfos = new ArrayList<>();

        for (Map.Entry<String, List<MedicationSchedule>> entry : schedulesByName.entrySet()) {
            String medicationName = entry.getKey();
            List<MedicationSchedule> schedulesForMed = entry.getValue();
            Map<MedicationScheduleTime, MedicationTakenRecord> takenRecordsForMed = takenRecordsByMedAndTime.getOrDefault(medicationName, Collections.emptyMap());

            long takenCount = schedulesForMed.stream()
                .map(MedicationSchedule::getScheduleTime)
                .map(takenRecordsForMed::get)
                .filter(Objects::nonNull)
                .filter(record -> record.getTakenStatus() == MedicationTakenStatus.TAKEN)
                .count();

            List<DailyMedicationResponse.TimeInfo> timeInfos = schedulesForMed.stream()
                .map(schedule -> {
                    MedicationTakenRecord record = takenRecordsForMed.get(schedule.getScheduleTime());
                    Boolean taken = null;
                    if (record != null) {
                        taken = record.getTakenStatus() == MedicationTakenStatus.TAKEN;
                    }
                    return DailyMedicationResponse.TimeInfo.builder()
                        .time(schedule.getScheduleTime())
                        .taken(taken)
                        .build();
                })
                .sorted(Comparator.comparing(DailyMedicationResponse.TimeInfo::getTime))
                .collect(Collectors.toList());

            medicationInfos.add(DailyMedicationResponse.MedicationInfo.builder()
                            .type(medicationName)
                            .goalCount(schedulesForMed.size())
                            .takenCount((int) takenCount)
                            .times(timeInfos)
                            .build());
        }

        return DailyMedicationResponse.builder()
                .date(date)
                .medications(medicationInfos)
                .build();
    }
} 