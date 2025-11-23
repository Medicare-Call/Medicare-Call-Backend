package com.example.medicare_call.service;

import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.dto.ElderRegisterResponse;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.ElderResponse;
import com.example.medicare_call.dto.ElderUpdateRequest;
import com.example.medicare_call.domain.MemberElder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.global.enums.MemberElderAuthority;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElderService {
    private final ElderRepository elderRepository;
    private final MemberRepository memberRepository;
    private final MemberElderRepository memberElderRepository;

    @Transactional
    public MemberElder registerElder(Integer memberId, @Valid ElderRegisterRequest request) {
        Member guardian = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Elder elder = Elder.builder()
            .name(request.getName())
            .birthDate(request.getBirthDate())
            .gender((byte) (request.getGender() == Gender.MALE ? 0 : 1))
            .phone(request.getPhone())
            .relationship(request.getRelationship())
            .residenceType(request.getResidenceType())
            .build();
        Elder savedElder = elderRepository.save(elder);
        MemberElder relation = MemberElder.builder()
                .guardian(guardian)
                .elder(savedElder)
                .authority(MemberElderAuthority.MANAGE)
                .build();
        memberElderRepository.save(relation);
        savedElder.addMemberElder(relation);
        guardian.addMemberElder(relation);
        return relation;
    }

    public List<ElderResponse> getElder(Integer memberId){
        Member guardian = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return memberElderRepository.findByGuardian_Id(guardian.getId()).stream()
                .map(MemberElder::getElder)
                .map(this::toElderResponse)
                .toList();

    }

    @Transactional
    public ElderResponse updateElder(Integer memberId, Integer elderId, ElderUpdateRequest req) {
        Elder updateElder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));
        MemberElder relation = memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));
        if(relation.getAuthority() != MemberElderAuthority.MANAGE) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        updateElder.applySettings(
                req.name(),
                req.birthDate(),
                req.gender().getCode(),
                req.phone(),
                req.relationship(),
                req.residenceType()
        );

        return toElderResponse(updateElder);
    }

    @Transactional
    public void deleteElder(Integer memberId, Integer elderId) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));
        MemberElder relation = memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));
        if(relation.getAuthority() != MemberElderAuthority.MANAGE) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        elderRepository.delete(elder);
    }

    @Transactional
    public List<ElderRegisterResponse> bulkRegisterElders(Integer memberId, List<ElderRegisterRequest> requests) {
        Member guardian = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Elder> elders = requests.stream()
                .map(request -> Elder.builder()
                        .name(request.getName())
                        .birthDate(request.getBirthDate())
                        .gender((byte) (request.getGender() == Gender.MALE ? 0 : 1))
                        .phone(request.getPhone())
                        .relationship(request.getRelationship())
                        .residenceType(request.getResidenceType())
                        .build())
                .toList();

        List<Elder> savedElders = elderRepository.saveAll(elders);
        savedElders.forEach(elder -> {
            MemberElder relation = MemberElder.builder()
                    .guardian(guardian)
                    .elder(elder)
                    .authority(MemberElderAuthority.MANAGE)
                    .build();
            memberElderRepository.save(relation);
            elder.addMemberElder(relation);
            guardian.addMemberElder(relation);
        });

        return savedElders.stream()
                .map(elder -> toRegisterResponse(elder, guardian))
                .toList();
    }

    private ElderResponse toElderResponse(Elder elder) {
        return new ElderResponse(
                elder.getId(),
                elder.getName(),
                elder.getBirthDate(),
                Gender.fromCode(elder.getGender()),
                elder.getPhone(),
                elder.getRelationship(),
                elder.getResidenceType()
        );
    }

    private ElderRegisterResponse toRegisterResponse(Elder elder, Member guardian) {
        return ElderRegisterResponse.builder()
                .id(elder.getId())
                .name(elder.getName())
                .birthDate(elder.getBirthDate())
                .phone(elder.getPhone())
                .gender(elder.getGender() == 0 ? "MALE" : "FEMALE")
                .relationship(elder.getRelationship() != null ? elder.getRelationship().name() : null)
                .residenceType(elder.getResidenceType() != null ? elder.getResidenceType().name() : null)
                .guardianId(guardian != null ? guardian.getId() : null)
                .guardianName(guardian != null ? guardian.getName() : null)
                .build();
    }
}
