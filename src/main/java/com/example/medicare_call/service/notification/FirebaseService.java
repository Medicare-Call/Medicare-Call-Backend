package com.example.medicare_call.service.notification;

import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
class FirebaseService {

    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    public void sendNotification(NotificationDto notificationDto) {
        try {
            Message newMessage = createMessageFromDto(notificationDto);
            send(newMessage);
        } catch (FirebaseMessagingException exception) {
            throw new CustomException(ErrorCode.FIREBASE_SEND_FAILED);
        }
    }
    private Message createMessageFromDto(NotificationDto notificationDto) {
        return Message.builder()
                .setNotification(Notification
                        .builder()
                        .setTitle(notificationDto.title())
                        .setBody(notificationDto.body())
                        .build())
                .putAllData(objectMapper.convertValue(notificationDto, Map.class))
                .setTopic("필요하다면 추가")
                .build();
    }

    private void send(Message message) throws FirebaseMessagingException {
        firebaseMessaging.send(message);
    }

}
