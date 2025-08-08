package com.example.medicare_call.global.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, LocalDate> {
    
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        // 현재 어노테이션에 추가 파라미터가 없으므로 초기화 로직 불필요
    }
    
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // null 값은 @NotNull 어노테이션에서 처리
        }
        
        LocalDate today = LocalDate.now();
        
        // 날짜가 오늘 이전이어야 함 (미래 날짜는 허용하지 않음)
        return !date.isAfter(today);
    }
} 