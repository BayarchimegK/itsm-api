package com.example.itsm_api.cmmncode.vo;

public class CmmnCodeVO extends CmmnCodeTyVO {
    public static final String USER_TY_CODE = "R0";
    public static final String USER_STTUS_CODE = "U0";
    public static final String TRGET_SRVC_CODE = "A0";
    public static final String TRGET_SRVC_DETAIL_CODE = "A2";
    public static final String REP_TY_CODE = "B0";
    public static final String REP_SYS_CODE = "B1";
    public static final String REP_ATTD_CODE = "B2";
    public static final String REP_STTUS_CODE = "B3";
    public static final String CHANGE_DFFLY_CODE = "S0";
    public static final String SRVC_RSPONS_CL_CODE = "S1";
    public static final String PROCESS_STDR_CODE = "S2";
    public static final String SRVC_RSPONS_BASIS_CODE = "S3";
    public static final String FI_CL_CODE = "F0";
    public static final String WDTB_SE_CODE = "W0";
    public static final String ASSET_SE_CODE = "T0";
    public static final String ASSET_HW_CODE = "T1";
    public static final String ASSET_SW_CODE = "T2";
    public static final String LOCATE_CODE = "L0";
    public static final String HOLIDAY_CODE = "D0";

    private String cmmnCode;
    private String cmmnCodeNm;
    private String cmmnCodeDc;
    private String cmmnCodeSubNm1;
    private String cmmnCodeSubNm2;
    private String cmmnCodeSubNm3;
    private String sortNo;

    public CmmnCodeVO() {
    }

    public CmmnCodeVO(String cmmnCodeTy) {
        this.setCmmnCodeTy(cmmnCodeTy);
    }

    public String getSortNo() {
        return sortNo;
    }

    public void setSortNo(String sortNo) {
        this.sortNo = sortNo;
    }

    public String getCmmnCode() {
        return cmmnCode;
    }

    public void setCmmnCode(String cmmnCode) {
        this.cmmnCode = cmmnCode;
    }

    public String getCmmnCodeNm() {
        return cmmnCodeNm;
    }

    public void setCmmnCodeNm(String cmmnCodeNm) {
        this.cmmnCodeNm = cmmnCodeNm;
    }

    public String getCmmnCodeDc() {
        return cmmnCodeDc;
    }

    public void setCmmnCodeDc(String cmmnCodeDc) {
        this.cmmnCodeDc = cmmnCodeDc;
    }

    public String getCmmnCodeSubNm1() {
        return cmmnCodeSubNm1;
    }

    public void setCmmnCodeSubNm1(String cmmnCodeSubNm1) {
        this.cmmnCodeSubNm1 = cmmnCodeSubNm1;
    }

    public String getCmmnCodeSubNm2() {
        return cmmnCodeSubNm2;
    }

    public void setCmmnCodeSubNm2(String cmmnCodeSubNm2) {
        this.cmmnCodeSubNm2 = cmmnCodeSubNm2;
    }

    public String getCmmnCodeSubNm3() {
        return cmmnCodeSubNm3;
    }

    public void setCmmnCodeSubNm3(String cmmnCodeSubNm3) {
        this.cmmnCodeSubNm3 = cmmnCodeSubNm3;
    }
}
