package com.example.itsm_api.cmmncode.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.itsm_api.cmmncode.vo.CmmnCodeVO;

@Mapper
public interface CmmnCodeMapper {
    void create(CmmnCodeVO vo) throws Exception;
    int update(CmmnCodeVO vo) throws Exception;
    int delete(CmmnCodeVO vo) throws Exception;
    int restore(CmmnCodeVO vo) throws Exception;
    CmmnCodeVO retrieve(CmmnCodeVO vo) throws Exception;
    List<CmmnCodeVO> retrieveList(CmmnCodeVO vo) throws Exception;
    List<CmmnCodeVO> retrievePagingList(CmmnCodeVO vo) throws Exception;
    int retrievePagingListCnt(CmmnCodeVO vo) throws Exception;
}
