package com.petcare.service;

import com.petcare.dto.request.LoginRequest;
import com.petcare.dto.request.RegisterRequest;
import com.petcare.dto.response.LoginResponse;
import com.petcare.dto.response.UserProfileResponse;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.DuplicateResourceException;
import com.petcare.repository.UserRepository;
import com.petcare.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không chính xác"));
    }

    private boolean isActive(UserEntity user) {
        return user.isActive();
    }

    private void verifyPassword(String rawPassword, String passwordHash) {
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new BadCredentialsException("Email hoặc mật khẩu không chính xác");
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

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        String phone = req.getPhone().trim();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email đã được sử dụng");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Số điện thoại đã được sử dụng");
        }

        String encodedPassword = passwordEncoder.encode(req.getPassword());
        UserEntity newUser = new UserEntity(
                req.getFullName().trim(),
                email,
                phone,
                encodedPassword,
                UserRole.CUSTOMER
        );

        UserEntity savedUser = userRepository.save(newUser);
        String token = jwtService.generateToken(savedUser);
        return buildLoginResponse(savedUser, token);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        UserEntity user = findUserByEmail(email);
        Long clinicId = user.getClinic() != null ? user.getClinic().getId() : null;

        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .clinicId(clinicId)
                .active(user.isActive())
                .build();
    }
}
