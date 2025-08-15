package com.example.medicare_call.service;

import com.example.medicare_call.dto.SubscriptionResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
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
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return subscriptionRepository.findByMemberId(memberId.intValue()).stream()
                .map(SubscriptionResponse::from)
                .collect(Collectors.toList());
    }
}
