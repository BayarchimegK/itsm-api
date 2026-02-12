package com.example.itsm_api.security;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

/**
 * Service for SR (Service Request) role-based authorization
 * Enforces ITSM business rules based on user type codes (userTyCode)
 */
@Service
public class SrAuthorizationService {

    // User Type Codes
    public static final String ROLE_TEMP = "R000";      // Temporary user (limited access)
    public static final String ROLE_MANAGER = "R001";   // Manager/Administrator
    public static final String ROLE_CUSTOMER = "R002";  // Customer/End User (Requester)
    public static final String ROLE_CHARGER = "R003";   // Service Handler/Charger
    public static final String ROLE_CONSULTANT = "R004"; // Consultant
    public static final String ROLE_CUSTOM = "R005";    // Custom/Reserved role

    /**
     * Verify user can create SR (Customer or Manager)
     * R002 (Customer): Can create SR
     * R001 (Manager): Can create SR with full parameters via /manager endpoint
     */
    public void verifyCanCreateSr(String userTyCode) {
        if (userTyCode == null || (!userTyCode.equals(ROLE_CUSTOMER) && !userTyCode.equals(ROLE_MANAGER))) {
            throw new AccessDeniedException("User type " + userTyCode + " cannot create Service Requests");
        }
    }

    /**
     * Verify user can create SR as Manager (R001 only)
     * Manager endpoint requires R001 role
     */
    public void verifyCanCreateSrAsManager(String userTyCode) {
        if (userTyCode == null || !userTyCode.equals(ROLE_MANAGER)) {
            throw new AccessDeniedException("Only managers (R001) can use the /manager endpoint. Current role: " + userTyCode);
        }
    }

    /**
     * Verify user can receive/acknowledge SR (Handler R003 only)
     */
    public void verifyCanReceiveSr(String userTyCode) {
        if (userTyCode == null || !userTyCode.equals(ROLE_CHARGER)) {
            throw new AccessDeniedException("Only service handlers (R003) can receive SRs. Current role: " + userTyCode);
        }
    }

    /**
     * Verify user can process SR (Handler R003 only)
     */
    public void verifyCanProcessSr(String userTyCode) {
        if (userTyCode == null || !userTyCode.equals(ROLE_CHARGER)) {
            throw new AccessDeniedException("Only service handlers (R003) can process SRs. Current role: " + userTyCode);
        }
    }

    /**
     * Verify user can verify SR (Handler R003 only)
     */
    public void verifyCanVerifySr(String userTyCode) {
        if (userTyCode == null || !userTyCode.equals(ROLE_CHARGER)) {
            throw new AccessDeniedException("Only service handlers (R003) can verify SRs. Current role: " + userTyCode);
        }
    }

    /**
     * Verify user can finish SR (Handler R003 only)
     */
    public void verifyCanFinishSr(String userTyCode) {
        if (userTyCode == null || !userTyCode.equals(ROLE_CHARGER)) {
            throw new AccessDeniedException("Only service handlers (R003) can finish SRs. Current role: " + userTyCode);
        }
    }

    /**
     * Verify user can evaluate/re-request SR (Customer R002 only)
     */
    public void verifyCanEvaluateSr(String userTyCode) {
        if (userTyCode == null || !userTyCode.equals(ROLE_CUSTOMER)) {
            throw new AccessDeniedException("Only customers (R002) can evaluate SRs. Current role: " + userTyCode);
        }
    }

    /**
     * Verify user can view list of SRs
     * All authenticated users can view, but results are filtered by role
     */
    public void verifyCanViewSrList(String userTyCode) {
        if (userTyCode == null) {
            throw new AccessDeniedException("User must be authenticated to view SR lists");
        }
    }

    /**
     * Check if user is Manager
     */
    public boolean isManager(String userTyCode) {
        return ROLE_MANAGER.equals(userTyCode);
    }

    /**
     * Check if user is Customer
     */
    public boolean isCustomer(String userTyCode) {
        return ROLE_CUSTOMER.equals(userTyCode);
    }

    /**
     * Check if user is Handler/Charger
     */
    public boolean isHandler(String userTyCode) {
        return ROLE_CHARGER.equals(userTyCode);
    }

    /**
     * Check if user is Custom role (R005)
     */
    public boolean isCustomRole(String userTyCode) {
        return ROLE_CUSTOM.equals(userTyCode);
    }

    /**
     * Check if user has admin privileges (Manager only)
     */
    public boolean hasAdminPrivileges(String userTyCode) {
        return ROLE_MANAGER.equals(userTyCode);
    }

    /**
     * Extract user type code from request context
     * Can be called from controller/interceptor to get current user type
     */
    public String getCurrentUserType(String userTyCode) {
        return userTyCode != null ? userTyCode : "UNKNOWN";
    }
}
