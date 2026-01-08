package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.dto.report.DailyMealResponse;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.service.ai.AiSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.MealEatenStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealRecordService {

    private final MealRecordRepository mealRecordRepository;
    private final ElderRepository elderRepository;
    private final AiSummaryService aiSummaryService;

    public DailyMealResponse getDailyMeals(Integer elderId, LocalDate date) {
        elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        List<MealRecord> mealRecords = mealRecordRepository.findByElderIdAndDate(elderId, date);

        if (mealRecords.isEmpty()) {
            throw new CustomException(ErrorCode.NO_DATA_FOR_TODAY);
        }

        String breakfast = null;
        String lunch = null;
        String dinner = null;

        for (MealRecord record : mealRecords) {
            MealType mealType = MealType.fromValue(record.getMealType());
            if (mealType != null) {
                String mealContent = record.getResponseSummary();

                switch (mealType) {
                    case BREAKFAST:
                        breakfast = mealContent;
                        break;
                    case LUNCH:
                        lunch = mealContent;
                        break;
                    case DINNER:
                        dinner = mealContent;
                        break;
                }
            }
        }

        DailyMealResponse.Meals meals = DailyMealResponse.Meals.builder()
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .build();

        return DailyMealResponse.builder()
                .date(date)
                .meals(meals)
                .build();
    }

    public void saveMealData(CareCallRecord callRecord, List<HealthDataExtractionResponse.MealData> mealDataList) {
        if (mealDataList == null || mealDataList.isEmpty()) {
            return;
        }

        for (HealthDataExtractionResponse.MealData mealData : mealDataList) {
            // 식사 타입 결정
            MealType mealType = MealType.fromDescription(mealData.getMealType());
            if (mealType == null) {
                log.warn("알 수 없는 식사 타입: {}", mealData.getMealType());
                continue;
            }

            // 식사 여부 결정
            MealEatenStatus mealEatenStatus = MealEatenStatus.fromDescription(mealData.getMealEatenStatus());
            Byte eatenStatusValue = null;
            String responseSummary = mealData.getMealSummary();

            if (mealEatenStatus == null) {
                // eatenStatus는 null로 저장, responseSummary는 고정 메시지
                responseSummary = "해당 시간대 식사 여부를 명확히 확인하지 못했어요.";
            } else {
                eatenStatusValue = mealEatenStatus.getValue();
            }

            // 식사 데이터 저장
            MealRecord mealRecord = MealRecord.builder()
                    .careCallRecord(callRecord)
                    .mealType(mealType.getValue())
                    .eatenStatus(eatenStatusValue)
                    .responseSummary(responseSummary)
                    .recordedAt(LocalDateTime.now())
                    .build();

            mealRecordRepository.save(mealRecord);
            log.info("식사 데이터 저장 완료: mealType={}, mealEatenStatus={}, summary={}",
                    mealRecord.getMealType(),
                    mealEatenStatus != null ? mealEatenStatus.getDescription() : "알 수 없음",
                    responseSummary);
        }
    }
}