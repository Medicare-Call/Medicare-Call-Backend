package com.example.medicare_call.service.notification;

import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(Notification notification) {
        try {
            Message newMessage = createMessageFromDto(notification);
            send(newMessage);
        } catch (FirebaseMessagingException exception) {
            throw new CustomException(ErrorCode.FIREBASE_SEND_FAILED);
        }
    }

    public void validationToken(String token) {
        try {
            Message message = Message.builder().setToken(token).build();
            send(message);
        } catch (FirebaseMessagingException e) {
            throw new CustomException(ErrorCode.FIREBASE_SEND_FAILED);
        }
    }

    private Message createMessageFromDto(Notification notification) {
        return Message.builder()
                .setToken(notification.getMember().getFcmToken())
                .setNotification(com.google.firebase.messaging.Notification
                        .builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getBody())
                        .build())
                .putAllData(Map.of("notificationId",""+notification.getId()))
                .setTopic("carecall_alarm")
                .build();
    }

    private void send(Message message) throws FirebaseMessagingException {
        firebaseMessaging.send(message);
    }

}
