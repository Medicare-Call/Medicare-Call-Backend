package com.example.medicare_call.service.notification;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.dto.NotificationDto;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private final Integer elderId = 42;

    private NotificationDto mockNotificationDto(Integer elderId, String title, String body) {
        NotificationDto dto = mock(NotificationDto.class);
        when(dto.elderId()).thenReturn(elderId);
        when(dto.title()).thenReturn(title);
        when(dto.body()).thenReturn(body);
        return dto;
    }

    @Test
    @DisplayName("성공: Elder/Member 조회 후 알림 저장")
    void save_success() {
        // given
        NotificationDto dto = mockNotificationDto(elderId, "제목", "내용");

        Elder elder = mock(Elder.class);
        when(elder.getId()).thenReturn(elderId);

        Member member = mock(Member.class);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(memberRepository.findById(elderId)).thenReturn(Optional.of(member));

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification arg = invocation.getArgument(0);
            return Notification.builder()
                    .id(1L)
                    .member(arg.getMember())
                    .title(arg.getTitle())
                    .body(arg.getBody())
                    .createdAt(arg.getCreatedAt())
                    .isRead(arg.getIsRead())
                    .build();
        });

        // when
        Notification saved = notificationService.saveNotification(dto);

        // then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getMember()).isSameAs(member);
        assertThat(saved.getTitle()).isEqualTo("제목");
        assertThat(saved.getBody()).isEqualTo("내용");
        assertThat(saved.getIsRead()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();

        verify(elderRepository).findById(elderId);
        verify(memberRepository).findById(elderId);
    }

    @Test
    @DisplayName("실패: Elder 미존재 시 CustomException 발생")
    void save_fail_elderNotFound() {
        // given
        NotificationDto dto = mock(NotificationDto.class);
        when(dto.elderId()).thenReturn(elderId);
        when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> notificationService.saveNotification(dto));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("실패: Member 미존재 시 CustomException 발생")
    void save_fail_memberNotFound() {
        // given
        NotificationDto dto = mock(NotificationDto.class);
        when(dto.elderId()).thenReturn(elderId);

        Elder elder = mock(Elder.class);
        when(elder.getId()).thenReturn(elderId);
        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(memberRepository.findById(elderId)).thenReturn(Optional.empty());

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> notificationService.saveNotification(dto));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }


}