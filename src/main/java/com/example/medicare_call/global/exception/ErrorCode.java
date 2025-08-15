package com.example.medicare_call.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버에서 알 수 없는 오류가 발생하였습니다. 고객센터에 문의해 주세요."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "요청 데이터 형식이 올바르지 않습니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "해당 작업에 대한 권한이 없습니다."),

    // Auth
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A003", "Access token 이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "JWT 검증 중 오류가 발생했습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A005", "Refresh token 이 만료되었습니다."),


    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원정보를 찾을 수 없습니다."),

    // Elder
    ELDER_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "어르신을 찾을 수 없습니다."),

    // CareCall
    CARE_CALL_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "CC001", "케어콜 설정을 찾을 수 없습니다."),

    // Medication
    MEDICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MD001", "약 정보를 찾을 수 없습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문 정보를 찾을 수 없습니다,"),
    ORDER_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "O002", "이미 처리된 주문입니다."),

    // NaverPay
    NAVER_PAY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "N001", "네이버페이 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
