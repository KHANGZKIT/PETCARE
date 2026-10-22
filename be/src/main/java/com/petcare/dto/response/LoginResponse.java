package com.petcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private Long clinicId;
}
