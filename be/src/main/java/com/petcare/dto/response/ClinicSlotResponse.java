package com.petcare.dto.response;

import com.petcare.entity.ClinicSlotEntity;
import com.petcare.entity.SlotStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicSlotResponse {

    private Long id;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;

    public static ClinicSlotResponse fromEntity(ClinicSlotEntity entity) {
        return ClinicSlotResponse.builder()
                .id(entity.getId())
                .slotDate(entity.getSlotDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .build();
    }
}

