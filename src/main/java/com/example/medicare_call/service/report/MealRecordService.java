package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.dto.report.DailyMealResponse;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.service.data_processor.ai.AiSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
            return DailyMealResponse.empty(date);
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
}