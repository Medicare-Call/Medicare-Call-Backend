package com.example.medicare_call.service;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.ElderSettingResponse;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElderSettingService {
    private final MemberRepository memberRepository;

    public List<ElderSettingResponse> getElderSetting(Integer memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        log.info("memberId from @AuthUser = {}", memberId);
        return member.getElders().stream()
                .map(elder -> new ElderSettingResponse(
                        elder.getId(),
                        elder.getName(),
                        elder.getBirthDate(),
                        Gender.fromCode(elder.getGender()),
                        elder.getPhone(),
                        elder.getRelationship(),
                        elder.getResidenceType()
                ))
                .toList();

    }
}
