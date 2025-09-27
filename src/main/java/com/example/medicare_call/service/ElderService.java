package com.example.medicare_call.service;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.*;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.service.report.ElderHealthInfoService;
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
    private final ElderHealthInfoService elderHealthInfoService;

    @Transactional
    public Elder registerElder(Integer memberId, @Valid ElderRegisterRequest request) {
        Member guardian = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
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

    // All-or-Nothing: 하나라도 처리 실패하면 예외 발생, 전체 롤백
    @Transactional(rollbackFor = Exception.class)
    public ElderBulkResponse bulkRegisterElders(Integer memberId, List<ElderBulkRequest> requests) {
        Member guardian = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Elder> elders = requests.stream()
                .map(request -> Elder.builder()
                        .name(request.getElder().getName())
                        .birthDate(request.getElder().getBirthDate())
                        .gender((byte) (request.getElder().getGender() == Gender.MALE ? 0 : 1))
                        .phone(request.getElder().getPhone())
                        .relationship(request.getElder().getRelationship())
                        .residenceType(request.getElder().getResidenceType())
                        .guardian(guardian)
                        .build())
                .toList();

        List<Elder> savedElders = elderRepository.saveAll(elders);

        for (int i = 0; i < requests.size(); i++) {
            elderHealthInfoService.upsertElderHealthInfo(
                    savedElders.get(i).getId(),
                    requests.get(i).getHealthInfo()
            );
        }

        List<ElderBulkResponse.ElderResult> successResults = savedElders.stream()
                .map(elder -> ElderBulkResponse.ElderResult.builder()
                        .elderId(elder.getId())
                        .elderName(elder.getName())
                        .build())
                .toList();

        return ElderBulkResponse.builder()
                .totalCount(successResults.size())
                .successResults(successResults)
                .build();
    }

    public List<ElderResponse> getElder(Integer memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return member.getElders().stream()
                .map(elder -> new ElderResponse(
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
    public ElderResponse updateElder(Integer memberId, Integer elderId, ElderUpdateRequest req) {
        Elder updateElder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));
        if(!updateElder.getGuardian().getId().equals(memberId)) throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);

        updateElder.applySettings(
                req.name(),
                req.birthDate(),
                req.gender().getCode(),
                req.phone(),
                req.relationship(),
                req.residenceType()
        );

        return new ElderResponse(
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
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));
        if(!elder.getGuardian().getId().equals(memberId)) throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);

        elderRepository.delete(elder);
    }
} 