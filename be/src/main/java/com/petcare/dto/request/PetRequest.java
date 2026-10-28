package com.petcare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO nhận thông tin tạo thú cưng mới cho khách hàng hiện tại.
 * Không chứa thông tin ownerId; server tự động gán chủ sở hữu từ SecurityContext.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetRequest {

    @NotBlank(message = "Tên thú cưng không được để trống")
    @Size(max = 100, message = "Tên thú cưng tối đa 100 ký tự")
    private String name;

    @NotBlank(message = "Loài thú cưng không được để trống")
    @Size(max = 50, message = "Loài thú cưng tối đa 50 ký tự")
    private String species;
}
