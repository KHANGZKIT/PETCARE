package com.petcare.service;

import com.petcare.dto.request.LoginRequest;
import com.petcare.dto.request.RegisterRequest;
import com.petcare.dto.response.LoginResponse;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.AccountDisabledException;
import com.petcare.exception.DuplicateResourceException;
import com.petcare.repository.UserRepository;
import com.petcare.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private UserEntity activeUser;
    private UserEntity disabledUser;

    @BeforeEach
    void setUp() throws Exception {
        activeUser = new UserEntity("Nguyễn An", "an@petcare.com", "0988000001", "hashed-pw", UserRole.CUSTOMER);
        disabledUser = new UserEntity("Trần Khóa", "locked@petcare.com", "0988000002", "hashed-pw", UserRole.CUSTOMER);
        disabledUser.setActive(false);

        Field idField = UserEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(activeUser, 1L);
        idField.set(disabledUser, 2L);
    }

    private static LoginRequest loginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    @Test
    void login_success_returnsTokenAndProfile() {
        LoginRequest req = loginRequest("an@petcare.com", "Password123");
        when(userRepository.findByEmailIgnoreCase("an@petcare.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password123", "hashed-pw")).thenReturn(true);
        when(jwtService.generateToken(activeUser)).thenReturn("jwt-token");

        LoginResponse res = authService.login(req);

        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getEmail()).isEqualTo("an@petcare.com");
        assertThat(res.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void login_wrongPasswordOnDisabledUser_throwsBadCredentialsWithoutLeakingAccountStatus() {
        LoginRequest req = loginRequest("locked@petcare.com", "wrong-pass");
        when(userRepository.findByEmailIgnoreCase("locked@petcare.com")).thenReturn(Optional.of(disabledUser));
        when(passwordEncoder.matches("wrong-pass", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Email hoặc mật khẩu không chính xác");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_correctPasswordOnDisabledUser_throwsAccountDisabledException() {
        LoginRequest req = loginRequest("locked@petcare.com", "Password123");
        when(userRepository.findByEmailIgnoreCase("locked@petcare.com")).thenReturn(Optional.of(disabledUser));
        when(passwordEncoder.matches("Password123", "hashed-pw")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AccountDisabledException.class)
                .hasMessageContaining("tạm khóa");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest req = new RegisterRequest("Test", "an@petcare.com", "0988999999", "Password123");
        when(userRepository.existsByEmailIgnoreCase("an@petcare.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email đã được sử dụng");
    }

    @Test
    void register_duplicatePhone_throwsDuplicateResourceException() {
        RegisterRequest req = new RegisterRequest("Test", "new@petcare.com", "0988000001", "Password123");
        when(userRepository.existsByEmailIgnoreCase("new@petcare.com")).thenReturn(false);
        when(userRepository.existsByPhone("0988000001")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Số điện thoại đã được sử dụng");
    }

    @Test
    void getProfile_disabledUser_throwsAccountDisabledException() {
        when(userRepository.findByEmailIgnoreCase("locked@petcare.com")).thenReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> authService.getProfile("locked@petcare.com"))
                .isInstanceOf(AccountDisabledException.class)
                .hasMessageContaining("tạm khóa");
    }
}
