package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ElderHealthInfoRepository extends JpaRepository<ElderHealthInfo, Integer> {
    @Query("SELECT ehi FROM ElderHealthInfo ehi JOIN ehi.elder e WHERE e.id = :elderId AND e.status = 'ACTIVATED'")
    ElderHealthInfo findByElderId(@Param("elderId") Integer elderId);
    void deleteAllByElder(Elder elder);
    Optional<ElderHealthInfo> findByElder(Elder elder);
} 