package com.example.itsm_api.base.vo;

import java.util.Date;

public class BaseVO {
    private String deleteYn;
    private String creatId;
    private String updtId;
    private Date creatDt;
    private Date updtDt;

    public String getDeleteYn() {
        return deleteYn;
    }

    public void setDeleteYn(String deleteYn) {
        this.deleteYn = deleteYn;
    }

    public String getCreatId() {
        return creatId;
    }

    public void setCreatId(String creatId) {
        this.creatId = creatId;
    }

    public String getUpdtId() {
        return updtId;
    }

    public void setUpdtId(String updtId) {
        this.updtId = updtId;
    }

    public Date getCreatDt() {
        return creatDt;
    }

    public void setCreatDt(Date creatDt) {
        this.creatDt = creatDt;
    }

    public Date getUpdtDt() {
        return updtDt;
    }

    public void setUpdtDt(Date updtDt) {
        this.updtDt = updtDt;
    }
}
