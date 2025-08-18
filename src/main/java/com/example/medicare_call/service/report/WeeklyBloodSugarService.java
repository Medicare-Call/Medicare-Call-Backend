package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.dto.report.WeeklyBloodSugarResponse;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private static final int PAGE_SIZE = 12;


    public WeeklyBloodSugarResponse getWeeklyBloodSugar(Integer elderId, Integer counter, String typeStr) {
        elderRepository.findById(elderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        BloodSugarMeasurementType type = BloodSugarMeasurementType.valueOf(typeStr.toUpperCase());
        Pageable pageable = PageRequest.of(counter, PAGE_SIZE);

        Page<BloodSugarRecord> recordsPage = bloodSugarRecordRepository.findByElderIdAndMeasurementTypeOrderByRecordedAtDesc(elderId, type, pageable);
        List<BloodSugarRecord> records = recordsPage.getContent();


        if (records.isEmpty()) {
            return WeeklyBloodSugarResponse.empty();
        }

        List<WeeklyBloodSugarResponse.BloodSugarData> data = records.stream()
                .map(this::convertToBloodSugarData)
                .collect(Collectors.toList());


        return WeeklyBloodSugarResponse.builder()
                .data(data)
                .hasNextPage(recordsPage.hasNext())
                .build();
    }

    private WeeklyBloodSugarResponse.BloodSugarData convertToBloodSugarData(BloodSugarRecord record) {
        return WeeklyBloodSugarResponse.BloodSugarData.builder()
                .date(record.getRecordedAt().toLocalDate())
                .value(record.getBlood_sugar_value().intValue())
                .status(record.getStatus())
                .build();
    }
} 