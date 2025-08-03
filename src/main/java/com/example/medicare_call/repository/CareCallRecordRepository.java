package com.example.medicare_call.repository;

import com.example.medicare_call.domain.CareCallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CareCallRecordRepository extends JpaRepository<CareCallRecord, Integer> {
    
                @Query("SELECT ccr FROM CareCallRecord ccr " +
                   "WHERE ccr.elder.id = :elderId " +
                   "AND DATE(ccr.startTime) = :date " +
                   "AND ccr.sleepStart IS NOT NULL " +
                   "ORDER BY ccr.startTime")
            List<CareCallRecord> findByElderIdAndDateWithSleepData(@Param("elderId") Integer elderId, @Param("date") LocalDate date);

            @Query("SELECT ccr FROM CareCallRecord ccr " +
                   "WHERE ccr.elder.id = :elderId " +
                   "AND DATE(ccr.startTime) = :date " +
                   "AND ccr.psychologicalDetails IS NOT NULL " +
                   "ORDER BY ccr.startTime")
            List<CareCallRecord> findByElderIdAndDateWithPsychologicalData(@Param("elderId") Integer elderId, @Param("date") LocalDate date);

            @Query("SELECT ccr FROM CareCallRecord ccr " +
                   "WHERE ccr.elder.id = :elderId " +
                   "AND DATE(ccr.startTime) = :date " +
                   "AND ccr.healthDetails IS NOT NULL " +
                   "ORDER BY ccr.startTime")
            List<CareCallRecord> findByElderIdAndDateWithHealthData(@Param("elderId") Integer elderId, @Param("date") LocalDate date);

    @Query("SELECT ccr FROM CareCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(ccr.startTime) BETWEEN :startDate AND :endDate " +
           "AND ccr.sleepStart IS NOT NULL " +
           "ORDER BY ccr.startTime")
    List<CareCallRecord> findByElderIdAndDateBetweenWithSleepData(@Param("elderId") Integer elderId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ccr FROM CareCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(ccr.startTime) BETWEEN :startDate AND :endDate " +
           "AND ccr.psychologicalDetails IS NOT NULL " +
           "ORDER BY ccr.startTime")
    List<CareCallRecord> findByElderIdAndDateBetweenWithPsychologicalData(@Param("elderId") Integer elderId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ccr FROM CareCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(ccr.startTime) BETWEEN :startDate AND :endDate " +
           "AND ccr.healthDetails IS NOT NULL " +
           "ORDER BY ccr.startTime")
    List<CareCallRecord> findByElderIdAndDateBetweenWithHealthData(@Param("elderId") Integer elderId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ccr FROM CareCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(ccr.startTime) BETWEEN :startDate AND :endDate " +
           "ORDER BY ccr.startTime")
    List<CareCallRecord> findByElderIdAndDateBetween(@Param("elderId") Integer elderId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
} 