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

/** Kiểm tra @NotNull trên BookingRequest — thiếu trường sẽ thành HTTP 400 qua GlobalExceptionHandler. */
class BookingRequestValidationTest {

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
    void fullRequest_passes() {
        Set<ConstraintViolation<BookingRequest>> violations =
                validator.validate(new BookingRequest(2L, 1L, 3L, 4L));
        assertThat(violations).isEmpty();
    }

    @Test
    void missingPetId_violated() {
        Set<ConstraintViolation<BookingRequest>> violations =
                validator.validate(new BookingRequest(null, 1L, 3L, 4L));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("thú cưng");
    }

    @Test
    void missingClinicId_violated() {
        Set<ConstraintViolation<BookingRequest>> violations =
                validator.validate(new BookingRequest(2L, null, 3L, 4L));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("phòng khám");
    }

    @Test
    void missingServiceId_violated() {
        Set<ConstraintViolation<BookingRequest>> violations =
                validator.validate(new BookingRequest(2L, 1L, null, 4L));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("dịch vụ");
    }

    @Test
    void missingSlotId_violated() {
        Set<ConstraintViolation<BookingRequest>> violations =
                validator.validate(new BookingRequest(2L, 1L, 3L, null));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("khung giờ");
    }

    @Test
    void emptyRequest_allFourViolated() {
        Set<ConstraintViolation<BookingRequest>> violations =
                validator.validate(new BookingRequest());
        assertThat(violations).hasSize(4);
    }
}
