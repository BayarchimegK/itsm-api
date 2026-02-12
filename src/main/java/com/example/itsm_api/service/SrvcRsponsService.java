package com.example.itsm_api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.itsm_api.mapper.SrvcRsponsMapper;
import com.example.itsm_api.vo.SrvcRsponsVO;

@Service("srvcRsponsService")
public class SrvcRsponsService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SrvcRsponsService.class);
	
	private final SrvcRsponsMapper srvcRsponsMapper;

	public SrvcRsponsService(SrvcRsponsMapper srvcRsponsMapper) {
		this.srvcRsponsMapper = srvcRsponsMapper;
	}
	
	/**
	 * SR정보를 등록한다. 요청 정보만 등록. 관리자에 의해 전체내용 등록시 update 호출하여 처리
	 * @param vo - 등록할 정보가 담긴 SrvcRsponsVO
	 * @return 등록 결과
	 * @exception Exception
	 */
	public void create(SrvcRsponsVO vo) throws Exception{
		srvcRsponsMapper.create(vo);
	}
	
	/**
	 * SR요청정보를 수정한다. 
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int updateRequst(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateRequst(vo);
	}
	
	public int updateReceive(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateReceive(vo);
	}
	
	public void createForMngr(SrvcRsponsVO vo) throws Exception{
		srvcRsponsMapper.create(vo);
		
		LOGGER.debug("srvcRsponsNo: "+vo.getSrvcRsponsNo());		
//		vo.setSrvcRsponsNo(srvcRsponsNo);
		
		int cnt = srvcRsponsMapper.update(vo);
		LOGGER.debug("update cnt: "+cnt);
	}	
		
	/**
	 * SR정보 1차응답 기록한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int updateRspons1st(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateRspons1st(vo);
	}
	
	/**
	 * SR정보 처리내역을 등록한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int updateProcess(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateProcess(vo);
	}

	public int updateSrProcess(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateSrProcess(vo);
	}

	public int updateSrVerify(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateSrVerify(vo);
	}

	public int updateSrFinish(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateSrFinish(vo);
	}

	public int updateSrEv(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateSrEv(vo);
	}

	public int updateSrEvReRequest(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateSrEvReRequest(vo);
	}

	public int createSrReRequest(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.createSrReRequest(vo);
	}

	/**
	 * SR정보 확인자의 확인 처리
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int updateCnfrmr(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateCnfrmr(vo);
	}
	
	/**
	 * SR배포확인서 등록 처리
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int updateWdtbCnfirm(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateWdtbCnfirm(vo);
	}
	
	/**
	 * 인프라작업 등록
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int updateInfraOpert(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateInfraOpert(vo);
	}
	
	/**
	 * sms확인처리
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int updateSmsChk(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.updateSmsChk(vo);
	}
	
	
	
	/**
	 * SR배포확인서 삭제
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int deleteWdtbCnfirm(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.deleteWdtbCnfirm(vo);
	}
	
	/**
	 * 인프라 작업 삭제
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int deleteInfraOpert(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.deleteInfraOpert(vo);
	}
	
	/**
	 * SR정보를 수정한다. 전체 데이터 갱신용(관리목적)
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int update(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.update(vo);
	}
	
	/**
	 * SR정보를 삭제한다.(soft delete)
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int delete(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.delete(vo);
	}
	
	/**
	 * SR정보를 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public SrvcRsponsVO retrieve(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieve(vo);
	}
	
	/**
	 * SR정보 페이징 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrievePagingList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrievePagingList(vo);
	}
	
	public List<SrvcRsponsVO> retrieveSrReqList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrReqList(vo);
	}
	public List<SrvcRsponsVO> retrieveSrRcvList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrRcvList(vo);
	}
	public List<SrvcRsponsVO> retrieveSrProcList(SrvcRsponsVO vo) throws Exception{
		//R001 모두 보기, R003 해당 서비스만 보기, R005 자신거만
		return srvcRsponsMapper.retrieveSrProcList(vo);
	}
	public List<SrvcRsponsVO> retrieveSrVrList(SrvcRsponsVO vo) throws Exception{
		//처리결과 검증 CMMN_CODE_SUB_NM1 trgetSrvcCodeSubNm1 = 'Z1' 
		return srvcRsponsMapper.retrieveSrVrList(vo);
	}
	public List<SrvcRsponsVO> retrieveSrFnList(SrvcRsponsVO vo) throws Exception{
		//검증결과 완료처리 CMMN_CODE_SUB_NM1 trgetSrvcCodeSubNm1 = 'Z1' 
		return srvcRsponsMapper.retrieveSrFnList(vo);
	}

	public List<SrvcRsponsVO> retrieveSrEvList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrEvList(vo);
	}
	
	
	/**
	 * 배포가 필요한 SR정보 페이징 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrieveWdtbPagingList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveWdtbPagingList(vo);
	}
	
	/**
	 * 인프라작업 관련 SR정보 페이징 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrieveInfraOpertPagingList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveInfraOpertPagingList(vo);
	}
	
	/**
	 * 응용기능개선 체크된 SR정보 페이징 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrievefnctImprvmPagingList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrievefnctImprvmPagingList(vo);
	}
	
	/**
	 * SR정보 총 갯수를 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int retrievePagingListCnt(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrievePagingListCnt(vo);
	}
	public int retrieveSrReqPagingListCnt(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrReqPagingListCnt(vo);
	}
	public int retrieveSrRcvPagingListCnt(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrReqPagingListCnt(vo);
	}
	public int retrieveSrProcPagingListCnt(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrProcPagingListCnt(vo);
	}
	public int retrieveSrVrPagingListCnt(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrVrPagingListCnt(vo);
	}
	public int retrieveSrFnPagingListCnt(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrFnPagingListCnt(vo);
	}
	public int retrieveSrEvPagingListCnt(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrEvPagingListCnt(vo);
	}
	
	/**
	 * SR정보 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrieveList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveList(vo);
	}
	
	/**
	 * SR정보 목록을 조회한다. 무제한
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrieveAllList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveAllList(vo);
	}
	
	/**
	 * SR정보중 배포확인서가 있는 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrieveAllwdtbList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveAllwdtbList(vo);
	}
	
	/**
	 * 요청자 정보 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrieveRqesterNmList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveRqesterNmList(vo);
	}
	
	/**
	 * 1차요청자 정보 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrieveRqester1stNmList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveRqester1stNmList(vo);
	}
	
	/**
	 * SR 번호 정보 목록을 조회한다.
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<SrvcRsponsVO> retrieveSrvcRsponsNoList(SrvcRsponsVO vo) throws Exception{
		return srvcRsponsMapper.retrieveSrvcRsponsNoList(vo);
	}
}
