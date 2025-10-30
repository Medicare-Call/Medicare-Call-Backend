package com.example.medicare_call.service.notification;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.dto.NotificationDto;
import com.example.medicare_call.global.event.CareCallEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
class NotificationSendEventListener {

    private final NotificationService notificationService;
    private final FirebaseService firebaseService;

    /**
     * 알람 전송 로직 흐름
     * 1. CareCallRecord 로부터 알림 데이터 추출
     * 2. Notification 엔티티를 저장 ( Notification 엔티티 스키마는 변경 가능 )
     * 3. 파이어베이스에 관련된 데이터 전송 ( Notification 엔티티의 PK )을 함께 전송해야함
     * */
    @EventListener
    public void listenCareCallEvent(CareCallEvent careCallEvent) {
        CareCallRecord careCallRecord = careCallEvent.careCallRecord();
        NotificationDto notificationDto = parseToNotificationDto(careCallRecord);
        Notification notification = notificationService.saveNotification(notificationDto);
        firebaseService.sendNotification(notification);
    }


    private NotificationDto parseToNotificationDto(CareCallRecord careCallRecord) {
        //Todo careCallRecord 로부터 알람 메시지 구성
        // 1차 2차 3차 구분 ( 3차 일 경우 건강 이상징후 확인)
        // careCallRecord의 callStatus 필드로, 케어콜 수신 & 미수신 파악
        return new NotificationDto(1, "","");
    }

}
