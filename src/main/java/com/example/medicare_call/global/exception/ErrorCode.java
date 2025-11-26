package com.example.medicare_call.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력하신 정보가 올바르지 않습니다. 다시 확인해 주세요."),
    ENTITY_ALREADY_EXISTS(HttpStatus.CONFLICT, "C002", "이미 등록된 정보입니다. 다른 정보로 시도해 주세요."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청하신 페이지나 기능을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "입력 형식이 올바르지 않습니다. 올바른 형식으로 입력해 주세요."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "해당 작업에 대한 권한이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C007", "지원하지 않는 요청 방식입니다. 올바른 방식으로 다시 시도해 주세요."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "C008", "필수 파라미터가 누락되었습니다. 요청을 확인해 주세요."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "C009", "JSON 형식이 올바르지 않습니다. 요청 데이터를 확인해 주세요."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "C010", "지원하지 않는 미디어 타입입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "C011", "파일 크기가 허용된 범위를 초과했습니다."),
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "C012", "요청 시간이 초과되었습니다. 다시 시도해 주세요."),

    // Auth
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A003", "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "인증 정보가 올바르지 않습니다. 다시 로그인해 주세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A005", "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."),
    SMS_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "A006", "인증번호가 올바르지 않거나 만료되었습니다. 다시 확인해 주세요."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),

    // Elder
    ELDER_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "등록되지 않은 어르신입니다."),
    ELDER_DELETED(HttpStatus.UNAUTHORIZED, "E002", "활성화되지 않은 어르신입니다."),

    // CareCall
    CARE_CALL_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "CC001", "케어콜 설정 정보를 찾을 수 없습니다."),
    CARE_CALL_WRONG_TIME(HttpStatus.BAD_REQUEST, "CC002","케어콜이 설정되지 않은 시간에 실행 되었습니다"),

    // Medication
    MEDICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MD001", "해당 복용약 정보를 찾을 수 없습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문 내역을 찾을 수 없습니다."),
    ORDER_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "O002", "이미 처리 완료된 주문입니다."),

    // NaverPay
    NAVER_PAY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "N001", "결제 처리 중 오류가 발생했습니다. 다시 시도해 주세요."),

    // OpenAI
    OPENAI_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI001", "OpenAI API 호출 중 오류가 발생했습니다. 다시 시도해 주세요."),
    STT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI002", "음성 텍스트 변환(STT) 처리에 실패했습니다."),

    // Data
    NO_DATA_FOR_TODAY(HttpStatus.NOT_FOUND, "D001", "오늘의 데이터가 없습니다."),
    NO_DATA_FOR_WEEK(HttpStatus.NOT_FOUND, "D002", "이번주의 데이터가 없습니다."),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "구독 정보를 찾을 수 없습니다."),

    // Firebase
    FIREBASE_INITIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FB001", "Firebase 초기화 중 오류가 발생했습니다. 다시 시도해 주세요."),
    FIREBASE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FB002", "FCM 메세지 전송에 실패했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
