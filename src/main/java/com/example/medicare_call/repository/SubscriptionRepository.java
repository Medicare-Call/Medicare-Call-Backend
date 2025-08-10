package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByMemberId(Integer memberId);
    Optional<Subscription> findByElderId(Integer elderId);
}
