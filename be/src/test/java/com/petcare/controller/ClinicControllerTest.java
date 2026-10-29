package com.petcare.controller;

import com.petcare.dto.response.ClinicResponse;
import com.petcare.dto.response.ClinicSlotResponse;
import com.petcare.dto.response.ServiceResponse;
import com.petcare.entity.SlotStatus;
import com.petcare.exception.GlobalExceptionHandler;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.service.ClinicService;
import com.petcare.service.ClinicSlotService;
import com.petcare.service.ServiceClinicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClinicControllerTest {

    @Mock
    private ClinicService clinicService;

    @Mock
    private ServiceClinicService serviceClinicService;

    @Mock
    private ClinicSlotService clinicSlotService;

    @InjectMocks
    private ClinicController clinicController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clinicController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllClinics_returns200() throws Exception {
        ClinicResponse clinic = ClinicResponse.builder()
                .id(1L)
                .name("Phòng khám Thú y An Nhiên")
                .area("Quận 1")
                .active(true)
                .build();

        when(clinicService.getAllClinics(null)).thenReturn(List.of(clinic));

        mockMvc.perform(get("/api/v1/clinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Phòng khám Thú y An Nhiên"));
    }

    @Test
    void getClinicById_success_returns200() throws Exception {
        ClinicResponse clinic = ClinicResponse.builder()
                .id(1L)
                .name("Phòng khám Thú y An Nhiên")
                .area("Quận 1")
                .active(true)
                .build();

        when(clinicService.getClinicById(1L)).thenReturn(clinic);

        mockMvc.perform(get("/api/v1/clinics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Phòng khám Thú y An Nhiên"));
    }

    @Test
    void getClinicById_notFound_returns404() throws Exception {
        when(clinicService.getClinicById(99L))
                .thenThrow(new ResourceNotFoundException("Phòng khám", "id", 99L));

        mockMvc.perform(get("/api/v1/clinics/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getServiceByClinicId_returns200() throws Exception {
        ServiceResponse service = ServiceResponse.builder()
                .id(1L)
                .clinicId(1L)
                .name("Khám tổng quát")
                .duration(30)
                .active(true)
                .build();

        when(serviceClinicService.getServicesByClinicId(1L)).thenReturn(List.of(service));

        mockMvc.perform(get("/api/v1/clinics/1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Khám tổng quát"))
                .andExpect(jsonPath("$[0].duration").value(30));
    }

    @Test
    void getSlotsByDate_returns200() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        ClinicSlotResponse slot = ClinicSlotResponse.builder()
                .id(1L)
                .slotDate(date)
                .startTime(LocalTime.of(8, 30))
                .endTime(LocalTime.of(9, 0))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(clinicSlotService.getSlotsByClinicAndDate(eq(1L), eq(date))).thenReturn(List.of(slot));

        mockMvc.perform(get("/api/v1/clinics/1/slots").param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }
}
