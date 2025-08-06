package com.example.medicare_call.global.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BirthDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthDate {
    String message() default "생년월일이 유효하지 않습니다. 1900년 1월 1일 이후부터 오늘 이전의 날짜여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 