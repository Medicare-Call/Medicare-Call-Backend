package com.example.medicare_call.service;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Subscription;
import com.example.medicare_call.dto.SubscriptionResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.enums.SubscriptionPlan;
import com.example.medicare_call.global.enums.SubscriptionStatus;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription 서비스 테스트")
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("구독 정보 조회 성공")
    void getSubscriptions_Success() {
        // given
        Long memberId = 1L;
        Member member = Member.builder().id(memberId.intValue()).name("테스트회원").build();
        Elder elder = Elder.builder().id(1).name("김어르신").guardian(member).build();
        Subscription subscription = Subscription.builder()
                .id(1L)
                .member(member)
                .elder(elder)
                .plan(SubscriptionPlan.PREMIUM)
                .price(29000)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .build();

        given(memberRepository.existsById(memberId.intValue())).willReturn(true);
        given(subscriptionRepository.findByMemberId(memberId.intValue())).willReturn(Collections.singletonList(subscription));

        // when
        List<SubscriptionResponse> responses = subscriptionService.getSubscriptionsByMember(memberId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getElderId()).isEqualTo(elder.getId());
        assertThat(responses.get(0).getName()).isEqualTo(elder.getName());
        assertThat(responses.get(0).getPlan()).isEqualTo(subscription.getPlan().getPlanName());
    }

    @Test
    @DisplayName("구독 정보 조회 실패 - 회원 없음")
    void getSubscriptions_Fail_MemberNotFound() {
        // given
        Long memberId = 999L;
        given(memberRepository.existsById(memberId.intValue())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> subscriptionService.getSubscriptionsByMember(memberId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다:");
    }

    @Test
    @DisplayName("구독 정보 조회 실패 - 구독 정보 없음")
    void getSubscriptions_Fail_NoSubscriptions() {
        // given
        Long memberId = 1L;
        given(memberRepository.existsById(memberId.intValue())).willReturn(true);
        given(subscriptionRepository.findByMemberId(memberId.intValue())).willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> subscriptionService.getSubscriptionsByMember(memberId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("해당 회원의 구독 정보를 찾을 수 없습니다.");
    }
}
