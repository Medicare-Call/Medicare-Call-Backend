package com.example.medicare_call.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", " Invalid Input Value"),
    ENTITY_NOT_FOUND(HttpStatus.BAD_REQUEST, "C003", " Entity Not Found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "Server Error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", " Invalid Type Value"),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "해당 작업에 대한 권한이 없습니다."),

    // Auth
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "A001", "Email is Duplication"),
    LOGIN_INPUT_INVALID(HttpStatus.BAD_REQUEST, "A002", "Login input is invalid"),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A003", "Access token has expired"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "Invalid token"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A005", "Refresh token has expired"),


    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원정보를 찾을 수 없습니다."),

    // Elder
    ELDER_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "어르신을 찾을 수 없습니다."),

    // CareCall
    CARE_CALL_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "CC001", "CareCall setting not found"),

    // Medication
    MEDICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MD001", "Medication not found"),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "Order not found"),
    ORDER_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "O002", "Order already processed"),

    // NaverPay
    NAVER_PAY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "N001", "NaverPay API error");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
