package com.example.medicare_call.global.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, LocalDate> {
    
    private static final LocalDate MIN_BIRTH_DATE = LocalDate.of(1900, 1, 1);
    
    @Override
    public void initialize(ValidBirthDate constraintAnnotation) {
        // 현재 어노테이션에 추가 파라미터가 없으므로 초기화 로직 불필요
    }
    
    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true; // null 값은 @NotNull 어노테이션에서 처리
        }
        
        LocalDate today = LocalDate.now();
        
        // 생년월일이 1900년 1월 1일 이후이고 오늘 이전이어야 함
        return !birthDate.isBefore(MIN_BIRTH_DATE) && !birthDate.isAfter(today);
    }
} 