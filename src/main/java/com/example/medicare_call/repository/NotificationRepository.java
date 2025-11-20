package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 추후에 Left 조인 최적화: 방안 Query DSL로 커스터마이징
    Page<Notification> findByMember_IdOrderByCreatedAtDesc(Integer memberId, Pageable pageable);

    int countByMemberIdAndIsReadFalse(Integer memberId);
}
