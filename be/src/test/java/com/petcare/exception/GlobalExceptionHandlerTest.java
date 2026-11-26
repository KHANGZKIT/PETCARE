package com.petcare.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/appointments/99");
    }

    @Test
    void handleResourceNotFoundException_returns404NotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: 99");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Không tìm thấy lịch hẹn với ID: 99");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/appointments/99");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleIllegalArgumentException_returns400BadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Không thể hủy lịch hẹn đã hoàn thành");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Không thể hủy lịch hẹn đã hoàn thành");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/appointments/99");
    }

    @Test
    void handleDuplicateResourceException_returns409Conflict() {
        DuplicateResourceException ex = new DuplicateResourceException("Khung giờ đã được đặt");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResourceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Khung giờ đã được đặt");
    }

    @Test
    void resourceNotFoundException_customConstructor_formatsCorrectly() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Appointment", "id", 99L);
        assertThat(ex.getMessage()).isEqualTo("Appointment không tồn tại với id: '99'");
    }

    @Test
    void handleAccountDisabledException_returns403Forbidden() {
        AccountDisabledException ex = new AccountDisabledException("Tài khoản đã bị tạm khóa");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountDisabledException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getMessage()).isEqualTo("Tài khoản đã bị tạm khóa");
    }

    @Test
    void handleGeneralException_illegalStateException_returns500() {
        IllegalStateException ex = new IllegalStateException("Unexpected internal state");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Đã xảy ra lỗi hệ thống, vui lòng thử lại sau");
    }
}
