package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.global.enums.ElderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ElderRepository extends JpaRepository<Elder, Integer> {
    List<Elder> findByGuardian(Member member);

    @Query("SELECT e FROM Elder e WHERE e.id = :id AND e.status = :status")
    Optional<Elder> findByIdAndStatus(@Param("id") Integer id, @Param("status") ElderStatus status);

    @Query("SELECT e FROM Elder e WHERE e.guardian = :guardian AND e.status = :status")
    List<Elder> findByGuardianAndStatus(@Param("guardian") Member guardian, @Param("status") ElderStatus status);

    // 모든 상태의 어르신을 조회 (관리자용)
    @Query("SELECT e FROM Elder e WHERE e.id = :id")
    Optional<Elder> findByIdIgnoreStatus(@Param("id") Integer id);

} 