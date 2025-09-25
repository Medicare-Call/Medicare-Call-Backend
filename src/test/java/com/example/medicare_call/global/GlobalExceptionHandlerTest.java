package com.example.medicare_call.global;

import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(globalExceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
    }
/*
*  TODO:
*  Spring Boot 3.x 에서는 기존에 DispatchServlet이 Resouce matching이 되지 않는 경우에서 NoResourceFoundException를 던짐
*  서버에서는 GlobalExceptionHandler이 잘 처리하고 있지만, 테스트 환경인 standaloneSetup MockMvc 에서는 DispatcherServlet의 전체 컨텍스트가 올라가지 않는다.
*  이 때문인지? 테스트 환경에서는 여전히 NoHandlerFoundException이 던져지고 있어 GlobalExceptionHandler이 잡아내지 못하는 것 같다.
*  우선 테스트에서 제외하고, 다시 한 번 확인해보자.
* **/
//    @Test
//    @DisplayName("NoResourceFoundException 처리 테스트")
//    void handleNoResourceFoundException() throws Exception {
//        // given & when & then
//        mockMvc.perform(get("/non-existent-endpoint"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
//                .andExpect(jsonPath("$.message").value("요청하신 페이지나 기능을 찾을 수 없습니다."));
//    }

    @Test
    @DisplayName("MissingServletRequestParameterException 처리 테스트")
    void handleMissingServletRequestParameterException() throws Exception {
        // given & when & then
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MISSING_REQUEST_PARAMETER"))
                .andExpect(jsonPath("$.message").value("필수 파라미터 'requiredParam'가 누락되었습니다."));
    }

    @Test
    @DisplayName("HttpMessageNotReadableException 처리 테스트")
    void handleHttpMessageNotReadableException() throws Exception {
        // given & when & then
        mockMvc.perform(post("/test/json-parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_JSON_FORMAT"))
                .andExpect(jsonPath("$.message").value("JSON 형식이 올바르지 않습니다. 요청 데이터를 확인해 주세요."));
    }

    @Test
    @DisplayName("HttpRequestMethodNotSupportedException 처리 테스트")
    void handleHttpRequestMethodNotSupportedException() throws Exception {
        // given & when & then
        mockMvc.perform(delete("/test/get-only"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 요청 방식입니다. 올바른 방식으로 다시 시도해 주세요."));
    }

    @Test
    @DisplayName("HttpMediaTypeNotSupportedException 처리 테스트")
    void handleHttpMediaTypeNotSupportedException() throws Exception {
        // given & when & then
        mockMvc.perform(post("/test/json-only")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 미디어 타입입니다."));
    }

    @Test
    @DisplayName("MethodArgumentTypeMismatchException 처리 테스트")
    void handleMethodArgumentTypeMismatchException() throws Exception {
        // given & when & then
        mockMvc.perform(get("/test/type-mismatch?id=invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_TYPE_VALUE"))
                .andExpect(jsonPath("$.message").value("Invalid value 'invalid' for parameter 'id'."));
    }

    @Test
    @DisplayName("CustomException 처리 테스트")
    void handleCustomException() throws Exception {
        // given & when & then
        mockMvc.perform(get("/test/custom-exception"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MEMBER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
    }

    @Test
    @DisplayName("일반 Exception 처리 테스트")
    void handleException() throws Exception {
        // given & when & then
        mockMvc.perform(get("/test/general-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }

    // 테스트용 컨트롤러
    @org.springframework.web.bind.annotation.RestController
    @org.springframework.web.bind.annotation.RequestMapping("/test")
    static class TestController {

        @org.springframework.web.bind.annotation.GetMapping("/missing-param")
        public String missingParam(@org.springframework.web.bind.annotation.RequestParam String requiredParam) {
            return "success";
        }

        @org.springframework.web.bind.annotation.PostMapping("/json-parse")
        public String jsonParse(@org.springframework.web.bind.annotation.RequestBody Object body) {
            return "success";
        }

        @org.springframework.web.bind.annotation.GetMapping("/get-only")
        public String getOnly() {
            return "success";
        }

        @org.springframework.web.bind.annotation.PostMapping(value = "/json-only", consumes = MediaType.APPLICATION_JSON_VALUE)
        public String jsonOnly(@org.springframework.web.bind.annotation.RequestBody Object body) {
            return "success";
        }

        @org.springframework.web.bind.annotation.GetMapping("/type-mismatch")
        public String typeMismatch(@org.springframework.web.bind.annotation.RequestParam Long id) {
            return "success";
        }

        @org.springframework.web.bind.annotation.GetMapping("/custom-exception")
        public String customException() {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        @org.springframework.web.bind.annotation.GetMapping("/general-exception")
        public String generalException() {
            throw new RuntimeException("Unexpected error");
        }
    }
}
