package com.example.itsm_api.cmmncode.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.itsm_api.cmmncode.vo.CmmnCodeTyVO;

@Mapper
public interface CmmnCodeTyMapper {
    List<CmmnCodeTyVO> retrieveList(CmmnCodeTyVO vo) throws Exception;
    List<CmmnCodeTyVO> retrieveAllList() throws Exception;
    void create(CmmnCodeTyVO vo) throws Exception;
    int update(CmmnCodeTyVO vo) throws Exception;
    int delete(CmmnCodeTyVO vo) throws Exception;
}
