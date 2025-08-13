package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ElderHealthInfoRepository extends JpaRepository<ElderHealthInfo, Integer> {
    ElderHealthInfo findByElderId(Integer elderId);
    void deleteAllByElder(Elder elder);
    Optional<ElderHealthInfo> findByElder(Elder elder);
} 