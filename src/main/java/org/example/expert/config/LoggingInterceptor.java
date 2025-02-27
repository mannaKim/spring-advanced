package org.example.expert.config;

import static org.example.expert.common.utils.ResponseUtil.sendErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            String message = "인증되지 않은 사용자입니다.";
            log.warn("{} - Method: {}, URL: {}, DateTime: {}", message, request.getMethod(), request.getRequestURI(), LocalDateTime.now());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, message);
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals(UserRole.ADMIN.name()));

        if (!isAdmin) {
            String message = "관리자 권한이 없습니다.";
            log.warn("{} - Method: {}, URL: {}, DateTime: {}", message, request.getMethod(), request.getRequestURI(), LocalDateTime.now());
            sendErrorResponse(response, HttpStatus.FORBIDDEN, message);
            return false;
        }

        log.info("{} admin 접근 - Method: {}, URL: {}", LocalDateTime.now(), request.getMethod(), request.getRequestURI());
        return true;
    }
}
