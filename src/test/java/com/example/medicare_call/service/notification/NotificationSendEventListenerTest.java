package com.example.medicare_call.service.notification;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.dto.NotificationDto;
import com.example.medicare_call.global.event.CareCallEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationSendEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private FirebaseService firebaseService;

    @InjectMocks
    private NotificationSendEventListener notificationSendEventListener;

    @Test
    @DisplayName("실패 케어콜: 부재중 상태로 알림 발송")
    void listenCareCallEvent_callStatus_failed() {
        // given
        Elder elder = mock(Elder.class);
        when(elder.getName()).thenReturn("김철수");
        when(elder.getId()).thenReturn(1);

        CareCallSetting setting = mock(CareCallSetting.class);

        CareCallRecord careCallRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .callStatus("failed")
                .startTime(LocalDateTime.of(2025, 11, 5, 10, 0))
                .build();

        CareCallEvent event = new CareCallEvent(careCallRecord);

        Notification notification = mock(Notification.class);
        when(notificationService.saveNotification(any(NotificationDto.class)))
                .thenReturn(notification);

        // when
        notificationSendEventListener.listenCareCallEvent(event);

        // then
        ArgumentCaptor<NotificationDto> dtoCaptor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(notificationService).saveNotification(dtoCaptor.capture());

        NotificationDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.elderId()).isEqualTo(1);
        assertThat(capturedDto.title()).isEqualTo("메디케어콜");
        assertThat(capturedDto.body()).contains("김철수");
        assertThat(capturedDto.body()).contains("부재중");

        verify(firebaseService).sendNotification(notification);
    }

    @Test
    @DisplayName("미응답 케어콜: no-answer 상태로 알림 발송")
    void listenCareCallEvent_callStatus_noAnswer() {
        // given
        Elder elder = mock(Elder.class);
        when(elder.getName()).thenReturn("이영희");
        when(elder.getId()).thenReturn(2);

        CareCallSetting setting = mock(CareCallSetting.class);

        CareCallRecord careCallRecord = CareCallRecord.builder()
                .id(2)
                .elder(elder)
                .setting(setting)
                .callStatus("no-answer")
                .startTime(LocalDateTime.of(2025, 11, 5, 14, 30))
                .build();

        CareCallEvent event = new CareCallEvent(careCallRecord);

        Notification notification = mock(Notification.class);
        when(notificationService.saveNotification(any(NotificationDto.class)))
                .thenReturn(notification);

        // when
        notificationSendEventListener.listenCareCallEvent(event);

        // then
        ArgumentCaptor<NotificationDto> dtoCaptor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(notificationService).saveNotification(dtoCaptor.capture());

        NotificationDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.elderId()).isEqualTo(2);
        assertThat(capturedDto.body()).contains("이영희");
        assertThat(capturedDto.body()).contains("부재중");

        verify(firebaseService).sendNotification(notification);
    }

    @Test
    @DisplayName("완료 케어콜 1차: completed 상태로 알림 발송")
    void listenCareCallEvent_callStatus_completed_firstCall() {
        // given
        Elder elder = mock(Elder.class);
        when(elder.getName()).thenReturn("박민수");
        when(elder.getId()).thenReturn(3);

        CareCallSetting setting = mock(CareCallSetting.class);
        when(setting.getFirstCallTime()).thenReturn(LocalTime.of(9, 0));
        when(setting.getSecondCallTime()).thenReturn(LocalTime.of(14, 0));
        when(setting.getThirdCallTime()).thenReturn(LocalTime.of(20, 0));

        CareCallRecord careCallRecord = CareCallRecord.builder()
                .id(3)
                .elder(elder)
                .setting(setting)
                .callStatus("completed")
                .startTime(LocalDateTime.of(2025, 11, 5, 10, 0))
                .build();

        CareCallEvent event = new CareCallEvent(careCallRecord);

        Notification notification = mock(Notification.class);
        when(notificationService.saveNotification(any(NotificationDto.class)))
                .thenReturn(notification);

        // when
        notificationSendEventListener.listenCareCallEvent(event);

        // then
        ArgumentCaptor<NotificationDto> dtoCaptor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(notificationService).saveNotification(dtoCaptor.capture());

        NotificationDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.elderId()).isEqualTo(3);
        assertThat(capturedDto.body()).contains("1차");
        assertThat(capturedDto.body()).contains("완료");

        verify(firebaseService).sendNotification(notification);
    }

    @Test
    @DisplayName("완료 케어콜 2차: completed 상태로 알림 발송")
    void listenCareCallEvent_callStatus_completed_secondCall() {
        // given
        Elder elder = mock(Elder.class);
        when(elder.getName()).thenReturn("최지원");
        when(elder.getId()).thenReturn(4);

        CareCallSetting setting = mock(CareCallSetting.class);
        when(setting.getFirstCallTime()).thenReturn(LocalTime.of(9, 0));
        when(setting.getSecondCallTime()).thenReturn(LocalTime.of(14, 0));
        when(setting.getThirdCallTime()).thenReturn(LocalTime.of(20, 0));

        CareCallRecord careCallRecord = CareCallRecord.builder()
                .id(4)
                .elder(elder)
                .setting(setting)
                .callStatus("completed")
                .startTime(LocalDateTime.of(2025, 11, 5, 15, 0))
                .build();

        CareCallEvent event = new CareCallEvent(careCallRecord);

        Notification notification = mock(Notification.class);
        when(notificationService.saveNotification(any(NotificationDto.class)))
                .thenReturn(notification);

        // when
        notificationSendEventListener.listenCareCallEvent(event);

        // then
        ArgumentCaptor<NotificationDto> dtoCaptor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(notificationService).saveNotification(dtoCaptor.capture());

        NotificationDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.elderId()).isEqualTo(4);
        assertThat(capturedDto.body()).contains("2차");
        assertThat(capturedDto.body()).contains("완료");

        verify(firebaseService).sendNotification(notification);
    }

    @Test
    @DisplayName("완료 케어콜 3차 + 건강 징후: completed 상태로 알림 발송")
    void listenCareCallEvent_callStatus_completed_thirdCall_withHealthDetails() {
        // given
        Elder elder = mock(Elder.class);
        when(elder.getName()).thenReturn("정수진");
        when(elder.getId()).thenReturn(5);

        CareCallSetting setting = mock(CareCallSetting.class);
        when(setting.getFirstCallTime()).thenReturn(LocalTime.of(9, 0));
        when(setting.getSecondCallTime()).thenReturn(LocalTime.of(14, 0));
        when(setting.getThirdCallTime()).thenReturn(LocalTime.of(20, 0));

        CareCallRecord careCallRecord = CareCallRecord.builder()
                .id(5)
                .elder(elder)
                .setting(setting)
                .callStatus("completed")
                .startTime(LocalDateTime.of(2025, 11, 5, 21, 0))
                .healthDetails("혈압이 높게 측정되었습니다.")
                .build();

        CareCallEvent event = new CareCallEvent(careCallRecord);

        Notification notification = mock(Notification.class);
        when(notificationService.saveNotification(any(NotificationDto.class)))
                .thenReturn(notification);

        // when
        notificationSendEventListener.listenCareCallEvent(event);

        // then
        ArgumentCaptor<NotificationDto> dtoCaptor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(notificationService).saveNotification(dtoCaptor.capture());

        NotificationDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.elderId()).isEqualTo(5);
        assertThat(capturedDto.body()).contains("3차");
        assertThat(capturedDto.body()).contains("완료");
        assertThat(capturedDto.body()).contains("건강 징후");

        verify(firebaseService).sendNotification(notification);
    }

    @Test
    @DisplayName("완료 케어콜 3차 + 건강 징후 미존재: 건강 징후 메시지 미포함")
    void listenCareCallEvent_callStatus_completed_thirdCall_withoutHealthDetails() {
        // given
        Elder elder = mock(Elder.class);
        when(elder.getName()).thenReturn("한민지");
        when(elder.getId()).thenReturn(6);

        CareCallSetting setting = mock(CareCallSetting.class);
        when(setting.getFirstCallTime()).thenReturn(LocalTime.of(9, 0));
        when(setting.getSecondCallTime()).thenReturn(LocalTime.of(14, 0));
        when(setting.getThirdCallTime()).thenReturn(LocalTime.of(20, 0));

        CareCallRecord careCallRecord = CareCallRecord.builder()
                .id(6)
                .elder(elder)
                .setting(setting)
                .callStatus("completed")
                .startTime(LocalDateTime.of(2025, 11, 5, 21, 0))
                .healthDetails(null)
                .build();

        CareCallEvent event = new CareCallEvent(careCallRecord);

        Notification notification = mock(Notification.class);
        when(notificationService.saveNotification(any(NotificationDto.class)))
                .thenReturn(notification);

        // when
        notificationSendEventListener.listenCareCallEvent(event);

        // then
        ArgumentCaptor<NotificationDto> dtoCaptor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(notificationService).saveNotification(dtoCaptor.capture());

        NotificationDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.elderId()).isEqualTo(6);
        assertThat(capturedDto.body()).contains("3차");
        assertThat(capturedDto.body()).contains("완료");
        assertThat(capturedDto.body()).doesNotContain("건강 징후");

        verify(firebaseService).sendNotification(notification);
    }

    @Test
    @DisplayName("케어콜 이벤트 처리: NotificationService와 FirebaseService 호출 순서 검증")
    void listenCareCallEvent_serviceCallOrder() {
        // given
        Elder elder = mock(Elder.class);
        when(elder.getName()).thenReturn("테스트");
        when(elder.getId()).thenReturn(7);

        CareCallSetting setting = mock(CareCallSetting.class);

        CareCallRecord careCallRecord = CareCallRecord.builder()
                .id(7)
                .elder(elder)
                .setting(setting)
                .callStatus("failed")
                .startTime(LocalDateTime.of(2025, 11, 5, 10, 0))
                .build();

        CareCallEvent event = new CareCallEvent(careCallRecord);

        Notification notification = mock(Notification.class);
        when(notificationService.saveNotification(any(NotificationDto.class)))
                .thenReturn(notification);

        // when
        notificationSendEventListener.listenCareCallEvent(event);

        // then - 호출 순서 검증
        InOrder inOrder = inOrder(notificationService, firebaseService);
        inOrder.verify(notificationService).saveNotification(any(NotificationDto.class));
        inOrder.verify(firebaseService).sendNotification(notification);
    }

    @Test
    @DisplayName("2차까지만 설정된 경우: 호출 시간별 구분 정확도")
    void listenCareCallEvent_onlyTwoCallTimes() {
        // given
        Elder elder = mock(Elder.class);
        when(elder.getName()).thenReturn("두 번호");
        when(elder.getId()).thenReturn(8);

        CareCallSetting setting = mock(CareCallSetting.class);
        when(setting.getFirstCallTime()).thenReturn(LocalTime.of(9, 0));
        when(setting.getSecondCallTime()).thenReturn(LocalTime.of(18, 0));
        when(setting.getThirdCallTime()).thenReturn(null);

        CareCallRecord careCallRecord = CareCallRecord.builder()
                .id(8)
                .elder(elder)
                .setting(setting)
                .callStatus("completed")
                .startTime(LocalDateTime.of(2025, 11, 5, 20, 0))
                .build();

        CareCallEvent event = new CareCallEvent(careCallRecord);

        Notification notification = mock(Notification.class);
        when(notificationService.saveNotification(any(NotificationDto.class)))
                .thenReturn(notification);

        // when
        notificationSendEventListener.listenCareCallEvent(event);

        // then
        ArgumentCaptor<NotificationDto> dtoCaptor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(notificationService).saveNotification(dtoCaptor.capture());

        NotificationDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.body()).contains("2차");

        verify(firebaseService).sendNotification(notification);
    }

}