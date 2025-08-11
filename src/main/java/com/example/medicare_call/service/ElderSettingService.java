package com.example.medicare_call.service;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.ElderUpdateRequest;
import com.example.medicare_call.dto.ElderSettingResponse;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElderSettingService {
    private final MemberRepository memberRepository;
    private final ElderRepository elderRepository;

    public List<ElderSettingResponse> getElder(Integer memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

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

    @Transactional
    public ElderSettingResponse updateElder(Integer memberId, Integer elderId, ElderUpdateRequest req) {
        Elder updateElder = elderRepository.findById(elderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 어르신입니다."));
        if(!updateElder.getGuardian().getId().equals(memberId)) throw new AccessDeniedException("해당 어르신에 대한 권한이 없습니다.");

        updateElder.applySettings(
                req.name(),
                req.birthDate(),
                req.gender().getCode(),
                req.phone(),
                req.relationship(),
                req.residenceType()
        );

        return new ElderSettingResponse(
                updateElder.getId(),
                updateElder.getName(),
                updateElder.getBirthDate(),
                Gender.fromCode(updateElder.getGender()),
                updateElder.getPhone(),
                updateElder.getRelationship(),
                updateElder.getResidenceType()
        );
    }

    @Transactional
    public void deleteElder(Integer memberId, Integer elderId) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 어르신입니다."));
        if(!elder.getGuardian().getId().equals(memberId)) throw new AccessDeniedException("해당 어르신에 대한 권한이 없습니다.");

        elderRepository.delete(elder);
    }
}
