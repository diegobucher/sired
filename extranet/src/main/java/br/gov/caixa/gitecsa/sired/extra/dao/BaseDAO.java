package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.LoteSequenciaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface BaseDAO extends GenericDAO<BaseVO> {

    List<BaseVO> consultaBasePorIdUnidade(long idUnidade);

    List<LoteSequenciaVO> loteSequenciaVinculado(String base, UnidadeVO unidadeVO) throws AppException;

    BaseVO getBaseByUnidade(UnidadeVO unidadeVO);

    List<BaseVO> consultaBasesEmpresaContrato();

    List<EmpresaContratoVO> relacionamentoEmpresaContrato(Long idBase) throws AppException;

}
