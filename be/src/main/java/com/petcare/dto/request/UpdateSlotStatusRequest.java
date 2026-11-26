package com.petcare.dto.request;

import com.petcare.entity.SlotStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSlotStatusRequest {

    @NotNull(message = "Trạng thái khung giờ không được để trống")
    private SlotStatus status;
}
