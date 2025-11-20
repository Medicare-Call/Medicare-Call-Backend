package com.example.medicare_call.dto.notification;

import com.example.medicare_call.domain.Notification;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class NotificationPageResponse {

    private int totalPages;
    private int currentPageNumber;
    private long currentElements;

    private List<NotificationResponse> notifications;


    public static NotificationPageResponse from(Page<Notification> notificationPage) {
        List<NotificationResponse> contents = notificationPage.getContent().stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());

        return NotificationPageResponse.builder()
                .notifications(contents)
                .currentPageNumber(notificationPage.getNumber())
                .totalPages(notificationPage.getTotalPages())
                .currentElements(notificationPage.getTotalElements())
                .build();
    }

    @Getter
    @Builder
    public static class NotificationResponse {
        private Long id;
        private String title;
        private String body;
        private Boolean isRead;
        private LocalDateTime createdAt;

        static NotificationResponse from(Notification notification) {
            return NotificationResponse.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .body(notification.getBody())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }
}
