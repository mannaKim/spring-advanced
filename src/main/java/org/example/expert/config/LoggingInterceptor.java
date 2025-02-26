package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        UserRole userRole = UserRole.valueOf((String) request.getAttribute("userRole"));

        if (!UserRole.ADMIN.equals(userRole)) {
            log.warn("{} 관리자 권한 없음 - Method: {}, URL: {}", LocalDateTime.now(), request.getMethod(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
            return false;
        }

        log.info("{} admin 접근 - Method: {}, URL: {}", LocalDateTime.now(), request.getMethod(), request.getRequestURI());
        return true;
    }
}
