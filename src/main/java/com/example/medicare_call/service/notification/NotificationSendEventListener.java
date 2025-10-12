package com.example.medicare_call.service.notification;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.global.event.CareCallEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
class NotificationSendEventListener {

    // Todo 이벤트를 발행하는 곳에서 어떤 데이터를 전달할지 정의해야함 elderId, title, body 등 더 구체적으로 파악해야함

    // Todo 두 이벤트를 하나로 합칠수있는지, 어차피 핵심은 서버 입장에서 알람만 보내는거고, 내용만 다른거니까

    private final NotificationService notificationService;
    private final FirebaseService firebaseService;

    /**
     * 알람 전송 로직 흐름
     * careCallRecord != null 케어콜 수신 / null 케어콜 미수신
     * 1. CareCallRecord 로부터 알림 데이터 추철
     * 2. Notification 엔티티를 저장
     * 3. 파이어베이스에 관련된 데이터 전송 ( Notification 엔티티의 PK ) 전송해야함을 유의
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
        // null이 들어올 경우, 케어콜을 못받았다는 알람 전송 (하드코딩)
        return new NotificationDto(1, "","");
    }

}
