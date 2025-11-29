package com.example.medicare_call.service.notification;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.dto.NotificationDto;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.event.CareCallEvent;
import com.example.medicare_call.util.CareCallUtil;
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
        String callStatus = careCallRecord.getCallStatus();
        String body;
        if(CareCallStatus.FAILED.matches(callStatus) || CareCallStatus.NO_ANSWER.matches(callStatus)){
            body = String.format("📞 %s 어르신 케어콜 부재중 상태입니다. 확인해 주세요!", careCallRecord.getElder().getName());
        }else{ // completed
            int order = CareCallUtil.extractCareCallOrder(careCallRecord.getStartTime(), careCallRecord.getSetting());
            body = String.format("✅ %d차 케어콜이 완료되었어요. 확인해보세요!", order);
            if(order == 3 && careCallRecord.getHealthDetails()!=null)
                body += "\n 추가적으로, 건강 징후가 탐지되었어요.";
        }
        return new NotificationDto(careCallRecord.getElder().getId(), "메디케어콜",body);
    }
    

}
