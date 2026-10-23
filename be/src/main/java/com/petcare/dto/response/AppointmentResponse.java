package com.petcare.dto.response;

import com.petcare.entity.AppointmentEntity;
import com.petcare.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private Long id;
    private AppointmentStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long petId;
    private String petName;
    private String petSpecies;
    private Long clinicId;
    private String clinicName;
    private String clinicAddress;
    private Long serviceId;
    private String serviceName;
    private Integer durationMinutes;
    private Long slotId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public static AppointmentResponse fromEntity(AppointmentEntity entity) {
        return AppointmentResponse.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .customerId(entity.getCustomer().getId())
                .customerName(entity.getCustomer().getFullName())
                .customerPhone(entity.getCustomer().getPhone())
                .petId(entity.getPet().getId())
                .petName(entity.getPet().getName())
                .petSpecies(entity.getPet().getSpecies())
                .clinicId(entity.getClinic().getId())
                .clinicName(entity.getClinic().getName())
                .clinicAddress(entity.getClinic().getAddress())
                .serviceId(entity.getService().getId())
                .serviceName(entity.getService().getName())
                .durationMinutes(entity.getService().getDurationMinutes())
                .slotId(entity.getSlot().getId())
                .slotDate(entity.getSlot().getSlotDate())
                .startTime(entity.getSlot().getStartTime())
                .endTime(entity.getSlot().getEndTime())
                .build();
    }
}
