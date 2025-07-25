package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ElderDiseaseRepository extends JpaRepository<ElderDisease, Object> {

    @Query("SELECT ed.disease FROM ElderDisease ed WHERE ed.elder = :elder")
    List<Disease> findDiseasesByElder(Elder elder);
}