package com.petcare.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PetRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validPetRequest_passes() {
        PetRequest request = new PetRequest("Milu", "DOG");
        Set<ConstraintViolation<PetRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void nullName_fails() {
        PetRequest request = new PetRequest(null, "DOG");
        Set<ConstraintViolation<PetRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Tên thú cưng không được để trống");
    }

    @Test
    void blankName_fails() {
        PetRequest request = new PetRequest("   ", "DOG");
        Set<ConstraintViolation<PetRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Tên thú cưng không được để trống");
    }

    @Test
    void nameExceeds100Chars_fails() {
        String longName = "a".repeat(101);
        PetRequest request = new PetRequest(longName, "DOG");
        Set<ConstraintViolation<PetRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("tối đa 100 ký tự");
    }

    @Test
    void nullSpecies_fails() {
        PetRequest request = new PetRequest("Milu", null);
        Set<ConstraintViolation<PetRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Loài thú cưng không được để trống");
    }

    @Test
    void blankSpecies_fails() {
        PetRequest request = new PetRequest("Milu", "   ");
        Set<ConstraintViolation<PetRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Loài thú cưng không được để trống");
    }

    @Test
    void speciesExceeds50Chars_fails() {
        String longSpecies = "a".repeat(51);
        PetRequest request = new PetRequest("Milu", longSpecies);
        Set<ConstraintViolation<PetRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("tối đa 50 ký tự");
    }
}
