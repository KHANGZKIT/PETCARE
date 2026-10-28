package com.petcare.controller;

import com.petcare.exception.GlobalExceptionHandler;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAppointmentDetail_notFound_returns404() throws Exception {
        when(appointmentService.getMyAppointmentDetail(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy lịch hẹn hoặc bạn không có quyền xem"));

        mockMvc.perform(get("/api/v1/appointments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy lịch hẹn hoặc bạn không có quyền xem"))
                .andExpect(jsonPath("$.path").value("/api/v1/appointments/999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void cancelAppointment_notFound_returns404() throws Exception {
        when(appointmentService.cancelAppointment(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy lịch hẹn hoặc bạn không có quyền hủy"));

        mockMvc.perform(put("/api/v1/appointments/999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy lịch hẹn hoặc bạn không có quyền hủy"))
                .andExpect(jsonPath("$.path").value("/api/v1/appointments/999/cancel"));
    }

    @Test
    void cancelAppointment_invalidState_returns400() throws Exception {
        when(appointmentService.cancelAppointment(eq(1L), any()))
                .thenThrow(new IllegalArgumentException("Không thể hủy lịch hẹn đang ở trạng thái: COMPLETED"));

        mockMvc.perform(put("/api/v1/appointments/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Không thể hủy lịch hẹn đang ở trạng thái: COMPLETED"))
                .andExpect(jsonPath("$.path").value("/api/v1/appointments/1/cancel"));
    }
}
