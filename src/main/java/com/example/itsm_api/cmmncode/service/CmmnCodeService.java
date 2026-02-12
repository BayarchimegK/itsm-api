package com.example.itsm_api.cmmncode.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.itsm_api.cmmncode.dao.CmmnCodeMapper;
import com.example.itsm_api.cmmncode.vo.CmmnCodeVO;

@Service
public class CmmnCodeService {
    private final CmmnCodeMapper cmmnCodeMapper;

    public CmmnCodeService(CmmnCodeMapper cmmnCodeMapper) {
        this.cmmnCodeMapper = cmmnCodeMapper;
    }

    public void create(CmmnCodeVO vo) throws Exception {
        cmmnCodeMapper.create(vo);
    }

    public int update(CmmnCodeVO vo) throws Exception {
        return cmmnCodeMapper.update(vo);
    }

    public int restore(CmmnCodeVO vo) throws Exception {
        return cmmnCodeMapper.restore(vo);
    }

    public int delete(CmmnCodeVO vo) throws Exception {
        return cmmnCodeMapper.delete(vo);
    }

    public CmmnCodeVO retrieve(CmmnCodeVO vo) throws Exception {
        return cmmnCodeMapper.retrieve(vo);
    }

    public List<CmmnCodeVO> retrieveList(CmmnCodeVO vo) throws Exception {
        return cmmnCodeMapper.retrieveList(vo);
    }

    public List<CmmnCodeVO> retrievePagingList(CmmnCodeVO vo) throws Exception {
        return cmmnCodeMapper.retrievePagingList(vo);
    }

    public int retrievePagingListCnt(CmmnCodeVO vo) throws Exception {
        return cmmnCodeMapper.retrievePagingListCnt(vo);
    }
}
