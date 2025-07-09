package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
} 