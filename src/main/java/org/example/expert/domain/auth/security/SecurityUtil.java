package org.example.expert.domain.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

public class SecurityUtil {

    public static CustomUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("사용자 인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new AuthenticationCredentialsNotFoundException("잘못된 인증 정보입니다.");
        }

        return (CustomUserDetails) principal;
    }
}
