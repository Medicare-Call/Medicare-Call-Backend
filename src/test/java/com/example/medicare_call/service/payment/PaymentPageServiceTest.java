package com.example.medicare_call.service.payment;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Order;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentPageServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Model model;

    @InjectMocks
    private PaymentPageService paymentPageService;

    private Member testMember;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1)
                .name("testUser")
                .build();

        testOrder = Order.builder()
                .code("ORDER-TEST-001")
                .productName("Test Product")
                .totalPayAmount(10000)
                .member(testMember)
                .build();
    }

    @Test
    @DisplayName("결제 페이지 데이터 준비 성공")
    void preparePaymentPage_success() {
        // given
        when(orderRepository.findByCode("ORDER-TEST-001")).thenReturn(Optional.of(testOrder));

        // when
        paymentPageService.preparePaymentPage(1L, "ORDER-TEST-001", model);

        // then
        verify(orderRepository).findByCode("ORDER-TEST-001");
        verify(model).addAttribute("orderCode", "ORDER-TEST-001");
        verify(model).addAttribute("productName", "Test Product");
        verify(model).addAttribute("totalPayAmount", 10000);
    }

    @Test
    @DisplayName("결제 페이지 데이터 준비 실패 - 주문자 불일치")
    void preparePaymentPage_fail_invalid_member() {
        // given
        when(orderRepository.findByCode("ORDER-TEST-001")).thenReturn(Optional.of(testOrder));
        Long wrongMemberId = 2L;

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentPageService.preparePaymentPage(wrongMemberId, "ORDER-TEST-001", model);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.HANDLE_ACCESS_DENIED);
        verify(orderRepository).findByCode("ORDER-TEST-001");
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("결제 페이지 데이터 준비 실패 - 주문 없음")
    void preparePaymentPage_fail_order_not_found() {
        // given
        when(orderRepository.findByCode("NON-EXISTENT-ORDER")).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            paymentPageService.preparePaymentPage(1L, "NON-EXISTENT-ORDER", model);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }
}
