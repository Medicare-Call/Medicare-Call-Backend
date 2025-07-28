package com.example.medicare_call.controller;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.ElderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.domain.Member;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ElderController.class)
@AutoConfigureMockMvc(addFilters = false) //security필터 비활성화
@ActiveProfiles("test")
class ElderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ElderService elderService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
            .name("테스트보호자")
            .phone("01000000000")
            .gender((byte)1)
            .termsAgreedAt(LocalDateTime.now())
            .plan((byte)1)
            .build();
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
    }

    @Test
    @DisplayName("어르신 등록 성공")
    void registerElder_success() throws Exception {
        ElderRegisterRequest req = new ElderRegisterRequest();
        req.setName("홍길동");
        req.setBirthDate(java.time.LocalDate.of(1940, 5, 1));
        req.setGender(Gender.MALE);
        req.setPhone("01012345678");
        req.setRelationship(ElderRelation.GRANDCHILD);
        req.setResidenceType(ResidenceType.ALONE);
        req.setGuardianId(1);

        when(elderService.registerElder(any())).thenReturn(
            Elder.builder()
                .id(1)
                .name("홍길동")
                .birthDate(java.time.LocalDate.of(1940, 5, 1))
                .gender((byte)0)
                .phone("01012345678")
                .relationship(ElderRelation.GRANDCHILD)
                .residenceType(ResidenceType.ALONE)
                .guardian(Member.builder()
                    .id(1)
                    .name("테스트보호자")
                    .phone("01000000000")
                    .gender((byte)1)
                    .termsAgreedAt(LocalDateTime.now())
                    .plan((byte)1)
                    .build())
                .build()
        );

        mockMvc.perform(post("/elders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("어르신 등록 실패 - 필수값 누락")
    void registerElder_fail_validation() throws Exception {
        ElderRegisterRequest req = new ElderRegisterRequest();
        req.setName(""); // name 누락
        req.setBirthDate(java.time.LocalDate.of(1940, 5, 1));
        req.setGender(Gender.MALE);
        req.setPhone("01012345678");
        req.setRelationship(ElderRelation.GRANDCHILD);
        req.setResidenceType(ResidenceType.ALONE);
        req.setGuardianId(1);

        mockMvc.perform(post("/elders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}