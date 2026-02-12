package com.example.itsm_api.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AOP Aspect for SR (Service Request) authorization enforcement
 * Intercepts API calls and verifies user has required permissions
 */
@Aspect
@Component
public class SrAuthorizationAspect {

    @Autowired
    private SrAuthorizationService authorizationService;

    /**
     * Extract user type code from request header
     * Header: X-User-Type-Code or from JWT token
     */
    private String extractUserTypeCode() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new AccessDeniedException("Unable to get request context");
        }
        
        HttpServletRequest request = attrs.getRequest();
        String userTyCode = request.getHeader("X-User-Type-Code");
        
        if (userTyCode == null || userTyCode.isEmpty()) {
            // Fallback to JWT claim if header not present
            userTyCode = (String) request.getAttribute("userTyCode");
        }
        
        if (userTyCode == null || userTyCode.isEmpty()) {
            throw new AccessDeniedException("User type code not found in request");
        }
        
        return userTyCode;
    }

    /**
     * Authorize SR creation (POST /api/v1/sr)
     * Allow: R002 (Customer), R001 (Manager)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.create(..))")
    public void authorizeCreateSr(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanCreateSr(userTyCode);
    }

    /**
     * Authorize SR creation as Manager (POST /api/v1/sr/manager)
     * Allow: R001 (Manager only)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.createForManager(..))")
    public void authorizeCreateSrAsManager(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanCreateSrAsManager(userTyCode);
    }

    /**
     * Authorize SR receive (PUT /api/v1/sr/{id}/receive)
     * Allow: R003 (Handler)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.updateReceive(..))")
    public void authorizeReceiveSr(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanReceiveSr(userTyCode);
    }

    /**
     * Authorize SR process (PUT /api/v1/sr/{id}/process)
     * Allow: R003 (Handler)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.updateProcess(..))")
    public void authorizeProcessSr(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanProcessSr(userTyCode);
    }

    /**
     * Authorize SR verify (PUT /api/v1/sr/{id}/verify)
     * Allow: R003 (Handler)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.updateVerify(..))")
    public void authorizeVerifySr(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanVerifySr(userTyCode);
    }

    /**
     * Authorize SR finish (PUT /api/v1/sr/{id}/finish)
     * Allow: R003 (Handler)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.updateFinish(..))")
    public void authorizeFinishSr(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanFinishSr(userTyCode);
    }

    /**
     * Authorize SR evaluation (PUT /api/v1/sr/{id}/evaluation)
     * Allow: R002 (Customer)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.updateEvaluation(..))")
    public void authorizeEvaluateSr(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanEvaluateSr(userTyCode);
    }

    /**
     * Authorize SR re-request (POST /api/v1/sr/{id}/re-request)
     * Allow: R002 (Customer)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.reRequest(..))")
    public void authorizeReRequest(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanEvaluateSr(userTyCode);
    }

    /**
     * Authorize SR list view (GET /api/v1/sr)
     * Allow: All authenticated users (but results filtered by role)
     */
    @Before("execution(* com.example.itsm_api.controller.SrvcRsponsApiController.list(..))")
    public void authorizeListSr(JoinPoint joinPoint) {
        String userTyCode = extractUserTypeCode();
        authorizationService.verifyCanViewSrList(userTyCode);
    }
}
