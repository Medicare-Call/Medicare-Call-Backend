package com.example.medicare_call.service.notification;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.dto.NotificationDto;
import com.example.medicare_call.dto.notification.NotificationPageResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ElderRepository elderRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification saveNotification(NotificationDto notificationDto) {
        Elder elder = elderRepository.findById(notificationDto.elderId()).orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));
        Member member = memberRepository.findById(elder.getGuardian().getId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification = Notification.builder()
                .member(member)
                .title(notificationDto.title())
                .body(notificationDto.body())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void updateIsRead(Long notificationId, boolean isRead) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        notification.updateIsRead(isRead);
    }

    public NotificationPageResponse getNotifications(Integer memberId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return NotificationPageResponse.from(
                notificationRepository.findByMember_IdOrderByCreatedAtDesc(memberId, pageable)
        );
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Integer memberId) {
        return notificationRepository.countByMemberIdAndIsReadFalse(memberId);
    }
}
