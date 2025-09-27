package com.example.medicare_call.controller;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.*;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.ElderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Elder", description = "어르신(노인) 등록/관리 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/elders")
public class ElderController {
    private final ElderService elderService;

    @Operation(summary = "어르신 등록", description = "이름, 생년월일, 성별, 휴대폰, 관계, 거주방식 정보를 입력받아 어르신을 등록합니다.")
    @PostMapping
    public ResponseEntity<ElderRegisterResponse> registerElder(
            @Parameter(hidden = true) @AuthUser Long memberId,
            @Valid @RequestBody ElderRegisterRequest request) {
        Elder elder = elderService.registerElder(memberId.intValue(), request);
        ElderRegisterResponse response = ElderRegisterResponse.builder()
            .id(elder.getId())
            .name(elder.getName())
            .birthDate(elder.getBirthDate())
            .phone(elder.getPhone())
            .gender(elder.getGender() == 0 ? "MALE" : "FEMALE")
            .relationship(elder.getRelationship() != null ? elder.getRelationship().name() : null)
            .residenceType(elder.getResidenceType() != null ? elder.getResidenceType().name() : null)
            .guardianId(elder.getGuardian() != null ? elder.getGuardian().getId() : null)
            .guardianName(elder.getGuardian() != null ? elder.getGuardian().getName() : null)
            .build();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "어르신 개인정보 조회",
            description = "로그인한 유저가 등록한 모든 어르신의 개인정보를 조회합니다."
    )
    @GetMapping
    public ResponseEntity<List<ElderResponse>> getElder(@Parameter(hidden = true) @AuthUser Long memberId){
        log.info("어르신 설정 정보 조회 요청: memberId={}", memberId);
        List<ElderResponse> body = elderService.getElder(memberId.intValue());
        return ResponseEntity.ok(body);
    }

    @Operation(
            summary = "어르신 개인정보 수정",
            description = "어르신 정보 전체를 요청받아 값을 수정합니다."
    )
    @PostMapping("/{elderId}")
    public ResponseEntity<ElderResponse> updateElder(
            @Parameter(hidden = true) @AuthUser Long memberId,
            @PathVariable Integer elderId,
            @Valid @RequestBody ElderUpdateRequest req
    ){
        log.info("어르신 설정 정보 수정 요청: memberId={}, elderId={}, name={}, birthDate={}, gender={}, phone={}, relationship={}, residenceType={}",
                memberId, elderId, req.name(), req.birthDate(), req.gender(), req.phone(), req.relationship(), req.residenceType());

        ElderResponse res = elderService.updateElder(memberId.intValue(), elderId, req);
        return ResponseEntity.ok(res);
    }

    @Operation(
            summary = "어르신 개인정보 삭제",
            description = "선택한 어르신의 개인정보를 삭제합니다."
    )
    @DeleteMapping("/{elderId}")
    public ResponseEntity<Void> deleteElder(
            @Parameter(hidden = true) @AuthUser Long memberId,
            @PathVariable Integer elderId
    ){
        log.info("어르신 설정 정보 삭제 요청: memberId={}, elderId={}", memberId, elderId);
        elderService.deleteElder(memberId.intValue(), elderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "어르신 정보 일괄 등록",
            description = "여러 어르신의 기본 정보와 건강 정보를 일괄로 등록합니다."
    )
    @PostMapping("/bulk")
    public ResponseEntity<ElderBulkResponse> bulkRegisterElders(
            @Parameter(hidden = true) @AuthUser Long memberId,
            @Valid @NotEmpty @RequestBody List<@Valid ElderBulkRequest> requests
    ){
        log.info("어르신 정보 일괄 등록 요청: memberId={}, 요청 건수={}", memberId, requests.size());
        ElderBulkResponse response = elderService.bulkRegisterElders(memberId.intValue(), requests);
        return ResponseEntity.ok(response);
    }
} 