package com.example.itsm_api.controller;

import com.example.itsm_api.security.AuthorizationService;
import com.example.itsm_api.security.CustomUserPrincipal;
import com.example.itsm_api.service.SrvcRsponsService;
import com.example.itsm_api.vo.SrvcRsponsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Request (SR) REST API Controller
 * 
 * Provides endpoints for Service Request management with role-based access control.
 * 
 * Roles:
 * - R001: Manager (can view all SRs)
 * - R003: Handler (can view assigned SRs and perform actions)
 * - R005: Requester (can view own SRs)
 * - R002: Admin (full access)
 */
@RestController
@RequestMapping("/api/sr")
public class SrvcRsponsController {
    private static final Logger log = LoggerFactory.getLogger(SrvcRsponsController.class);

    @Autowired
    private SrvcRsponsService srvcRsponsService;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Create a new Service Request
     * 
     * POST /api/sr/create
     * 
     * Request body example:
     * {
     *   "title": "System access request",
     *   "description": "Need access to HR portal",
     *   "urgency": "2",
     *   "priority": "3"
     * }
     * 
     * @param vo The SR details
     * @return Created SR with generated ID
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createSr(@RequestBody SrvcRsponsVO vo,
                                      @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        try {
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            log.debug("Creating SR for user: {}, userTyCode: {}", user.getUsername(), user.getUserTyCode());
            
            // Set requester information from JWT
            vo.setRqesterId(user.getUsername());
            vo.setRqesterNm(user.getFirstName());
            vo.setRqesterEmail(user.getEmail());
            
            // Ensure creatId/updtId are populated: prefer existing VO, then X-User-Id header, then authenticated user
            String creatorId = vo.getCreatId();
            if (creatorId == null || creatorId.isEmpty()) {
                if (xUserId != null && !xUserId.isEmpty()) {
                    creatorId = xUserId;
                } else {
                    creatorId = user.getUsername();
                }
            }
            vo.setCreatId(creatorId);
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(creatorId);
            }
            
            srvcRsponsService.create(vo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Service Request created successfully");
            response.put("srNo", vo.getSrvcRsponsNo());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating SR: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get a Service Request by ID
     * 
     * GET /api/sr/{id}
     * 
     * Returns the full SR details including all related information.
     * Access is controlled by role-based authorization - users can only see:
     * - R005 (Requester): Their own requests
     * - R003 (Handler): Assigned requests
     * - R001 (Manager): All requests
     * - R002 (Admin): All requests
     * 
     * @param id The SR ID (e.g., "SR-202501-001")
     * @return SR details or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSrById(@PathVariable String id) {
        try {
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            SrvcRsponsVO searchVo = new SrvcRsponsVO();
            searchVo.setSrvcRsponsNo(id);
            searchVo.setUserTyCode(user.getUserTyCode().isEmpty() ? null : user.getUserTyCode().get(0));
            searchVo.setUserId(user.getUsername());
            
            log.debug("Getting SR {} for user: {}, userTyCode: {}", id, user.getUsername(), 
                      user.getUserTyCode().isEmpty() ? "NONE" : user.getUserTyCode().get(0));
            
            SrvcRsponsVO sr = srvcRsponsService.retrieve(searchVo);
            
            if (sr == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "error", "Service Request not found"));
            }
            
            return ResponseEntity.ok(sr);
        } catch (Exception e) {
            log.error("Error retrieving SR {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get list of Service Requests (paginated)
     * 
     * GET /api/sr/list
     * 
     * Returns a list of SRs based on the user's role:
     * - R005 (Requester): Can only see their own SRs
     * - R003 (Handler): Can see assigned SRs
     * - R001 (Manager): Can see all SRs
     * - R002 (Admin): Can see all SRs
     * 
     * @return List of SRs
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getList() {
        try {
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            
            SrvcRsponsVO searchVo = new SrvcRsponsVO();
            // Set role and user info for role-based filtering
            searchVo.setUserTyCode(user.getUserTyCode().isEmpty() ? null : user.getUserTyCode().get(0));
            searchVo.setUserId(user.getUsername());
            
            // Set default pagination values
            searchVo.setPageSize(100);
            searchVo.setStartRow(0);
            
            log.debug("Getting SR list for user: {}, userTyCode: {}", user.getUsername(), 
                      user.getUserTyCode().isEmpty() ? "NONE" : user.getUserTyCode().get(0));
            
            List<SrvcRsponsVO> list = srvcRsponsService.retrievePagingList(searchVo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", list.size());
            response.put("data", list);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving SR list: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Update SR request details
     * 
     * PUT /api/sr/{id}/request
     * 
     * Update the initial request information. Only available before SR is assigned.
     * 
     * @param id The SR ID
     * @param vo Updated SR details
     * @return Update result
     */
    @PutMapping("/{id}/request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateRequest(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateRequst(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "Updated successfully" : "No records updated"
            ));
        } catch (Exception e) {
            log.error("Error in updateReceive: ", e);
            // Detect SQL data truncation (MySQL) and return 400 with helpful info
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            String causeMsg = cause.getMessage() == null ? "" : cause.getMessage();
            if (cause.getClass().getName().endsWith("MysqlDataTruncation") || causeMsg.contains("Data too long for column")) {
                Map<String, Object> body = new HashMap<>();
                body.put("success", false);
                body.put("error", "Data too long for DB column");
                body.put("sqlError", causeMsg);
                // include the likely offending value to help client fix payload
                body.put("srvcRsponsClCode", vo.getSrvcRsponsClCode());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
            }

            StringBuilder trace = new StringBuilder();
            for (StackTraceElement ste : e.getStackTrace()) {
                trace.append(ste.toString()).append("\\n");
            }
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("error", e.getMessage());
            body.put("exception", e.getClass().getName());
            body.put("trace", trace.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    /**
     * Record first response to SR
     * 
     * PUT /api/sr/{id}/receive
     * 
     * Updates the SR status when initially received/acknowledged by handler.
     * 
     * @param id The SR ID
     * @param vo Response details
     * @return Update result
     */
    @PutMapping("/{id}/receive")
    @PreAuthorize("hasRole('HANDLER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateReceive(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateReceive(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "SR received and acknowledged" : "No records updated"
            ));
        } catch (Exception e) {
            log.error("Error in updateReceive: ", e);
            StringBuilder trace = new StringBuilder();
            for (StackTraceElement ste : e.getStackTrace()) {
                trace.append(ste.toString()).append("\\n");
            }
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("error", e.getMessage());
            body.put("exception", e.getClass().getName());
            body.put("trace", trace.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    /**
     * Record first response
     * 
     * PUT /api/sr/{id}/response-1st
     * 
     * Records the first response from handler to the requester.
     * 
     * @param id The SR ID
     * @param vo Response details
     * @return Update result
     */
    @PutMapping("/{id}/response-1st")
    @PreAuthorize("hasRole('HANDLER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateRspons1st(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateRspons1st(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "First response recorded" : "No records updated"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Record processing activity
     * 
     * PUT /api/sr/{id}/process
     * 
     * Records work/processing activities on the SR.
     * 
     * @param id The SR ID
     * @param vo Processing details
     * @return Update result
     */
    @PutMapping("/{id}/process")
    @PreAuthorize("hasRole('HANDLER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateProcess(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateProcess(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "Processing activity recorded" : "No records updated"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Update SR processing status
     * 
     * PUT /api/sr/{id}/sr-process
     * 
     * Updates the overall processing status of the SR.
     * 
     * @param id The SR ID
     * @param vo Processing status
     * @return Update result
     */
    @PutMapping("/{id}/sr-process")
    @PreAuthorize("hasRole('HANDLER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateSrProcess(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateSrProcess(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "SR processing status updated" : "No records updated"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Verify SR completion
     * 
     * PUT /api/sr/{id}/verify
     * 
     * Records verification/approval that the SR is completed.
     * Usually performed by manager or requester.
     * 
     * @param id The SR ID
     * @param vo Verification details
     * @return Update result
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<?> updateSrVerify(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateSrVerify(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "SR verified and completed" : "No records updated"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Finish/Close SR
     * 
     * PUT /api/sr/{id}/finish
     * 
     * Marks the SR as finished and closed.
     * 
     * @param id The SR ID
     * @param vo Completion details
     * @return Update result
     */
    @PutMapping("/{id}/finish")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateSrFinish(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateSrFinish(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "SR finished and closed" : "No records updated"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Record evaluation/feedback
     * 
     * PUT /api/sr/{id}/evaluate
     * 
     * Records satisfaction evaluation or feedback on the SR.
     * 
     * @param id The SR ID
     * @param vo Evaluation details
     * @return Update result
     */
    @PutMapping("/{id}/evaluate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateSrEv(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateSrEv(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "Evaluation recorded" : "No records updated"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Request re-evaluation/re-open SR
     * 
     * POST /api/sr/{id}/re-evaluate
     * 
     * Requests re-evaluation or reopens a closed SR.
     * 
     * @param id The SR ID
     * @param vo Re-evaluation request details
     * @return Update result
     */
    @PostMapping("/{id}/re-evaluate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateSrEvReRequest(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.updateSrEvReRequest(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "Re-evaluation requested" : "No records updated"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Re-request - Create a follow-up SR
     * 
     * POST /api/sr/{id}/re-request
     * 
     * Creates a follow-up/related service request.
     * 
     * @param id The original SR ID
     * @param vo Re-request details
     * @return Created SR with new ID
     */
    @PostMapping("/{id}/re-request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createSrReRequest(@PathVariable String id, @RequestBody SrvcRsponsVO vo) {
        try {
            vo.setSrvcRsponsNo(id);
            CustomUserPrincipal user = authorizationService.getCurrentUser();
            // set creat/updt id for re-request creation
            if (vo.getCreatId() == null || vo.getCreatId().isEmpty()) {
                vo.setCreatId(user.getUsername());
            }
            if (vo.getUpdtId() == null || vo.getUpdtId().isEmpty()) {
                vo.setUpdtId(user.getUsername());
            }
            int result = srvcRsponsService.createSrReRequest(vo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result > 0);
            response.put("message", result > 0 ? "Follow-up SR created" : "Failed to create follow-up SR");
            response.put("srNo", vo.getSrvcRsponsNo());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Delete/Cancel SR
     * 
     * DELETE /api/sr/{id}
     * 
     * Soft delete - marks SR as deleted without removing data.
     * Can only be done by authorized users.
     * 
     * @param id The SR ID
     * @return Delete result
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteSr(@PathVariable String id) {
        try {
            SrvcRsponsVO vo = new SrvcRsponsVO();
            vo.setSrvcRsponsNo(id);
            int result = srvcRsponsService.delete(vo);
            
            return ResponseEntity.ok(Map.of(
                    "success", result > 0,
                    "message", result > 0 ? "SR deleted successfully" : "No records deleted"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
