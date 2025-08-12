package com.example.medicare_call.service;

import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.ElderInfoResponse;
import com.example.medicare_call.dto.ElderUpdateRequest;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElderService {
    private final ElderRepository elderRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Elder registerElder(Integer memberId, @Valid ElderRegisterRequest request) {
        Member guardian = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("보호자를 찾을 수 없습니다: " + memberId));
        Elder elder = Elder.builder()
            .name(request.getName())
            .birthDate(request.getBirthDate())
            .gender((byte) (request.getGender() == Gender.MALE ? 0 : 1))
            .phone(request.getPhone())
            .relationship(request.getRelationship())
            .residenceType(request.getResidenceType())
            .guardian(guardian)
            .build();
        return elderRepository.save(elder);
    }

    public List<ElderInfoResponse> getElder(Integer memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        return member.getElders().stream()
                .map(elder -> new ElderInfoResponse(
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
    public ElderInfoResponse updateElder(Integer memberId, Integer elderId, ElderUpdateRequest req) {
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

        return new ElderInfoResponse(
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