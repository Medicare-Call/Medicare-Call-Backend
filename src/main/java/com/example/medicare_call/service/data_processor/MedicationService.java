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

        if (takenRecords.isEmpty()) {
            return DailyMedicationResponse.empty(date);
        }

        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElder(elder);

        // 약 종류별 스케줄
        Map<String, List<MedicationSchedule>> schedulesByName = schedules.stream()
                .collect(Collectors.groupingBy(
                        MedicationSchedule::getName
                ));

        // 약 종류별 복용 기록
        Map<String, List<MedicationTakenRecord>> takenRecordsByName = takenRecords.stream()
                .collect(Collectors.groupingBy(MedicationTakenRecord::getName));

        List<DailyMedicationResponse.MedicationInfo> medicationInfos = new ArrayList<>();

        for (Map.Entry<String, List<MedicationSchedule>> entry : schedulesByName.entrySet()) {
            String medicationName = entry.getKey();
            List<MedicationSchedule> schedulesForMed = entry.getValue();
            List<MedicationTakenRecord> takenRecordsForMed = takenRecordsByName.getOrDefault(medicationName, Collections.emptyList());

            // 복용 완료한 시간대 집합
            Set<MedicationScheduleTime> takenTimes = takenRecordsForMed.stream()
                    .map(MedicationTakenRecord::getTakenTime)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<DailyMedicationResponse.TimeInfo> timeInfos = createTimeInfos(schedulesForMed, takenTimes);

            medicationInfos.add(DailyMedicationResponse.MedicationInfo.builder()
                            .type(medicationName)
                            .goalCount(schedulesForMed.size())
                            .takenCount(takenTimes.size())
                            .times(timeInfos)
                            .build());
        }

        return DailyMedicationResponse.builder()
                .date(date)
                .medications(medicationInfos)
                .build();
    }

    private List<DailyMedicationResponse.TimeInfo> createTimeInfos(
            List<MedicationSchedule> schedules,
            Set<MedicationScheduleTime> takenTimes) {

        return schedules.stream()
                .map(MedicationSchedule::getScheduleTime)
                .distinct()
                .map(scheduleTime -> DailyMedicationResponse.TimeInfo.builder()
                        .time(scheduleTime)
                        .taken(takenTimes.contains(scheduleTime))
                        .build())
                .sorted(Comparator.comparing(DailyMedicationResponse.TimeInfo::getTime))
                .collect(Collectors.toList());
    }
} 