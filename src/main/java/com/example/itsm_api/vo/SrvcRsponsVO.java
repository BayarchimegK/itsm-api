package com.example.itsm_api.vo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SrvcRsponsVO {
    // Security context properties
    private String userTyCode;
    private String userId;

    // Pagination properties
    private Integer pageIndex = 1;
    private Integer recordCountPerPage = 15;
    private Integer firstIndex = 0;

    // Audit fields
    private Date creatDt;
    private String creatId;
    private String creatUserNm;
    private Date updtDt;
    private String updtId;
    private String updtUserNm;
    private Date modifyDt;
    private String modifyUserId;

    // Request content
    private String rqestCn;

    // Example domain fields (kept broad to match prior uses)
    private Date rspons1stDt;
    private String rspons1stDtDateDisplay;
    private String rspons1stDtTimeDisplay;
    private String processMt;
    private String changeDfflyCode;
    private String changeDfflyCodeNm;
    private String srvcRsponsClCode;
    private String srvcRsponsClCodeNm;
    private String processStdrCode;
    private String processStdrCodeNm;
    private String processTerm;
    private String srvcProcessDtls;
    private String etc;
    private String srvcRsponsBasisCode;
    private String srvcRsponsBasisCodeNm;
    private String rsponsAtchmnflId;
    private Date processDt;
    private String processDtDateDisplay;
    private String processDtTimeDisplay;
    private String dataUpdtYn;
    private String progrmUpdtYn;
    private String stopInstlYn;
    private String noneStopInstlYn;
    private String instlYn;
    private String infraOpertYn;
    private String chargerId;
    private String chargerUserNm;
    private String cnfrmrId;
    private String cnfrmrUserNm;
    private String orderLevel;
    private String priorLevel;
    private String smsChk;
    private String excludeprocessYn;
    private String requstAtchmnflAt;
    private String fnctImprvmNo;
    private String wdtbCnfirmNo;
    private Date srvcWdtbDt;
    private String srvcWdtbDtDateDisplay;
    private String srvcWdtbDtTimeDisplay;
    private String infraOpertNo;
    private String reSrvcRsponsNo;
    private Date reRequestDt;
    private String reRequestDtDateDisplay;
    private String reRequestDtTimeDisplay;
    private String verifyYn;
    private Date verifyDt;
    private String verifyDtDateDisplay;
    private String verifyDtTimeDisplay;
    private Date finishDt;
    private String finishDtDateDisplay;
    private String finishDtTimeDisplay;
    private String verifyId;
    private String verifyUserNm;
    private String srvcVerifyDtls;
    private String srvcFinDtls;
    private String refIds;
    private String srcRqesterId;
    private String srcRqesterNm;
    private String reSrvcRsponsSj;
    private String reSrvcRsponsCn;
    private String trgetSrvcCode;
    private String trgetSrvcCodeNm;
    private String trgetSrvcDetailCode;
    private String trgetSrvcCodeSubNm1;
    private String trgetSrvcCodeSubNm2;
    private String trgetSrvcCodeSubNm3;
    private String srvcRsponsNo;
    private Date requstDt;
    private String requstDtDateDisplay;
    private String requstDtTimeDisplay;
    private String rqester1stNm;
    private String rqester1stPsitn;
    private String rqester1stCttpc;
    private String rqester1stEmail;
    private String rqesterId;
    private String rqesterNm;
    private String rqesterCttpc;
    private String rqesterEmail;
    private String rqesterDept;
    private String srvcRsponsSj;
    private String srvcRsponsCn;
    private String requstAtchmnflId;
    private String rqesterPsitn;
    private String finishId;
    private String finishUserNm;

    // --- Getters / Setters ---
    public String getUserTyCode() { return userTyCode; }
    public void setUserTyCode(String userTyCode) { this.userTyCode = userTyCode; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Integer getPageIndex() { return pageIndex; }
    public void setPageIndex(Integer pageIndex) { this.pageIndex = pageIndex; }
    public Integer getRecordCountPerPage() { return recordCountPerPage; }
    public void setRecordCountPerPage(Integer recordCountPerPage) { this.recordCountPerPage = recordCountPerPage; }
    public Integer getFirstIndex() { return firstIndex; }
    public void setFirstIndex(Integer firstIndex) { this.firstIndex = firstIndex; }

    // compatibility aliases used by older controller code
    public void setPageSize(int size) { this.setRecordCountPerPage(size); }
    public void setStartRow(int start) { this.setFirstIndex(start); }

    public Date getCreatDt() { return creatDt; }
    public void setCreatDt(Date creatDt) { this.creatDt = creatDt; }
    public String getCreatId() { return creatId; }
    public void setCreatId(String creatId) { this.creatId = creatId; }
    public String getCreatUserNm() { return creatUserNm; }
    public void setCreatUserNm(String creatUserNm) { this.creatUserNm = creatUserNm; }

    public Date getUpdtDt() { return updtDt; }
    public void setUpdtDt(Date updtDt) { this.updtDt = updtDt; }
    public String getUpdtId() { return updtId; }
    public void setUpdtId(String updtId) { this.updtId = updtId; }
    public String getUpdtUserNm() { return updtUserNm; }
    public void setUpdtUserNm(String updtUserNm) { this.updtUserNm = updtUserNm; }

    public Date getModifyDt() { return modifyDt; }
    public void setModifyDt(Date modifyDt) { this.modifyDt = modifyDt; }
    public String getModifyUserId() { return modifyUserId; }
    public void setModifyUserId(String modifyUserId) { this.modifyUserId = modifyUserId; }

    public String getRqestCn() { return rqestCn; }
    public void setRqestCn(String rqestCn) { this.rqestCn = rqestCn; }

    public Date getRspons1stDt() { return rspons1stDt; }
    public void setRspons1stDt(Date rspons1stDt) { this.rspons1stDt = rspons1stDt; }

    public String getProcessMt() { return processMt; }
    public void setProcessMt(String processMt) { this.processMt = processMt; }

    public String getChangeDfflyCode() { return changeDfflyCode; }
    public void setChangeDfflyCode(String changeDfflyCode) { this.changeDfflyCode = changeDfflyCode; }
    public String getChangeDfflyCodeNm() { return changeDfflyCodeNm; }
    public void setChangeDfflyCodeNm(String changeDfflyCodeNm) { this.changeDfflyCodeNm = changeDfflyCodeNm; }

    public String getSrvcRsponsClCode() { return srvcRsponsClCode; }
    public void setSrvcRsponsClCode(String srvcRsponsClCode) { this.srvcRsponsClCode = srvcRsponsClCode; }
    public String getSrvcRsponsClCodeNm() { return srvcRsponsClCodeNm; }
    public void setSrvcRsponsClCodeNm(String srvcRsponsClCodeNm) { this.srvcRsponsClCodeNm = srvcRsponsClCodeNm; }

    public String getProcessStdrCode() { return processStdrCode; }
    public void setProcessStdrCode(String processStdrCode) { this.processStdrCode = processStdrCode; }
    public String getProcessStdrCodeNm() { return processStdrCodeNm; }
    public void setProcessStdrCodeNm(String processStdrCodeNm) { this.processStdrCodeNm = processStdrCodeNm; }

    public String getProcessTerm() { return processTerm; }
    public void setProcessTerm(String processTerm) { this.processTerm = processTerm; }

    public String getSrvcProcessDtls() { return srvcProcessDtls; }
    public void setSrvcProcessDtls(String srvcProcessDtls) { this.srvcProcessDtls = srvcProcessDtls; }
    public String getEtc() { return etc; }
    public void setEtc(String etc) { this.etc = etc; }

    public String getSrvcRsponsBasisCode() { return srvcRsponsBasisCode; }
    public void setSrvcRsponsBasisCode(String srvcRsponsBasisCode) { this.srvcRsponsBasisCode = srvcRsponsBasisCode; }
    public String getSrvcRsponsBasisCodeNm() { return srvcRsponsBasisCodeNm; }
    public void setSrvcRsponsBasisCodeNm(String srvcRsponsBasisCodeNm) { this.srvcRsponsBasisCodeNm = srvcRsponsBasisCodeNm; }

    public String getRsponsAtchmnflId() { return rsponsAtchmnflId; }
    public void setRsponsAtchmnflId(String rsponsAtchmnflId) { this.rsponsAtchmnflId = rsponsAtchmnflId; }

    public Date getProcessDt() { return processDt; }
    public void setProcessDt(Date processDt) { this.processDt = processDt; }

    public String getDataUpdtYn() { return dataUpdtYn; }
    public void setDataUpdtYn(String dataUpdtYn) { this.dataUpdtYn = dataUpdtYn; }
    public String getProgrmUpdtYn() { return progrmUpdtYn; }
    public void setProgrmUpdtYn(String progrmUpdtYn) { this.progrmUpdtYn = progrmUpdtYn; }

    public String getStopInstlYn() { return stopInstlYn; }
    public void setStopInstlYn(String stopInstlYn) { this.stopInstlYn = stopInstlYn; }
    public String getNoneStopInstlYn() { return noneStopInstlYn; }
    public void setNoneStopInstlYn(String noneStopInstlYn) { this.noneStopInstlYn = noneStopInstlYn; }
    public String getInstlYn() { return instlYn; }
    public void setInstlYn(String instlYn) { this.instlYn = instlYn; }
    public String getInfraOpertYn() { return infraOpertYn; }
    public void setInfraOpertYn(String infraOpertYn) { this.infraOpertYn = infraOpertYn; }

    public String getChargerId() { return chargerId; }
    public void setChargerId(String chargerId) { this.chargerId = chargerId; }
    public String getChargerUserNm() { return chargerUserNm; }
    public void setChargerUserNm(String chargerUserNm) { this.chargerUserNm = chargerUserNm; }
    public String getCnfrmrId() { return cnfrmrId; }
    public void setCnfrmrId(String cnfrmrId) { this.cnfrmrId = cnfrmrId; }
    public String getCnfrmrUserNm() { return cnfrmrUserNm; }
    public void setCnfrmrUserNm(String cnfrmrUserNm) { this.cnfrmrUserNm = cnfrmrUserNm; }

    public String getOrderLevel() { return orderLevel; }
    public void setOrderLevel(String orderLevel) { this.orderLevel = orderLevel; }
    public String getPriorLevel() { return priorLevel; }
    public void setPriorLevel(String priorLevel) { this.priorLevel = priorLevel; }

    public String getSmsChk() { return smsChk; }
    public void setSmsChk(String smsChk) { this.smsChk = smsChk; }
    public String getExcludeprocessYn() { return excludeprocessYn; }
    public void setExcludeprocessYn(String excludeprocessYn) { this.excludeprocessYn = excludeprocessYn; }

    public String getRequstAtchmnflAt() { return requstAtchmnflAt; }
    public void setRequstAtchmnflAt(String requstAtchmnflAt) { this.requstAtchmnflAt = requstAtchmnflAt; }
    public String getFnctImprvmNo() { return fnctImprvmNo; }
    public void setFnctImprvmNo(String fnctImprvmNo) { this.fnctImprvmNo = fnctImprvmNo; }
    public String getWdtbCnfirmNo() { return wdtbCnfirmNo; }
    public void setWdtbCnfirmNo(String wdtbCnfirmNo) { this.wdtbCnfirmNo = wdtbCnfirmNo; }

    public Date getSrvcWdtbDt() { return srvcWdtbDt; }
    public void setSrvcWdtbDt(Date srvcWdtbDt) { this.srvcWdtbDt = srvcWdtbDt; }

    public String getInfraOpertNo() { return infraOpertNo; }
    public void setInfraOpertNo(String infraOpertNo) { this.infraOpertNo = infraOpertNo; }

    public String getReSrvcRsponsNo() { return reSrvcRsponsNo; }
    public void setReSrvcRsponsNo(String reSrvcRsponsNo) { this.reSrvcRsponsNo = reSrvcRsponsNo; }

    public Date getReRequestDt() { return reRequestDt; }
    public void setReRequestDt(Date reRequestDt) { this.reRequestDt = reRequestDt; }

    public String getVerifyYn() { return verifyYn; }
    public void setVerifyYn(String verifyYn) { this.verifyYn = verifyYn; }
    public Date getVerifyDt() { return verifyDt; }
    public void setVerifyDt(Date verifyDt) { this.verifyDt = verifyDt; }

    public Date getFinishDt() { return finishDt; }
    public void setFinishDt(Date finishDt) { this.finishDt = finishDt; }

    public String getVerifyId() { return verifyId; }
    public void setVerifyId(String verifyId) { this.verifyId = verifyId; }
    public String getVerifyUserNm() { return verifyUserNm; }
    public void setVerifyUserNm(String verifyUserNm) { this.verifyUserNm = verifyUserNm; }

    public String getSrvcVerifyDtls() { return srvcVerifyDtls; }
    public void setSrvcVerifyDtls(String srvcVerifyDtls) { this.srvcVerifyDtls = srvcVerifyDtls; }
    public String getSrvcFinDtls() { return srvcFinDtls; }
    public void setSrvcFinDtls(String srvcFinDtls) { this.srvcFinDtls = srvcFinDtls; }

    public String getRefIds() { return refIds; }
    public void setRefIds(String refIds) { this.refIds = refIds; }

    public String getSrcRqesterId() { return srcRqesterId; }
    public void setSrcRqesterId(String srcRqesterId) { this.srcRqesterId = srcRqesterId; }
    public String getSrcRqesterNm() { return srcRqesterNm; }
    public void setSrcRqesterNm(String srcRqesterNm) { this.srcRqesterNm = srcRqesterNm; }

    public String getReSrvcRsponsSj() { return reSrvcRsponsSj; }
    public void setReSrvcRsponsSj(String reSrvcRsponsSj) { this.reSrvcRsponsSj = reSrvcRsponsSj; }
    public String getReSrvcRsponsCn() { return reSrvcRsponsCn; }
    public void setReSrvcRsponsCn(String reSrvcRsponsCn) { this.reSrvcRsponsCn = reSrvcRsponsCn; }

    public String getTrgetSrvcCode() { return trgetSrvcCode; }
    public void setTrgetSrvcCode(String trgetSrvcCode) { this.trgetSrvcCode = trgetSrvcCode; }
    public String getTrgetSrvcCodeNm() { return trgetSrvcCodeNm; }
    public void setTrgetSrvcCodeNm(String trgetSrvcCodeNm) { this.trgetSrvcCodeNm = trgetSrvcCodeNm; }
    public String getTrgetSrvcDetailCode() { return trgetSrvcDetailCode; }
    public void setTrgetSrvcDetailCode(String trgetSrvcDetailCode) { this.trgetSrvcDetailCode = trgetSrvcDetailCode; }

    public String getTrgetSrvcCodeSubNm1() { return trgetSrvcCodeSubNm1; }
    public void setTrgetSrvcCodeSubNm1(String trgetSrvcCodeSubNm1) { this.trgetSrvcCodeSubNm1 = trgetSrvcCodeSubNm1; }
    public String getTrgetSrvcCodeSubNm2() { return trgetSrvcCodeSubNm2; }
    public void setTrgetSrvcCodeSubNm2(String trgetSrvcCodeSubNm2) { this.trgetSrvcCodeSubNm2 = trgetSrvcCodeSubNm2; }
    public String getTrgetSrvcCodeSubNm3() { return trgetSrvcCodeSubNm3; }
    public void setTrgetSrvcCodeSubNm3(String trgetSrvcCodeSubNm3) { this.trgetSrvcCodeSubNm3 = trgetSrvcCodeSubNm3; }

    public String getSrvcRsponsNo() { return srvcRsponsNo; }
    public void setSrvcRsponsNo(String srvcRsponsNo) { this.srvcRsponsNo = srvcRsponsNo; }

    public Date getRequstDt() { return requstDt; }
    public void setRequstDt(Date requstDt) { this.requstDt = requstDt; }

    public String getRqester1stNm() { return rqester1stNm; }
    public void setRqester1stNm(String rqester1stNm) { this.rqester1stNm = rqester1stNm; }
    public String getRqester1stPsitn() { return rqester1stPsitn; }
    public void setRqester1stPsitn(String rqester1stPsitn) { this.rqester1stPsitn = rqester1stPsitn; }
    public String getRqester1stCttpc() { return rqester1stCttpc; }
    public void setRqester1stCttpc(String rqester1stCttpc) { this.rqester1stCttpc = rqester1stCttpc; }
    public String getRqester1stEmail() { return rqester1stEmail; }
    public void setRqester1stEmail(String rqester1stEmail) { this.rqester1stEmail = rqester1stEmail; }

    public String getRqesterId() { return rqesterId; }
    public void setRqesterId(String rqesterId) { this.rqesterId = rqesterId; }
    public String getRqesterNm() { return rqesterNm; }
    public void setRqesterNm(String rqesterNm) { this.rqesterNm = rqesterNm; }
    public String getRqesterCttpc() { return rqesterCttpc; }
    public void setRqesterCttpc(String rqesterCttpc) { this.rqesterCttpc = rqesterCttpc; }
    public String getRqesterEmail() { return rqesterEmail; }
    public void setRqesterEmail(String rqesterEmail) { this.rqesterEmail = rqesterEmail; }
    public String getRqesterDept() { return rqesterDept; }
    public void setRqesterDept(String rqesterDept) { this.rqesterDept = rqesterDept; }

    public String getSrvcRsponsSj() { return srvcRsponsSj; }
    public void setSrvcRsponsSj(String srvcRsponsSj) { this.srvcRsponsSj = srvcRsponsSj; }
    public String getSrvcRsponsCn() { return srvcRsponsCn; }
    public void setSrvcRsponsCn(String srvcRsponsCn) { this.srvcRsponsCn = srvcRsponsCn; }

    public String getRequstAtchmnflId() { return requstAtchmnflId; }
    public void setRequstAtchmnflId(String requstAtchmnflId) { this.requstAtchmnflId = requstAtchmnflId; }

    public String getRqesterPsitn() { return rqesterPsitn; }
    public void setRqesterPsitn(String rqesterPsitn) { this.rqesterPsitn = rqesterPsitn; }

    public String getFinishId() { return finishId; }
    public void setFinishId(String finishId) { this.finishId = finishId; }
    public String getFinishUserNm() { return finishUserNm; }
    public void setFinishUserNm(String finishUserNm) { this.finishUserNm = finishUserNm; }

    // Date display helpers (examples)
    public String getVerifyDtDateDisplay() {
        if (verifyDt != null) {
            verifyDtDateDisplay = new SimpleDateFormat("yyyy-MM-dd").format(verifyDt);
        }
        return verifyDtDateDisplay;
    }
    public String getVerifyDtTimeDisplay() {
        if (verifyDt != null) {
            verifyDtTimeDisplay = new SimpleDateFormat("HH:mm").format(verifyDt);
        }
        return verifyDtTimeDisplay;
    }
    public void makeVerifyDt() {
        String dateString = (this.getVerifyDtDateDisplay() == null ? "" : this.getVerifyDtDateDisplay()) + " " + (this.getVerifyDtTimeDisplay() == null ? "" : this.getVerifyDtTimeDisplay());
        try {
            this.verifyDt = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateString.trim());
        } catch (ParseException e) {
            // ignore parse problems here
        }
    }

    // Keep class closed and tidy
}
