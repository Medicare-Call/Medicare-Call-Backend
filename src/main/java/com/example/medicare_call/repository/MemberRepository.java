package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Boolean existsByPhone(String phone);
    Optional<Member> findByPhone(String phone);
}