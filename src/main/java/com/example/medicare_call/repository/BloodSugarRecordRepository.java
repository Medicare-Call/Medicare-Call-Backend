package com.example.medicare_call.repository;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BloodSugarRecordRepository extends JpaRepository<BloodSugarRecord, Integer> {

    @Query(value = "SELECT bsr FROM BloodSugarRecord bsr " +
        "JOIN bsr.careCallRecord ccr " +
        "WHERE ccr.elder.id = :elderId " +
        "AND bsr.measurementType = :measurementType " +
        "ORDER BY bsr.recordedAt DESC",
        countQuery = "SELECT count(bsr) FROM BloodSugarRecord bsr " +
            "JOIN bsr.careCallRecord ccr " +
            "WHERE ccr.elder.id = :elderId " +
            "AND bsr.measurementType = :measurementType")
    Page<BloodSugarRecord> findByElderIdAndMeasurementTypeOrderByRecordedAtDesc(
        @Param("elderId") Integer elderId,
        @Param("measurementType") BloodSugarMeasurementType measurementType,
        Pageable pageable
    );

    @Query("SELECT bsr FROM BloodSugarRecord bsr " +
           "JOIN bsr.careCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND bsr.measurementType = :measurementType " +
           "AND DATE(bsr.recordedAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY bsr.recordedAt")
    List<BloodSugarRecord> findByElderIdAndMeasurementTypeAndDateBetween(
            @Param("elderId") Integer elderId,
            @Param("measurementType") BloodSugarMeasurementType measurementType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT bsr FROM BloodSugarRecord bsr " +
           "JOIN bsr.careCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(bsr.recordedAt) = :date " +
           "ORDER BY bsr.recordedAt")
    List<BloodSugarRecord> findByElderIdAndDate(@Param("elderId") Integer elderId, @Param("date") LocalDate date);

    @Query("SELECT bsr FROM BloodSugarRecord bsr " +
           "JOIN bsr.careCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(bsr.recordedAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY bsr.recordedAt")
    List<BloodSugarRecord> findByElderIdAndDateBetween(@Param("elderId") Integer elderId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
} 