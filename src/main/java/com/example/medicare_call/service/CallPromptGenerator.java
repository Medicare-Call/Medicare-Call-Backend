package com.example.medicare_call.service;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;

import java.util.List;

public interface CallPromptGenerator {
    String generate(Elder elder, ElderHealthInfo healthInfo,
                    List<Disease> diseases, List<MedicationSchedule> medicationSchedules);
}
