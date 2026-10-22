package com.petcare.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcare.dto.request.LoginRequest;
import com.petcare.dto.response.LoginResponse;
import com.petcare.entity.UserEntity;
import com.petcare.repository.UserRepository;
import com.petcare.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không chính xác"));
    }

    private boolean isActive(UserEntity user) {
        return user.isActive();
    }

    private void verifyPassword(String rawPassword, String passwordHash) {
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không chính xác");
        }
    }

    private LoginResponse buildLoginResponse(UserEntity user, String token) {
        Long clinicId = user.getClinic() != null ? user.getClinic().getId() : null;
        return new LoginResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                clinicId
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        UserEntity user = findUserByEmail(req.getEmail());

        if (!isActive(user)) {
            throw new IllegalStateException("Tài khoản đã bị tạm khóa, vui lòng liên hệ quản trị viên");
        }

        verifyPassword(req.getPassword(), user.getPasswordHash());

        String token = jwtService.generateToken(user);
        return buildLoginResponse(user, token);
    }
}
