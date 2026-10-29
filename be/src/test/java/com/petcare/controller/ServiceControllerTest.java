package com.petcare.controller;

import com.petcare.dto.response.ServiceResponse;
import com.petcare.exception.GlobalExceptionHandler;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.service.ServiceClinicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceControllerTest {

    @Mock
    private ServiceClinicService serviceClinicService;

    @InjectMocks
    private ServiceController serviceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(serviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getServiceById_success_returns200() throws Exception {
        ServiceResponse service = ServiceResponse.builder()
                .id(1L)
                .clinicId(1L)
                .name("Khám tổng quát")
                .duration(30)
                .active(true)
                .build();

        when(serviceClinicService.getServiceById(1L)).thenReturn(service);

        mockMvc.perform(get("/api/v1/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Khám tổng quát"));
    }

    @Test
    void getServiceById_notFound_returns404() throws Exception {
        when(serviceClinicService.getServiceById(99L))
                .thenThrow(new ResourceNotFoundException("Dịch vụ", "id", 99L));

        mockMvc.perform(get("/api/v1/services/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getServicesByClinicId_returns200() throws Exception {
        ServiceResponse service = ServiceResponse.builder()
                .id(1L)
                .clinicId(1L)
                .name("Khám tổng quát")
                .duration(30)
                .active(true)
                .build();

        when(serviceClinicService.getServicesByClinicId(1L)).thenReturn(List.of(service));

        mockMvc.perform(get("/api/v1/services").param("clinicId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Khám tổng quát"));
    }
}
