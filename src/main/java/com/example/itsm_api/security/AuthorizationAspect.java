package com.example.itsm_api.security;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Aspect for handling custom authorization annotations
 */
@Aspect
@Component
public class AuthorizationAspect {

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Check @RequireUserTyCode authorization
     */
    @Before("@annotation(requireUserTyCode)")
    public void checkUserTyCode(JoinPoint joinPoint, RequireUserTyCode requireUserTyCode) {
        if (!authorizationService.hasUserTyCode(requireUserTyCode.value())) {
            throw new AccessDeniedException(
                    "Access denied. Required user type code: " + Arrays.toString(requireUserTyCode.value()));
        }
    }

    /**
     * Check @RequireUserSttusCode authorization
     */
    @Before("@annotation(requireUserSttusCode)")
    public void checkUserSttusCode(JoinPoint joinPoint, RequireUserSttusCode requireUserSttusCode) {
        if (!authorizationService.hasUserSttusCode(requireUserSttusCode.value())) {
            throw new AccessDeniedException(
                    "Access denied. Required user status code: " + Arrays.toString(requireUserSttusCode.value()));
        }
    }
}
