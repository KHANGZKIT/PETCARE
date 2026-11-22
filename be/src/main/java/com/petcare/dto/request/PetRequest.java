package com.petcare.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO nhận thông tin tạo / cập nhật thú cưng cho khách hàng hiện tại.
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

    @Size(max = 100, message = "Giống thú cưng tối đa 100 ký tự")
    private String breed;

    @Size(max = 20, message = "Giới tính tối đa 20 ký tự")
    private String gender;

    @Size(max = 50, message = "Tuổi tối đa 50 ký tự")
    private String age;

    @DecimalMin(value = "0.0", inclusive = false, message = "Cân nặng phải lớn hơn 0")
    private BigDecimal weight;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    public PetRequest(String name, String species) {
        this.name = name;
        this.species = species;
    }
}
