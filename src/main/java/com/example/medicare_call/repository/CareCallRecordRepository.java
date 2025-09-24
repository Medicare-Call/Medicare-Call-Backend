package com.example.medicare_call.repository;

import com.example.medicare_call.domain.CareCallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CareCallRecordRepository extends JpaRepository<CareCallRecord, Integer> {
    
    @Query("SELECT ccr FROM CareCallRecord ccr " +
            "JOIN ccr.elder e " +
       "WHERE ccr.elder.id = :elderId " +
       "AND e.status = 'ACTIVATED' " +
       "AND DATE(ccr.calledAt) = :date " +
       "AND ccr.sleepStart IS NOT NULL " +
       "ORDER BY ccr.calledAt")
    List<CareCallRecord> findByElderIdAndDateWithSleepData(@Param("elderId") Integer elderId, @Param("date") LocalDate date);

    @Query("SELECT ccr FROM CareCallRecord ccr " +
            "JOIN ccr.elder e " +
           "WHERE ccr.elder.id = :elderId " +
            "AND e.status = 'ACTIVATED' " +
           "AND DATE(ccr.calledAt) = :date " +
           "AND ccr.psychologicalDetails IS NOT NULL " +
           "ORDER BY ccr.calledAt")
    List<CareCallRecord> findByElderIdAndDateWithPsychologicalData(@Param("elderId") Integer elderId, @Param("date") LocalDate date);

    @Query("SELECT ccr FROM CareCallRecord ccr " +
            "JOIN ccr.elder e " +
           "WHERE ccr.elder.id = :elderId " +
            "AND e.status = 'ACTIVATED' " +
           "AND DATE(ccr.calledAt) = :date " +
           "AND ccr.healthDetails IS NOT NULL " +
           "ORDER BY ccr.calledAt")
    List<CareCallRecord> findByElderIdAndDateWithHealthData(@Param("elderId") Integer elderId, @Param("date") LocalDate date);


    @Query("SELECT ccr FROM CareCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND ccr.calledAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ccr.calledAt")
    List<CareCallRecord> findByElderIdAndDateBetween(@Param("elderId") Integer elderId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);



}