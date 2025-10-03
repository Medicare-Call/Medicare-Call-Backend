package com.example.medicare_call.global;

import com.example.medicare_call.global.exception.ErrorCode;
import lombok.*;
import org.springframework.validation.BindingResult;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @Builder.Default
    private boolean success = false;
    private String code;
    private String message;

    public static ErrorResponse of(final ErrorCode code, final BindingResult bindingResult) {
        String errorMessage = code.getMessage() + ": " + FieldError.of(bindingResult).toString();
        return new ErrorResponse(false, code.name(), errorMessage);
    }

    public static ErrorResponse of(final ErrorCode code) {
        return new ErrorResponse(false, code.name(), code.getMessage());
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        private FieldError(final String field, final String value, final String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        public static List<Map<String, String>> of(final BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> {
                        Map<String, String> errorDetail = new HashMap<>();
                        errorDetail.put("field", error.getField());
                        errorDetail.put("value", error.getRejectedValue() == null ? "" : error.getRejectedValue().toString());
                        errorDetail.put("reason", error.getDefaultMessage());
                        return errorDetail;
                    })
                    .collect(Collectors.toList());
        }
    }
} 