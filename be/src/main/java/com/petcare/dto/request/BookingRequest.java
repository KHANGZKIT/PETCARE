package com.petcare.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request đặt lịch khám cho thú cưng của khách hàng hiện tại.
 * Khách hàng không truyền customerId; server lấy từ SecurityContext.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Vui lòng chọn thú cưng")
    private Long petId;

    @NotNull(message = "Vui lòng chọn phòng khám")
    private Long clinicId;

    @NotNull(message = "Vui lòng chọn dịch vụ")
    private Long serviceId;

    @NotNull(message = "Vui lòng chọn khung giờ")
    private Long slotId;
}
