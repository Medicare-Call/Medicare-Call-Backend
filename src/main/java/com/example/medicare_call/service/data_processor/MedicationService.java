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

            // 해당 어르신의 복약 스케줄에서 매칭되는 것 찾기
            MedicationSchedule matchedSchedule = null;

            for (MedicationSchedule schedule : schedules) {
                if (schedule.getName().equals(medicationData.getMedicationType()) &&
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
                    .name(medicationData.getMedicationType())
                    .takenStatus(takenStatus)
                    .responseSummary(String.format("복용시간: %s, 복용여부: %s", //TODO: MedicationTakenRecord 컬럼 정리
                            medicationData.getTakenTime(), medicationData.getTaken()))
                    .recordedAt(LocalDateTime.now())
                    .build();

            medicationTakenRecordRepository.save(medicationRecord);
            MedicationService.log.info("복약 데이터 저장 완료: medication={}, taken={}, scheduleMatched={}",
                    medicationData.getMedicationType(), medicationData.getTaken(), matchedSchedule != null);
        }
    }

    /**
     * 스케줄 시간과 복용 시간을 매칭하는 메서드
     * DB에 저장된 스케줄 시간(예: "MORNING,LUNCH")과 프롬프트에서 나온 복용 시간(예: "아침")을 매칭
     */
    private boolean isScheduleTimeMatched(MedicationScheduleTime scheduleTime, String takenTime) {
        if (scheduleTime == null || takenTime == null) {
            return false;
        }

        // 프롬프트에서 나오는 시간을 enum으로 변환
        MedicationScheduleTime takenTimeEnum = MedicationScheduleTime.fromDescription(takenTime);
        if (takenTimeEnum == null) {
            return false;
        }

        // DB에 저장된 스케줄 시간에 포함되어 있는지 확인
        return scheduleTime == takenTimeEnum;
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
        Map<String, List<MedicationSchedule>> medicationSchedules = schedules.stream()
                .collect(Collectors.groupingBy(
                        MedicationSchedule::getName
                ));

        // 약 종류별 복용 기록
        Map<String, List<MedicationTakenRecord>> medicationTakenRecords = takenRecords.stream()
                .collect(Collectors.groupingBy(
                        MedicationTakenRecord::getName
                ));

        List<DailyMedicationResponse.MedicationInfo> medicationList = medicationSchedules.entrySet().stream()
                .map(entry -> {
                    String medicationName = entry.getKey();
                    List<MedicationSchedule> medicationScheduleList = entry.getValue();
                    List<MedicationTakenRecord> takenRecordList = medicationTakenRecords.getOrDefault(medicationName, List.of());
                    
                    int goalCount = medicationScheduleList.size();
                    int takenCount = (int) takenRecordList.stream()
                            .filter(record -> record.getTakenStatus() == MedicationTakenStatus.TAKEN)
                            .count();

                    List<DailyMedicationResponse.TimeInfo> timeInfos = createTimeInfos(medicationScheduleList, takenRecordList);
                    
                    return DailyMedicationResponse.MedicationInfo.builder()
                            .type(medicationName)
                            .goalCount(goalCount)
                            .takenCount(takenCount)
                            .times(timeInfos)
                            .build();
                })
                .collect(Collectors.toList());

        return DailyMedicationResponse.builder()
                .date(date)
                .medications(medicationList)
                .build();
    }

    private List<DailyMedicationResponse.TimeInfo> createTimeInfos(
            List<MedicationSchedule> schedules,
            List<MedicationTakenRecord> takenRecords) {

        Set<MedicationScheduleTime> takenTimes = takenRecords.stream()
                .filter(record ->
                        record.getTakenStatus() == MedicationTakenStatus.TAKEN && record.getCareCallRecord() != null)
                .map(record ->
                        MedicationScheduleTime.fromHour(record.getCareCallRecord().getCalledAt().getHour()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

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