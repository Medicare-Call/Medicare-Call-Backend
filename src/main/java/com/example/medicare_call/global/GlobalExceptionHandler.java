package com.example.medicare_call.global;

import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 접근 권한 에러 처리
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.error("handleAccessDeniedException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.HANDLE_ACCESS_DENIED);
        return new ResponseEntity<>(response, ErrorCode.HANDLE_ACCESS_DENIED.getStatus());
    }

    // DTO 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult());
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    // 타입 변환 실패 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("handleMethodArgumentTypeMismatchException", e);

        String fieldName = e.getName();
        String value = e.getValue() != null ? e.getValue().toString() : "null";
        String message = String.format("Invalid value '%s' for parameter '%s'.", value, fieldName);

        ErrorResponse response = ErrorResponse.builder()
                .status(ErrorCode.INVALID_TYPE_VALUE.getStatus().value())
                .code(ErrorCode.INVALID_TYPE_VALUE.getCode())
                .message(message)
                .build();

        return new ResponseEntity<>(response, ErrorCode.INVALID_TYPE_VALUE.getStatus());
    }

    // 지원하지 않는 HTTP Method 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 404 Not Found 처리
    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("handleNoResourceFoundException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 필수 파라미터 누락 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("handleMissingServletRequestParameterException", e);
        
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        ErrorResponse response = ErrorResponse.builder()
                .status(ErrorCode.MISSING_REQUEST_PARAMETER.getStatus().value())
                .code(ErrorCode.MISSING_REQUEST_PARAMETER.getCode())
                .message(message)
                .build();
        
        return new ResponseEntity<>(response, ErrorCode.MISSING_REQUEST_PARAMETER.getStatus());
    }

    // JSON 파싱 에러 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("handleHttpMessageNotReadableException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_JSON_FORMAT);
        return new ResponseEntity<>(response, ErrorCode.INVALID_JSON_FORMAT.getStatus());
    }

    // 지원하지 않는 미디어 타입 처리
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("handleHttpMediaTypeNotSupportedException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        return new ResponseEntity<>(response, ErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus());
    }

    // 파일 크기 초과 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("handleMaxUploadSizeExceededException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.FILE_SIZE_EXCEEDED);
        return new ResponseEntity<>(response, ErrorCode.FILE_SIZE_EXCEEDED.getStatus());
    }

    // 비동기 요청 타임아웃 처리
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    protected ResponseEntity<ErrorResponse> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
        log.error("handleAsyncRequestTimeoutException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.REQUEST_TIMEOUT);
        return new ResponseEntity<>(response, ErrorCode.REQUEST_TIMEOUT.getStatus());
    }

    // 커스텀 에러
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("handleCustomException", e);
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    // 마지막 catch-all 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handleException - Unhandled exception occurred", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 