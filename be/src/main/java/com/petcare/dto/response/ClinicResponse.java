package com.petcare.dto.response;

import com.petcare.entity.ClinicEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicResponse {
    private Long id;
    private String name;
    private String address;
    private String area;
    private String phone;
    private String workingHourNote;
    private boolean active;

    public static ClinicResponse fromEntity(ClinicEntity entity) {
        return ClinicResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .area(entity.getArea())
                .phone(entity.getPhone())
                .workingHourNote(entity.getWorkingHoursNote())
                .active(entity.isActive())
                .build();
    }
}
