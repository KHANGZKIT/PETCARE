package com.petcare.dto.response;

import com.petcare.entity.PetEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO trả về thông tin thú cưng cho client Android.
 * Không để lộ thông tin nhạy cảm của người dùng hay quan hệ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetResponse {

    private Long id;
    private String name;
    private String species;

    public static PetResponse fromEntity(PetEntity entity) {
        if (entity == null) {
            return null;
        }
        return PetResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .species(entity.getSpecies())
                .build();
    }
}
