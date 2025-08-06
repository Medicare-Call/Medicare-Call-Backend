package com.example.medicare_call.global.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "날짜가 유효하지 않습니다. 오늘 이전의 날짜여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 