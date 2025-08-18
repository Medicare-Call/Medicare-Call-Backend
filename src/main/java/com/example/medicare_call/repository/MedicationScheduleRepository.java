package com.example.medicare_call.repository;

import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.domain.Elder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Integer> {
    @Query("SELECT ms FROM MedicationSchedule ms JOIN ms.elder e WHERE e.id = :elderId AND e.status = 'ACTIVATED'")
    List<MedicationSchedule> findByElderId(@Param("elderId") Integer elderId);
    List<MedicationSchedule> findByElder(Elder elder);
    void deleteAllByElder(Elder elder);
    
} 
