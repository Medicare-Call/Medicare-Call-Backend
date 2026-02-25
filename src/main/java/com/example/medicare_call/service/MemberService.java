package com.example.medicare_call.service;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.MemberInfoResponse;
import com.example.medicare_call.dto.MemberInfoUpdateRequest;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberInfoResponse getMemberInfo(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberInfoResponse.from(member);
    }

    @Transactional
    public MemberInfoResponse updateMemberInfo(Integer memberId, MemberInfoUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        request.updateMember(member);
        Member updatedMember = memberRepository.save(member);
        return MemberInfoResponse.from(updatedMember);
    }

    public String getFcmToken(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return member.getFcmToken();
    }

    @Transactional
    public void updateFcmToken(Integer memberId, String fcmToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateFcmToken(fcmToken);
        memberRepository.save(member);
    }

    @Transactional
    public void withdraw(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // Member를 삭제하면 cascade 설정에 의해 Member_Elder 및 Subscription도 함께 삭제됩니다.
        memberRepository.delete(member);
    }

}
