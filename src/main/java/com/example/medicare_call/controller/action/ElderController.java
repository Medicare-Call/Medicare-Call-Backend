package com.example.medicare_call.controller.action;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.service.ElderService;
import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.dto.ElderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Elder", description = "어르신(노인) 등록/관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/elders")
public class ElderController {
    private final ElderService elderService;

    @Operation(summary = "어르신 등록", description = "이름, 생년월일, 성별, 휴대폰, 관계, 거주방식 정보를 입력받아 어르신을 등록합니다.")
    @PostMapping
    public ResponseEntity<ElderResponse> registerElder(@Valid @RequestBody ElderRegisterRequest request) {
        Elder elder = elderService.registerElder(request);
        ElderResponse response = ElderResponse.builder()
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
} 