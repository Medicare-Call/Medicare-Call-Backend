package com.example.medicare_call.service;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.dto.WeeklyBloodSugarResponse;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyBloodSugarService {

    private final BloodSugarRecordRepository bloodSugarRecordRepository;
    private final ElderRepository elderRepository;

    public WeeklyBloodSugarResponse getWeeklyBloodSugar(Integer elderId, LocalDate startDate, String typeStr) {
        elderRepository.findById(elderId)
            .orElseThrow(() -> new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + elderId));
        
        LocalDate endDate = startDate.plusDays(6); // 7일간 조회

        BloodSugarMeasurementType measurementType = BloodSugarMeasurementType.valueOf(typeStr);
        List<BloodSugarRecord> records = bloodSugarRecordRepository
                .findByElderIdAndMeasurementTypeAndDateBetween(elderId, measurementType, startDate, endDate);

        List<WeeklyBloodSugarResponse.BloodSugarData> data = records.stream()
                .map(this::convertToBloodSugarData)
                .collect(Collectors.toList());

        // 평균 계산
        WeeklyBloodSugarResponse.BloodSugarSummary average = calculateAverage(records);

        // 최신 데이터 선택
        WeeklyBloodSugarResponse.BloodSugarSummary latest = records.isEmpty() ? null : 
                convertToBloodSugarSummary(records.get(records.size() - 1));

        return WeeklyBloodSugarResponse.builder()
                .period(WeeklyBloodSugarResponse.Period.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build())
                .data(data)
                .average(average)
                .latest(latest)
                .build();
    }

    private WeeklyBloodSugarResponse.BloodSugarData convertToBloodSugarData(BloodSugarRecord record) {
        return WeeklyBloodSugarResponse.BloodSugarData.builder()
                .date(record.getRecordedAt().toLocalDate())
                .value(record.getBlood_sugar_value().intValue())
                .status(record.getStatus())
                .build();
    }

    private WeeklyBloodSugarResponse.BloodSugarSummary convertToBloodSugarSummary(BloodSugarRecord record) {
        return WeeklyBloodSugarResponse.BloodSugarSummary.builder()
                .value(record.getBlood_sugar_value().intValue())
                .status(record.getStatus())
                .build();
    }

    private WeeklyBloodSugarResponse.BloodSugarSummary calculateAverage(List<BloodSugarRecord> records) {
        if (records.isEmpty()) {
            return null;
        }

        BigDecimal sum = records.stream()
                .map(BloodSugarRecord::getBlood_sugar_value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = sum.divide(BigDecimal.valueOf(records.size()), 0, RoundingMode.HALF_UP);
        
        // 평균값의 상태 판정 (기준: 70 미만=저혈당, 70-200=정상, 200 초과=고혈당)
        // TODO: 일일 혈당 데이터에 대한 상태 판정은 OpenAI API에서 수행하는데, 여기서도 호출할지, 아니면 둘다 서비스에서 호출할지? 통일된 방식이 좋아보인다.
        BloodSugarStatus averageStatus = determineStatus(average.intValue());

        return WeeklyBloodSugarResponse.BloodSugarSummary.builder()
                .value(average.intValue())
                .status(averageStatus)
                .build();
    }

    private BloodSugarStatus determineStatus(int value) {
        if (value < 70) {
            return BloodSugarStatus.LOW;
        } else if (value > 200) {
            return BloodSugarStatus.HIGH;
        } else {
            return BloodSugarStatus.NORMAL;
        }
    }
} 