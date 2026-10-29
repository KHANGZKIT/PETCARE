package com.petcare.controller;

import com.petcare.dto.request.CreateSlotRequest;
import com.petcare.dto.response.ClinicSlotResponse;
import com.petcare.entity.SlotStatus;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.DuplicateResourceException;
import com.petcare.exception.GlobalExceptionHandler;
import com.petcare.service.ClinicSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StaffSlotControllerTest {

    @Mock
    private ClinicSlotService clinicSlotService;

    @InjectMocks
    private StaffSlotController staffSlotController;

    private MockMvc mockMvc;
    private UsernamePasswordAuthenticationToken staffAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(staffSlotController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserEntity staffUser = new UserEntity("Nhân Viên", "staff@petcare.com", "0911000001", "hash", UserRole.STAFF);
        staffAuth = new UsernamePasswordAuthenticationToken(
                staffUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STAFF"))
        );
    }

    @Test
    void getStaffSlots_success_returns200() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        ClinicSlotResponse slot = ClinicSlotResponse.builder()
                .id(1L)
                .slotDate(date)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(clinicSlotService.getStaffSlots(eq("staff@petcare.com"), eq(date)))
                .thenReturn(List.of(slot));

        mockMvc.perform(get("/api/v1/staff/slots")
                        .param("date", date.toString())
                        .principal(staffAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void createSlot_success_returns201() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ClinicSlotResponse response = ClinicSlotResponse.builder()
                .id(5L)
                .slotDate(tomorrow)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(clinicSlotService.createSlot(any(CreateSlotRequest.class), eq("staff@petcare.com")))
                .thenReturn(response);

        String json = """
                {
                    "slotDate": "%s",
                    "startTime": "10:00:00",
                    "endTime": "10:30:00"
                }
                """.formatted(tomorrow);

        mockMvc.perform(post("/api/v1/staff/slots")
                        .principal(staffAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void createSlot_missingDate_returns400() throws Exception {
        String json = """
                {
                    "startTime": "10:00:00",
                    "endTime": "10:30:00"
                }
                """;

        mockMvc.perform(post("/api/v1/staff/slots")
                        .principal(staffAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.slotDate").value("Ngày của khung giờ không được để trống"));
    }

    @Test
    void createSlot_conflict_returns409() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(clinicSlotService.createSlot(any(CreateSlotRequest.class), eq("staff@petcare.com")))
                .thenThrow(new DuplicateResourceException("Khung giờ bị trùng hoặc giao thoa"));

        String json = """
                {
                    "slotDate": "%s",
                    "startTime": "10:00:00",
                    "endTime": "10:30:00"
                }
                """.formatted(tomorrow);

        mockMvc.perform(post("/api/v1/staff/slots")
                        .principal(staffAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Khung giờ bị trùng hoặc giao thoa"));
    }
}
