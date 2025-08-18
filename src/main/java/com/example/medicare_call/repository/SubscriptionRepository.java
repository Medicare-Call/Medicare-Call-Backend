package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("SELECT s FROM Subscription s JOIN s.elder e WHERE s.member.id = :memberId AND e.status = 'ACTIVATED'")
    List<Subscription> findByMemberId(@Param("memberId") Integer memberId);
    Optional<Subscription> findByElderId(Integer elderId);
}
