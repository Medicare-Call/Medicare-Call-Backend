package com.example.medicare_call.domain;

import com.example.medicare_call.global.enums.OrderStatus;
import com.example.medicare_call.global.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Orders")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    // TODO: 플랜 목록들을 Enum, 혹은 테이블로 분리하여 사용
    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;
    
    @Column(name = "product_count", nullable = false)
    private Integer productCount;
    
    @Column(name = "total_pay_amount", nullable = false)
    private Integer totalPayAmount;
    
    @Column(name = "tax_scope_amount", nullable = false)
    private Integer taxScopeAmount; // 과세 금액
    
    @Column(name = "tax_ex_scope_amount", nullable = false)
    private Integer taxExScopeAmount; // 면세 금액
    
    @Column(name = "naverpay_reserve_id", length = 64)
    private String naverpayReserveId; // 네이버페이 결제 예약 번호
    
    @Column(name = "naverpay_payment_id", length = 64)
    private String naverpayPaymentId; // 네이버페이 결제 번호
    
    @Column(name = "naverpay_hist_id", length = 64)
    private String naverpayHistId; // 네이버페이 결제 이력 번호
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    // 결제한 회원 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    // 결제 대상 어르신 ID 목록 (JSON 형태로 저장: "[1,2,3]")
    // TODO: 소주문 테이블로 분리 여부를 검토
    @Column(name = "elder_ids", columnDefinition = "JSON")
    private String elderIds;
    
    // 결제 수단
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.CREATED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 결제 승인 시 호출되는 메서드
    public void approvePayment(String naverpayPaymentId, String naverpayHistId) {
        this.naverpayPaymentId = naverpayPaymentId;
        this.naverpayHistId = naverpayHistId;
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.approvedAt = LocalDateTime.now();
    }
    
    // 결제 실패 시 호출되는 메서드
    public void failPayment() {
        this.status = OrderStatus.PAYMENT_FAILED;
    }

    // 주문 정보 변조 시 호출되는 메서드
    public void tamper() {
        this.status = OrderStatus.TAMPERED;
    }
}
