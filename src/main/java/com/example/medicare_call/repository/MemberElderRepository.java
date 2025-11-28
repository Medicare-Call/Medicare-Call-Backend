package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MemberElder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberElderRepository extends JpaRepository<MemberElder, Long> {
    List<MemberElder> findByGuardian_Id(Integer guardianId);

    Optional<MemberElder> findByGuardian_IdAndElder_Id(Integer guardianId, Integer elderId);

    List<MemberElder> findByElder_Id(Integer elderId);

    List<MemberElder> findByElder(Elder elder);
}
