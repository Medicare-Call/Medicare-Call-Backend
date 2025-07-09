package com.example.medicare_call.domain;

import java.io.Serializable;
import java.util.Objects;

public class ElderDiseaseId implements Serializable {
    private Integer elder;
    private Integer disease;

    public ElderDiseaseId() {}

    public ElderDiseaseId(Integer elder, Integer disease) {
        this.elder = elder;
        this.disease = disease;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElderDiseaseId that = (ElderDiseaseId) o;
        return Objects.equals(elder, that.elder) && Objects.equals(disease, that.disease);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elder, disease);
    }
} 