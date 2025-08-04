package com.example.medicare_call.service;

import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ElderService {
    private final ElderRepository elderRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Elder registerElder(@Valid ElderRegisterRequest request) {
        Member guardian = memberRepository.findById(request.getGuardianId())
            .orElseThrow(() -> new ResourceNotFoundException("보호자를 찾을 수 없습니다: " + request.getGuardianId()));
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
} 