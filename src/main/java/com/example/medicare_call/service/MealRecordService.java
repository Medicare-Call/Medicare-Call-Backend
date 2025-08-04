package com.example.medicare_call.service;

import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.dto.DailyMealResponse;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.repository.MealRecordRepository;
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
    
    public DailyMealResponse getDailyMeals(Integer elderId, LocalDate date) {
        List<MealRecord> mealRecords = mealRecordRepository.findByElderIdAndDate(elderId, date);
        
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