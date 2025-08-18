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

    @Query("SELECT c FROM CareCallSetting c JOIN c.elder e WHERE c.firstCallTime = :now AND e.status = 'ACTIVATED'")
    List<CareCallSetting> findByFirstCallTime(@Param("now") LocalTime now);

    @Query("SELECT c FROM CareCallSetting c JOIN c.elder e WHERE c.secondCallTime = :now AND e.status = 'ACTIVATED'")
    List<CareCallSetting> findBySecondCallTime(@Param("now") LocalTime now);

    Optional<CareCallSetting> findByElder(Elder elder);
}