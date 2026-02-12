package com.example.medicare_call.controller;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MemberElder;
import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.dto.ElderRegisterResponse;
import com.example.medicare_call.dto.BulkElderRegisterRequest;
import com.example.medicare_call.global.enums.*;
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
import com.example.medicare_call.global.annotation.AuthUser;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import com.example.medicare_call.global.GlobalExceptionHandler;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ElderController.class)
@AutoConfigureMockMvc(addFilters = false) //security필터 비활성화
@ActiveProfiles("test")
class ElderControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ElderService elderService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private JwtProvider jwtProvider;

    private Member testMember;
    private ElderRegisterRequest testElderRequest1;
    private ElderRegisterRequest testElderRequest2;

    private static class TestAuthUserArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthUser.class);
        }

        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                      org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                      org.springframework.web.context.request.NativeWebRequest webRequest,
                                      org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
            return 1; // 테스트용 고정 memberId
        }
    }

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ElderController(elderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestAuthUserArgumentResolver())
                .build();

        testMember = Member.builder()
                .id(1)
                .name("테스트보호자")
                .phone("01000000000")
                .gender(Gender.FEMALE)
                .termsAgreedAt(LocalDateTime.now())
                .plan(SubscriptionPlan.PREMIUM)
                .build();

        testElderRequest1 = new ElderRegisterRequest();
        testElderRequest1.setName("홍길동");
        testElderRequest1.setBirthDate(java.time.LocalDate.of(1940, 5, 1));
        testElderRequest1.setGender(Gender.MALE);
        testElderRequest1.setPhone("01012345678");
        testElderRequest1.setRelationship(ElderRelation.GRANDCHILD);
        testElderRequest1.setResidenceType(ResidenceType.ALONE);

        testElderRequest2 = new ElderRegisterRequest();
        testElderRequest2.setName("김영희");
        testElderRequest2.setBirthDate(java.time.LocalDate.of(1945, 3, 15));
        testElderRequest2.setGender(Gender.FEMALE);
        testElderRequest2.setPhone("01098765432");
        testElderRequest2.setRelationship(ElderRelation.CHILD);
        testElderRequest2.setResidenceType(ResidenceType.WITH_FAMILY);

        when(memberRepository.findById(1)).thenReturn(Optional.of(testMember));
    }

    private byte convertGenderToByte(Gender gender) {
        return (byte) (gender == Gender.MALE ? 0 : 1);
    }

    @Test
    @DisplayName("어르신 등록 성공")
    void registerElder_success() throws Exception {
        Elder elder = Elder.builder()
                .id(1)
                .name(testElderRequest1.getName())
                .birthDate(testElderRequest1.getBirthDate())
                .gender(testElderRequest1.getGender())
                .phone(testElderRequest1.getPhone())
                .relationship(testElderRequest1.getRelationship())
                .residenceType(testElderRequest1.getResidenceType())
                .build();
        MemberElder relation = MemberElder.builder()
                .guardian(testMember)
                .elder(elder)
                .authority(MemberElderAuthority.MANAGE)
                .build();
        elder.addMemberElder(relation);
        when(elderService.registerElder(any(Integer.class), any(ElderRegisterRequest.class))).thenReturn(relation);

        mockMvc.perform(post("/elders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testElderRequest1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").value(testMember.getId()))
                .andExpect(jsonPath("$.guardianName").value(testMember.getName()));
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

        mockMvc.perform(post("/elders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("어르신 정보 일괄 등록 성공")
    void bulkRegisterElders_success() throws Exception {
        // given
        BulkElderRegisterRequest bulkRequest = BulkElderRegisterRequest.builder()
                .elders(List.of(testElderRequest1, testElderRequest2))
                .build();

        List<ElderRegisterResponse> expectedResponses = List.of(
                ElderRegisterResponse.builder()
                        .id(1)
                        .name(testElderRequest1.getName())
                        .birthDate(testElderRequest1.getBirthDate())
                        .phone(testElderRequest1.getPhone())
                        .gender("MALE")
                        .relationship(testElderRequest1.getRelationship().name())
                        .residenceType(testElderRequest1.getResidenceType().name())
                        .guardianId(testMember.getId())
                        .guardianName(testMember.getName())
                        .build(),
                ElderRegisterResponse.builder()
                        .id(2)
                        .name(testElderRequest2.getName())
                        .birthDate(testElderRequest2.getBirthDate())
                        .phone(testElderRequest2.getPhone())
                        .gender("FEMALE")
                        .relationship(testElderRequest2.getRelationship().name())
                        .residenceType(testElderRequest2.getResidenceType().name())
                        .guardianId(testMember.getId())
                        .guardianName(testMember.getName())
                        .build()
        );

        when(elderService.bulkRegisterElders(any(Integer.class), any(List.class)))
                .thenReturn(expectedResponses);

        // when & then
        mockMvc.perform(post("/elders/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("홍길동"))
                .andExpect(jsonPath("$[1].name").value("김영희"));
    }

    @Test
    @DisplayName("어르신 정보 일괄 등록 실패 - 빈 리스트")
    void bulkRegisterElders_fail_emptyList() throws Exception {
        // given
        BulkElderRegisterRequest emptyRequest = BulkElderRegisterRequest.builder()
                .elders(List.of())
                .build();

        // when & then
        mockMvc.perform(post("/elders/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("어르신 정보 일괄 등록 실패 - 필수값 누락")
    void bulkRegisterElders_fail_validation() throws Exception {
        // given
        ElderRegisterRequest invalidRequest = new ElderRegisterRequest();
        invalidRequest.setName(""); // name 누락
        invalidRequest.setBirthDate(java.time.LocalDate.of(1940, 5, 1));
        invalidRequest.setGender(Gender.MALE);
        invalidRequest.setPhone("01012345678");
        invalidRequest.setRelationship(ElderRelation.GRANDCHILD);
        invalidRequest.setResidenceType(ResidenceType.ALONE);

        BulkElderRegisterRequest bulkRequest = BulkElderRegisterRequest.builder()
                .elders(List.of(invalidRequest))
                .build();

        // when & then
        mockMvc.perform(post("/elders/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isBadRequest());
    }
}
