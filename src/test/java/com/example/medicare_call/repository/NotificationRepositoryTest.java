package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.global.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 생성
        testMember = Member.builder()
                .name("테스트 사용자")
                .phone("01012345678")
                .gender(Gender.FEMALE)
                .termsAgreedAt(LocalDateTime.now())
                .build();

        memberRepository.save(testMember);

        // 테스트용 알림 데이터 생성
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 5; i++) {
            Notification notification = Notification.builder()
                    .member(testMember)
                    .title("테스트 제목 " + i)
                    .body("테스트 내용 " + i)
                    .createdAt(now.minusHours(i))  // 시간 간격을 두어 정렬 테스트
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
        }
    }

    @Test
    @DisplayName("회원 ID로 알림을 조회하고 생성일자 내림차순으로 정렬되는지 확인")
    void findByMemberIdOrderByCreatedAtDesc_ShouldReturnPagedAndSortedResults() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 3);  // 첫 페이지, 페이지 크기 3

        // when
        Page<Notification> notifications = notificationRepository.findByMember_IdOrderByCreatedAtDesc(
                testMember.getId(),
                pageRequest
        );

        // then
        assertThat(notifications).isNotNull();
        assertThat(notifications.getContent()).hasSize(3);  // 페이지 크기만큼 결과 반환
        assertThat(notifications.getTotalElements()).isEqualTo(5);  // 전체 알림 개수
        assertThat(notifications.getTotalPages()).isEqualTo(2);  // 전체 페이지 수

        // 정렬 순서 확인 (createdAt 기준 내림차순)
        assertThat(notifications.getContent())
                .extracting(Notification::getCreatedAt)
                .isSortedAccordingTo((d1, d2) -> d2.compareTo(d1));
    }

    @Test
    @DisplayName("두 번째 페이지 조회 결과 확인")
    void findByMemberIdOrderByCreatedAtDesc_ShouldReturnSecondPage() {
        // given
        PageRequest pageRequest = PageRequest.of(1, 3);  // 두 번째 페이지, 페이지 크기 3

        // when
        Page<Notification> notifications = notificationRepository.findByMember_IdOrderByCreatedAtDesc(
                testMember.getId(),
                pageRequest
        );

        // then
        assertThat(notifications).isNotNull();
        assertThat(notifications.getContent()).hasSize(2);  // 남은 데이터 개수만큼 반환
        assertThat(notifications.getNumber()).isEqualTo(1);  // 현재 페이지 번호
        assertThat(notifications.hasNext()).isFalse();  // 다음 페이지 없음
    }
}