package com.example.medicare_call.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ElderRegisterResponse {
    private Integer id;
    private String name;
    private LocalDate birthDate;
    private String phone;
    private String gender;
    private String relationship;
    private String residenceType;
    private Integer guardianId;
    private String guardianName;
} 