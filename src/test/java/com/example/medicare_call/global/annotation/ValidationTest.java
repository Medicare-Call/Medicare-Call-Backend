package com.example.medicare_call.global.annotation;

import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.dto.ElderHealthInfoCreateRequest;
import com.example.medicare_call.dto.ElderHealthInfoCreateRequestWithElderId;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.enums.ElderHealthNoteType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 생년월일이면 Validation을 통과한다")
    void validBirthDate_passesValidation() {
        // given
        ElderRegisterRequest request = new ElderRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.of(1940, 5, 1));
        request.setGender(Gender.MALE);
        request.setPhone("01012345678");
        request.setRelationship(ElderRelation.GRANDCHILD);
        request.setResidenceType(ResidenceType.ALONE);

        // when
        Set<ConstraintViolation<ElderRegisterRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("미래의 생년월일이면 Validation에 실패한다")
    void futureBirthDate_failsValidation() {
        // given
        ElderRegisterRequest request = new ElderRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.now().plusDays(1)); // 미래 날짜
        request.setGender(Gender.MALE);
        request.setPhone("01012345678");
        request.setRelationship(ElderRelation.GRANDCHILD);
        request.setResidenceType(ResidenceType.ALONE);

        // when
        Set<ConstraintViolation<ElderRegisterRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> 
            violation.getPropertyPath().toString().equals("birthDate") &&
            violation.getMessage().contains("생년월일이 유효하지 않습니다")
        );
    }

    @Test
    @DisplayName("1900년 이전의 생년월일이면 Validation에 실패한다")
    void tooOldBirthDate_failsValidation() {
        // given
        ElderRegisterRequest request = new ElderRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.of(1899, 12, 31)); // 1900년 이전
        request.setGender(Gender.MALE);
        request.setPhone("01012345678");
        request.setRelationship(ElderRelation.GRANDCHILD);
        request.setResidenceType(ResidenceType.ALONE);

        // when
        Set<ConstraintViolation<ElderRegisterRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> 
            violation.getPropertyPath().toString().equals("birthDate") &&
            violation.getMessage().contains("생년월일이 유효하지 않습니다")
        );
    }

    @Test
    @DisplayName("유효한 휴대폰 번호면 Validation을 통과한다")
    void validPhoneNumber_passesValidation() {
        // given
        ElderRegisterRequest request = new ElderRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.of(1940, 5, 1));
        request.setGender(Gender.MALE);
        request.setPhone("01012345678");
        request.setRelationship(ElderRelation.GRANDCHILD);
        request.setResidenceType(ResidenceType.ALONE);

        // when
        Set<ConstraintViolation<ElderRegisterRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("잘못된 형식의 휴대폰 번호면 Validation에 실패한다")
    void invalidPhoneNumber_failsValidation() {
        // given
        ElderRegisterRequest request = new ElderRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.of(1940, 5, 1));
        request.setGender(Gender.MALE);
        request.setPhone("010-1234-5678"); // 하이픈 포함
        request.setRelationship(ElderRelation.GRANDCHILD);
        request.setResidenceType(ResidenceType.ALONE);

        // when
        Set<ConstraintViolation<ElderRegisterRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation -> 
            violation.getPropertyPath().toString().equals("phone") &&
            violation.getMessage().contains("휴대폰 번호가 유효하지 않습니다")
        );
    }

    @Test
    @DisplayName("010으로 시작하지 않는 휴대폰 번호면 Validation에 실패한다")
    void phoneNumberNotStartingWith010_failsValidation() {
        // given
        ElderRegisterRequest request = new ElderRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.of(1940, 5, 1));
        request.setGender(Gender.MALE);
        request.setPhone("01112345678"); // 011로 시작
        request.setRelationship(ElderRelation.GRANDCHILD);
        request.setResidenceType(ResidenceType.ALONE);

        // when
        Set<ConstraintViolation<ElderRegisterRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation ->
            violation.getPropertyPath().toString().equals("phone") &&
            violation.getMessage().contains("휴대폰 번호가 유효하지 않습니다")
        );
    }

    @Test
    @DisplayName("elderId가 null이면 Validation에 실패한다")
    void nullElderId_failsValidation() {
        // given
        ElderHealthInfoCreateRequestWithElderId request = ElderHealthInfoCreateRequestWithElderId.builder()
                .elderId(null) // null elderId
                .diseaseNames(List.of("당뇨병"))
                .build();

        // when
        Set<ConstraintViolation<ElderHealthInfoCreateRequestWithElderId>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(violation ->
            violation.getPropertyPath().toString().equals("elderId") &&
            violation.getMessage().contains("어르신 ID는 필수입니다")
        );
    }
} 