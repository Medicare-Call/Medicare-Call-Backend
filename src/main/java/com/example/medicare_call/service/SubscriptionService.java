package com.example.medicare_call.service;

import com.example.medicare_call.dto.SubscriptionResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    public List<SubscriptionResponse> getSubscriptionsByMember(Long memberId) {
        if (!memberRepository.existsById(memberId.intValue())) {
            throw new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId);
        }

        List<SubscriptionResponse> subscriptions = subscriptionRepository.findByMemberId(memberId.intValue()).stream()
                .map(SubscriptionResponse::from)
                .collect(Collectors.toList());

        if (subscriptions.isEmpty()) {
            throw new ResourceNotFoundException("해당 회원의 구독 정보를 찾을 수 없습니다.");
        }

        return subscriptions;
    }
}
