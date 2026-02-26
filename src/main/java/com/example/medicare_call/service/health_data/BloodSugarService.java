package com.example.medicare_call.service.health_data;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloodSugarService {
    private final BloodSugarRecordRepository bloodSugarRecordRepository;

    @Transactional
    public void saveBloodSugarData(CareCallRecord callRecord, List<HealthDataExtractionResponse.BloodSugarData> bloodSugarDataList) {

        for (HealthDataExtractionResponse.BloodSugarData bloodSugarData : bloodSugarDataList) {

            if (bloodSugarData.getBloodSugarValue() == null) {
                log.warn("혈당 값이 없어서 저장하지 않습니다.");
                continue;
            }

            // measurementType 결정 (식전/식후)
            BloodSugarMeasurementType measurementType = bloodSugarData.getMeasurementType();

            // status 결정 (LOW/NORMAL/HIGH)
            BloodSugarStatus status = null;
            if (bloodSugarData.getStatus() != null) {
                status = BloodSugarStatus.valueOf(bloodSugarData.getStatus());
            }

            BloodSugarRecord bloodSugarRecord = BloodSugarRecord.builder()
                    .careCallRecord(callRecord)
                    .blood_sugar_value(BigDecimal.valueOf(bloodSugarData.getBloodSugarValue()))
                    .measurementType(measurementType)
                    .status(status)
                    .recordedAt(LocalDateTime.now()) // TODO: 혈당 측정한 시간을 전화를 통해 질의할 것인지 확정한 뒤에 재검토
                    .responseSummary(String.format("측정시각: %s, 식전/식후: %s",
                            bloodSugarData.getMeasurementTime(), bloodSugarData.getMeasurementType().name()))
                    .build();

            bloodSugarRecordRepository.save(bloodSugarRecord);
            log.info("혈당 데이터 저장 완료: value={}, mealTime={}, status={}",
                    bloodSugarData.getBloodSugarValue(), bloodSugarData.getMeasurementType(), status);
        }
    }
} 