package com.example.itsm_api.mapper;

import com.example.itsm_api.vo.SrvcRsponsVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * MyBatis Mapper Interface for Service Request (SR) Operations
 * Handles all CRUD operations for TB_SRVC_RSPONS table
 * 
 * SQL Queries are defined in: src/main/resources/mapper/SrvcRsponsMapper.xml
 */
@Mapper
public interface SrvcRsponsMapper {

    // ==================== CREATE OPERATIONS ====================
    
    /**
     * Create new Service Request
     * Auto-generates SR number with format: SR-YYMM-NNN
     * @param vo Service Request VO with all required fields
     * @return Number of rows inserted (1 if successful)
     */
    int create(SrvcRsponsVO vo);

    /**
     * Create Service Request from re-request (evaluation stage)
     * Copies original SR data and creates new SR with RE_SRVC_RSPONS_NO reference
     * @param vo Service Request VO with re-request data
     * @return Number of rows inserted
     */
    int createSrReRequest(SrvcRsponsVO vo);

    
    // ==================== READ OPERATIONS ====================
    
    /**
     * Retrieve single Service Request by SR number or related field
     * @param vo VO with srvcRsponsNo, fnctImprvmNo, wdtbCnfirmNo, or infraOpertNo
     * @return Service Request details with all joined data (users, codes, etc.)
     */
    SrvcRsponsVO retrieve(SrvcRsponsVO vo);

    /**
     * Retrieve paginated list of all Service Requests
     * Applies role-based filtering:
     * - R001 (Manager): sees all
     * - R003 (Handler): sees assigned services via TB_SYS_CHARGER
     * - R005 (Specialist): sees own requests only
     * @param vo Paging parameters and filter criteria
     * @return Paginated list of SRs
     */
    List<SrvcRsponsVO> retrievePagingList(SrvcRsponsVO vo);

    /**
     * Get count for paginated list
     * @param vo Filter criteria
     * @return Total count
     */
    int retrievePagingListCnt(SrvcRsponsVO vo);

    /**
     * Retrieve REQUEST stage SRs (not yet received)
     * @param vo Paging and filter parameters
     * @return List of SRs in REQUEST stage
     */
    List<SrvcRsponsVO> retrieveSrReqList(SrvcRsponsVO vo);
    int retrieveSrReqPagingListCnt(SrvcRsponsVO vo);

    /**
     * Retrieve RECEIVE stage SRs
     * @param vo Paging and filter parameters
     * @return List of SRs in RECEIVE stage
     */
    List<SrvcRsponsVO> retrieveSrRcvList(SrvcRsponsVO vo);
    int retrieveSrRcvPagingListCnt(SrvcRsponsVO vo);

    /**
     * Retrieve PROCESS stage SRs (being worked on)
     * Handler (R003) sees only assigned services
     * @param vo Paging and filter parameters
     * @return List of SRs in PROCESS stage by priority
     */
    List<SrvcRsponsVO> retrieveSrProcList(SrvcRsponsVO vo);
    int retrieveSrProcPagingListCnt(SrvcRsponsVO vo);

    /**
     * Retrieve VERIFY stage SRs (awaiting verification)
     * Only returns SRs marked for verification (VERIFY_YN = 'Y')
     * @param vo Paging and filter parameters
     * @return List of SRs awaiting verification
     */
    List<SrvcRsponsVO> retrieveSrVrList(SrvcRsponsVO vo);
    int retrieveSrVrPagingListCnt(SrvcRsponsVO vo);

    /**
     * Retrieve FINISH stage SRs (completed, awaiting evaluation)
     * @param vo Paging and filter parameters
     * @return List of finished SRs
     */
    List<SrvcRsponsVO> retrieveSrFnList(SrvcRsponsVO vo);
    int retrieveSrFnPagingListCnt(SrvcRsponsVO vo);

    /**
     * Retrieve EVALUATION stage SRs (customer evaluation)
     * @param vo Paging and filter parameters
     * @return List of SRs in evaluation
     */
    List<SrvcRsponsVO> retrieveSrEvList(SrvcRsponsVO vo);
    int retrieveSrEvPagingListCnt(SrvcRsponsVO vo);

    /**
     * Retrieve SRs with Withdrawal Confirmation info
     * @param vo Paging and filter parameters
     * @return List of SRs with WDTB data
     */
    List<SrvcRsponsVO> retrieveWdtbPagingList(SrvcRsponsVO vo);

    /**
     * Retrieve SRs with Function Improvement records
     * @param vo Paging and filter parameters
     * @return List of SRs with FNCT_IMPRVM_NO
     */
    List<SrvcRsponsVO> retrievefnctImprvmPagingList(SrvcRsponsVO vo);

    /**
     * Retrieve SRs with Infrastructure Operation records
     * @param vo Paging and filter parameters
     * @return List of SRs marked for infrastructure operation
     */
    List<SrvcRsponsVO> retrieveInfraOpertPagingList(SrvcRsponsVO vo);

    /**
     * Retrieve basic list (limited to 9 records)
     * @param vo Filter parameters
     * @return Limited list of SRs
     */
    List<SrvcRsponsVO> retrieveList(SrvcRsponsVO vo);

    /**
     * Retrieve all SRs (no pagination)
     * @param vo Filter parameters
     * @return All matching SRs
     */
    List<SrvcRsponsVO> retrieveAllList(SrvcRsponsVO vo);

    /**
     * Retrieve all SRs with WDTB confirmation
     * @param vo Filter parameters
     * @return SRs with WDTB_CNFIRM_NO
     */
    List<SrvcRsponsVO> retrieveAllwdtbList(SrvcRsponsVO vo);

    /**
     * Retrieve unique requester names for autocomplete
     * @param vo Contains rqesterNm search term
     * @return List of distinct requester names matching search
     */
    List<SrvcRsponsVO> retrieveRqesterNmList(SrvcRsponsVO vo);

    /**
     * Retrieve SRs by handler assignment
     * Searches by charger ID or assigned service codes
     * @param vo Contains chargerId and srvcRsponsNo search criteria
     * @return List of SRs for display dropdown (max 5)
     */
    List<SrvcRsponsVO> retrieveSrvcRsponsNoList(SrvcRsponsVO vo);

    /**
     * Retrieve unique initial requester names for autocomplete
     * @param vo Contains rqester1stNm search term
     * @return List of distinct initial requester names
     */
    List<SrvcRsponsVO> retrieveRqester1stNmList(SrvcRsponsVO vo);

    
    // ==================== UPDATE OPERATIONS ====================
    
    /**
     * Update REQUEST stage fields
     * CRITICAL: Only callable before RSPONS_1ST_DT is set (request edit lockdown)
     * @param vo SR with updated request fields
     * @return Number of rows updated
     */
    int updateRequst(SrvcRsponsVO vo);

    /**
     * Update to RECEIVE stage
     * Sets RSPONS_1ST_DT (first response date)
     * Triggers request lockdown for customers
     * @param vo SR with receive stage data
     * @return Number of rows updated
     */
    int updateReceive(SrvcRsponsVO vo);

    /**
     * Update first response details
     * Sets handler info and process parameters
     * @param vo SR with response details
     * @return Number of rows updated
     */
    int updateRspons1st(SrvcRsponsVO vo);

    /**
     * Update PROCESS stage details
     * Captures work performed, data changes, installation info
     * @param vo SR with process details
     * @return Number of rows updated
     */
    int updateProcess(SrvcRsponsVO vo);

    /**
     * Update SR process details (continued work)
     * @param vo SR with additional process details
     * @return Number of rows updated
     */
    int updateSrProcess(SrvcRsponsVO vo);

    /**
     * Update VERIFY stage
     * Records verification decision and details
     * @param vo SR with verify data
     * @return Number of rows updated
     */
    int updateSrVerify(SrvcRsponsVO vo);

    /**
     * Update FINISH stage
     * Records work completion details
     * @param vo SR with finish data
     * @return Number of rows updated
     */
    int updateSrFinish(SrvcRsponsVO vo);

    /**
     * Update EVALUATION stage
     * Records customer evaluation
     * @param vo SR with evaluation data
     * @return Number of rows updated
     */
    int updateSrEv(SrvcRsponsVO vo);

    /**
     * Update re-request from evaluation
     * Links to original SR (RE_SRVC_RSPONS_NO)
     * @param vo Original SR VO with reSrvcRsponsNo
     * @return Number of rows updated
     */
    int updateSrEvReRequest(SrvcRsponsVO vo);

    /**
     * Update confirmation handler
     * @param vo SR with cnfrmrId
     * @return Number of rows updated
     */
    int updateCnfrmr(SrvcRsponsVO vo);

    /**
     * Update withdrawal confirmation reference
     * @param vo SR with wdtbCnfirmNo
     * @return Number of rows updated
     */
    int updateWdtbCnfirm(SrvcRsponsVO vo);

    /**
     * Update infrastructure operation reference
     * @param vo SR with infraOpertNo
     * @return Number of rows updated
     */
    int updateInfraOpert(SrvcRsponsVO vo);

    /**
     * Mark SMS notification as sent
     * @param vo SR with srvcRsponsNo
     * @return Number of rows updated
     */
    int updateSmsChk(SrvcRsponsVO vo);

    /**
     * Clear withdrawal confirmation reference
     * @param vo SR with srvcRsponsNo
     * @return Number of rows updated
     */
    int deleteWdtbCnfirm(SrvcRsponsVO vo);

    /**
     * Clear infrastructure operation reference
     * @param vo SR with srvcRsponsNo
     * @return Number of rows updated
     */
    int deleteInfraOpert(SrvcRsponsVO vo);

    /**
     * Assign handler/charger to Service Request
     * Updates only CHARGER_ID, CHARGER_NM, and timestamp fields
     * @param vo SR with srvcRsponsNo, chargerId, chargerUserNm
     * @return Number of rows updated
     */
    int assignHandler(SrvcRsponsVO vo);

    /**
     * Generic update for fields set in VO
     * Uses dynamic SQL with <if> tags to update only provided fields
     * @param vo SR with fields to update
     * @return Number of rows updated
     */
    int update(SrvcRsponsVO vo);

    
    // ==================== DELETE OPERATIONS ====================
    
    /**
     * Soft delete Service Request
     * Sets DELETE_YN = 'Y' instead of physical delete
     * @param vo SR with srvcRsponsNo
     * @return Number of rows updated
     */
    int delete(SrvcRsponsVO vo);
}
