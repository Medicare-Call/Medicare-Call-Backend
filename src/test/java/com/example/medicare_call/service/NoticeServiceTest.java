package com.example.medicare_call.service;

import com.example.medicare_call.domain.Notice;
import com.example.medicare_call.dto.NoticeResponse;
import com.example.medicare_call.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeService noticeService;

    @Test
    @DisplayName("공지사항 목록 조회 성공")
    void getAllNotices_success() {
        // given
        Notice notice1 = Notice.builder()
                .id(1L)
                .title("메디케어콜 서비스 오픈 안내")
                .author("관리자")
                .contents("안녕하세요. 메디케어콜 서비스를 정식 오픈합니다.")
                .publishedAt(LocalDate.of(2025, 7, 1))
                .build();

        Notice notice2 = Notice.builder()
                .id(2L)
                .title("7월 정기 점검 안내")
                .author("운영팀")
                .contents("7월 15일(월) 오전 2시부터 4시까지 정기 점검이 진행됩니다.")
                .publishedAt(LocalDate.of(2025, 7, 10))
                .build();

        List<Notice> notices = Arrays.asList(notice1, notice2);
        when(noticeRepository.findAllByOrderByPublishedAtDesc()).thenReturn(notices);

        // when
        List<NoticeResponse> result = noticeService.getAllNotices();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("메디케어콜 서비스 오픈 안내");
        assertThat(result.get(0).getAuthor()).isEqualTo("관리자");
        assertThat(result.get(0).getContents()).isEqualTo("안녕하세요. 메디케어콜 서비스를 정식 오픈합니다.");
        assertThat(result.get(0).getPublishedAt()).isEqualTo("2025-07-01");

        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("7월 정기 점검 안내");
        assertThat(result.get(1).getAuthor()).isEqualTo("운영팀");
        assertThat(result.get(1).getContents()).isEqualTo("7월 15일(월) 오전 2시부터 4시까지 정기 점검이 진행됩니다.");
        assertThat(result.get(1).getPublishedAt()).isEqualTo("2025-07-10");
    }

    @Test
    @DisplayName("공지사항 목록 조회 - 빈 목록")
    void getAllNotices_empty() {
        // given
        when(noticeRepository.findAllByOrderByPublishedAtDesc()).thenReturn(Arrays.asList());

        // when
        List<NoticeResponse> result = noticeService.getAllNotices();

        // then
        assertThat(result).isEmpty();
    }
} 