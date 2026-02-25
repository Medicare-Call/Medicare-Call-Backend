package com.example.medicare_call.service;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.MemberElder;
import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.dto.ElderRegisterResponse;
import com.example.medicare_call.dto.ElderResponse;
import com.example.medicare_call.dto.ElderUpdateRequest;
import com.example.medicare_call.global.enums.MemberElderAuthority;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
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
        Member guardian = getMemberOrThrow(memberId);

        Elder savedElder = elderRepository.save(buildElder(request));
        return linkGuardianToElder(guardian, savedElder);
    }

    public Elder getElder(Integer elderId) {
        return getElderOrThrow(elderId);
    }

    public List<ElderResponse> getElders(Integer memberId) {
        Member guardian = getMemberOrThrow(memberId);

        return memberElderRepository.findEldersByGuardianId(guardian.getId()).stream()
                .map(this::toElderResponse)
                .toList();
    }

    @Transactional
    public ElderResponse updateElder(Integer memberId, Integer elderId, ElderUpdateRequest req) {
        Elder elder = getElderOrThrow(elderId);
        getManageRelationOrThrow(memberId, elderId);

        elder.applySettings(
                req.name(),
                req.birthDate(),
                req.gender(),
                req.phone(),
                req.relationship(),
                req.residenceType()
        );

        return toElderResponse(elder);
    }

    @Transactional
    public void deleteElder(Integer memberId, Integer elderId) {
        Elder elder = getElderOrThrow(elderId);
        getManageRelationOrThrow(memberId, elderId);

        elderRepository.delete(elder);
    }

    @Transactional
    public List<ElderRegisterResponse> bulkRegisterElders(Integer memberId, List<ElderRegisterRequest> requests) {
        Member guardian = getMemberOrThrow(memberId);

        List<Elder> elders = requests.stream()
                .map(this::buildElder)
                .toList();

        List<Elder> savedElders = elderRepository.saveAll(elders);

        savedElders.forEach(elder -> linkGuardianToElder(guardian, elder));

        return savedElders.stream()
                .map(elder -> toRegisterResponse(elder, guardian))
                .toList();
    }


    private Member getMemberOrThrow(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Elder getElderOrThrow(Integer elderId) {
        return elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));
    }

    private void getManageRelationOrThrow(Integer memberId, Integer elderId) {
        MemberElder relation = memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

        if (relation.getAuthority() != MemberElderAuthority.MANAGE) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    private Elder buildElder(ElderRegisterRequest request) {
        return Elder.builder()
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .phone(request.getPhone())
                .relationship(request.getRelationship())
                .residenceType(request.getResidenceType())
                .build();
    }

    private MemberElder linkGuardianToElder(Member guardian, Elder elder) {
        MemberElder relation = MemberElder.builder()
                .guardian(guardian)
                .elder(elder)
                .authority(MemberElderAuthority.MANAGE)
                .build();

        memberElderRepository.save(relation);
        elder.addMemberElder(relation);
        guardian.addMemberElder(relation);

        return relation;
    }


    private ElderResponse toElderResponse(Elder elder) {
        return new ElderResponse(
                elder.getId(),
                elder.getName(),
                elder.getBirthDate(),
                elder.getGender(),
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
                .gender(elder.getGender())
                .relationship(elder.getRelationship() != null ? elder.getRelationship().name() : null)
                .residenceType(elder.getResidenceType() != null ? elder.getResidenceType().name() : null)
                .guardianId(guardian != null ? guardian.getId() : null)
                .guardianName(guardian != null ? guardian.getName() : null)
                .build();
    }
}