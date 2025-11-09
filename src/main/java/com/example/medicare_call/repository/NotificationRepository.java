package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    int countByMemberIdAndIsReadFalse(Integer memberId);
}
