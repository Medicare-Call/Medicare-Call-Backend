package com.example.medicare_call.repository;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CareCallSettingRepository extends JpaRepository<CareCallSetting, Integer> {

    // 1차 콜 시간 범위 조회
    @Query("SELECT c FROM CareCallSetting c JOIN c.elder e WHERE c.firstCallTime BETWEEN :startTime AND :endTime AND e.status = 'ACTIVATED'")
    List<CareCallSetting> findByFirstCallTimeBetween(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    // 2차 콜 시간 범위 조회
    @Query("SELECT c FROM CareCallSetting c JOIN c.elder e WHERE c.secondCallTime BETWEEN :startTime AND :endTime AND e.status = 'ACTIVATED'")
    List<CareCallSetting> findBySecondCallTimeBetween(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    // 3차 콜 시간 범위 조회
    @Query("SELECT c FROM CareCallSetting c JOIN c.elder e WHERE c.thirdCallTime BETWEEN :startTime AND :endTime AND e.status = 'ACTIVATED'")
    List<CareCallSetting> findByThirdCallTimeBetween(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    Optional<CareCallSetting> findByElder(Elder elder);
}