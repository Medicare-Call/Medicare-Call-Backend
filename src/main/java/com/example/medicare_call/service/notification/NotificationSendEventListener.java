package com.example.medicare_call.service.notification;

import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.global.event.CareCallCompleteEvent;
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
     * 이메일 전송 로직큰 흐름
     * 1. 이벤트로부터 Notification 을 저장한다.
     * 2. pk 포함해서, 알람을 보낼 데이터를 정의해서 파이어베이스로 전송
     * */
    @EventListener
    public void listenCareCallCompleteEvent(CareCallCompleteEvent careCallCompleteEvent) {
        NotificationDto notificationDto = parseToNotificationDto(careCallCompleteEvent);
        Notification notification = notificationService.saveNotification(notificationDto);
        // 파이어베이스에
//        firebaseService.sendNotification();

    }

    @EventListener
    public void listenCareCallMissEvent(CareCallCompleteEvent careCallCompleteEvent) {

    }

    private NotificationDto parseToNotificationDto(CareCallCompleteEvent careCallCompleteEvent) {
        return null;
    }

}
