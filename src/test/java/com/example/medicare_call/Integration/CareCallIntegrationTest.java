package com.example.medicare_call.Integration;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.MemberElder;
import com.example.medicare_call.global.enums.CallRecurrenceType;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.MemberElderAuthority;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.service.carecall.CareCallSchedulerService;
import com.example.medicare_call.service.carecall.CareCallRequestSenderService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@SpringBootTest
@AutoConfigureMockMvc
@Disabled("실제 전화 서버 요청이므로 일시적으로 비활성화. " +
        "테스트 하려면 1. 전화번호를 본인 것으로 바꾸고 2.test application.yml파일의 care-call.url을 요청 엔드포인트로 변경")
class CareCallIntegrationTest {

    @Autowired
    private CareCallSchedulerService schedulerService;

    @Autowired
    private CareCallSettingRepository settingRepository;

    @Autowired
    private ElderRepository elderRepository;

    @Autowired
    private CareCallRequestSenderService careCallRequestSenderService;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberElderRepository memberElderRepository;

    @Test
    void 크론잡_전화요청() {
        // given
        Member guardian = memberRepository.save(
                Member.builder()
                        .name("보호자")
                        .phone("01099998888")
                        .gender(Gender.FEMALE)
                        .termsAgreedAt(LocalDateTime.now())
                        .build()
        );

        Elder elder = elderRepository.save(
                Elder.builder()
                        .id(1)
                        .name("테스터")
                        .birthDate(LocalDate.of(1940, 1, 1))
                        .gender((byte) 1)
                        .phone("+821088000000")
                        .relationship(ElderRelation.CHILD)
                        .residenceType(ResidenceType.ALONE)
                        .build()
        );
        memberElderRepository.save(
                MemberElder.builder()
                        .guardian(guardian)
                        .elder(elder)
                        .authority(MemberElderAuthority.MANAGE)
                        .build()
        );

        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        settingRepository.save(
                CareCallSetting.builder()
                        .elder(elder)
                        .firstCallTime(now)
                        .recurrence(CallRecurrenceType.DAILY)
                        .build()
        );

        // when
        schedulerService.checkAndSendCalls();

        // then: 실제 전화 여부 확인
    }
}
