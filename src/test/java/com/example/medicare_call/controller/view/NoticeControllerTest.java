package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.NoticeResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.NoticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeController.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeService noticeService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("공지사항 목록 조회 성공")
    void getNotices_success() throws Exception {
        // given
        List<NoticeResponse> notices = Arrays.asList(
                NoticeResponse.builder()
                        .id(1L)
                        .title("메디케어콜 서비스 오픈 안내")
                        .author("관리자")
                        .contents("안녕하세요. 메디케어콜 서비스를 정식 오픈합니다.")
                        .publishedAt("2025-07-01")
                        .build(),
                NoticeResponse.builder()
                        .id(2L)
                        .title("7월 정기 점검 안내")
                        .author("운영팀")
                        .contents("7월 15일(월) 오전 2시부터 4시까지 정기 점검이 진행됩니다.")
                        .publishedAt("2025-07-10")
                        .build()
        );

        when(noticeService.getAllNotices()).thenReturn(notices);

        // when & then
        mockMvc.perform(get("/notices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("메디케어콜 서비스 오픈 안내"))
                .andExpect(jsonPath("$[0].author").value("관리자"))
                .andExpect(jsonPath("$[0].contents").value("안녕하세요. 메디케어콜 서비스를 정식 오픈합니다."))
                .andExpect(jsonPath("$[0].publishedAt").value("2025-07-01"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("7월 정기 점검 안내"))
                .andExpect(jsonPath("$[1].author").value("운영팀"))
                .andExpect(jsonPath("$[1].contents").value("7월 15일(월) 오전 2시부터 4시까지 정기 점검이 진행됩니다."))
                .andExpect(jsonPath("$[1].publishedAt").value("2025-07-10"));
    }

    @Test
    @DisplayName("공지사항 목록 조회 - 빈 목록")
    void getNotices_empty() throws Exception {
        // given
        when(noticeService.getAllNotices()).thenReturn(Arrays.asList());

        // when & then
        mockMvc.perform(get("/notices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
} 