package org.example.expert.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class ApiLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingAspect.class);
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    public ApiLoggingAspect(HttpServletRequest request, ObjectMapper objectMapper) {
        this.request = request;
        this.objectMapper = objectMapper;
    }

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) " +
            "|| execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    private void cut(){}

    @Around("cut()")
    public Object logAdminApi(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Method method = getMethod(proceedingJoinPoint);
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();
        Long userId = (Long) request.getAttribute("userId");

        log.info("======= [Admin API Request] =======");
        log.info("Request User ID: {}", userId);
        log.info("Request Time: {}", LocalDateTime.now());
        log.info("Request URL: {}", requestUrl);
        log.info("Request Method: {}", requestMethod);
        log.info("Method Name: {}", method.getName());

        Object[] args = proceedingJoinPoint.getArgs();
        if (args.length == 0) {
            log.info("Request Body: no parameter");
        } else {
            Arrays.stream(args).forEach(arg -> {
                log.info("Request Parameter Type: {}", arg.getClass().getSimpleName());
                log.info("Request Parameter: {}", convertToJson(arg));
            });
        }

        // proceed()를 호출하여 실제 메서드 실행
        Object returnObj = proceedingJoinPoint.proceed();

        log.info("======= [Admin API Response] =======");
        if (returnObj != null) {
            log.info("Response Type: {}", returnObj.getClass().getSimpleName());

            if (returnObj instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) returnObj;
                log.info("Response Status: {}", responseEntity.getStatusCode());
                log.info("Response Body: {}", convertToJson(responseEntity.getBody()));
            } else {
                log.info("Response Value: {}", convertToJson(returnObj));
            }
        } else {
            log.info("Response Value: null (void method)");
        }

        return returnObj;
    }

    private Method getMethod(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        return signature.getMethod();
    }

    private String convertToJson(Object object) {
        if (object == null) return "null";
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Json 형식으로 변환 실패: {}", e.getMessage());
            return "Json 형식으로 변환 실패";
        }
    }
}