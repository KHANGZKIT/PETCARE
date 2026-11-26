package com.petcare.security;

import com.petcare.entity.UserEntity;
import org.springframework.security.core.Authentication;

/**
 * Helper dùng chung để trích xuất email người dùng hiện tại từ Spring Security Authentication.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String extractEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof UserEntity user) {
            return user.getEmail();
        }
        return authentication.getName();
    }
}
