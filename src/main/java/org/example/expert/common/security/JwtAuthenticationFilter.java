package org.example.expert.common.security;

import static org.example.expert.common.utils.ResponseUtil.sendErrorResponse;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.NonNull;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = jwtUtil.substringToken(authorizationHeader);
            try {
                // JWT 유효성 검사와 claims 추출
                Claims claims = jwtUtil.extractClaims(jwt);

                // SecurityContext에 인증 정보 설정
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    setAuthentication(claims);
                }
            } catch (SecurityException | MalformedJwtException e) {
                String message = "유효하지 않은 JWT 토큰입니다.";
                log.error(message, e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, message);
                return;
            } catch (ExpiredJwtException e) {
                String message = "만료된 JWT 토큰입니다.";
                log.error(message, e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, message);
                return;
            } catch (UnsupportedJwtException e) {
                String message = "Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.";
                log.error(message, e);
                sendErrorResponse(response, HttpStatus.BAD_REQUEST, message);
                return;
            } catch (Exception e) {
                String message = "Internal server error";
                log.error(message, e);
                sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, message);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(Claims claims) {
        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));

        AuthUser authUser = new AuthUser(userId, email, userRole);
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
