package com.example.medicare_call.global.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    
    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // 현재 어노테이션에 추가 파라미터가 없으므로 초기화 로직 불필요
    }
    
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null) {
            return true; // null 값은 @NotNull 어노테이션에서 처리
        }
        
        // 010으로 시작하는 11자리 숫자인지 확인
        return phoneNumber.matches("^010\\d{8}$");
    }
} 