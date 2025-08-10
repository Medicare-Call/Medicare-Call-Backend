package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Order;
import com.example.medicare_call.global.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 주문 코드로 주문 조회
     */
    Optional<Order> findByCode(String code);
    
    /**
     * 네이버페이 결제번호로 주문 조회
     */
    Optional<Order> findByNaverpayPaymentId(String naverpayPaymentId);
    
    /**
     * 주문 코드와 상태로 주문 조회
     */
    Optional<Order> findByCodeAndStatus(String code, OrderStatus status);
    
    /**
     * 주문 코드가 존재하는지 확인
     */
    boolean existsByCode(String code);
    
    /**
     * 네이버페이 결제번호가 존재하는지 확인
     */
    boolean existsByNaverpayPaymentId(String naverpayPaymentId);
}
