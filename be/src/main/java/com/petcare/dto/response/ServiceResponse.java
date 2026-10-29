package com.petcare.dto.response;

import com.petcare.entity.ServiceEntity;

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
public class ServiceResponse {
    private Long id;
    private Long clinicId;
    private String name;
    private Integer duration; // phút
    private boolean active;

    public static ServiceResponse fromEntity(ServiceEntity entity) {
        return ServiceResponse.builder().id(entity.getId()).clinicId(entity.getClinic().getId()).name(entity.getName())
                .duration(entity.getDurationMinutes()).active(entity.isActive()).build();
    }

}
