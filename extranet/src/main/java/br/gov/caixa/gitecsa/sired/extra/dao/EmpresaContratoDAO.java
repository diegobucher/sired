package br.gov.caixa.gitecsa.sired.extra.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public interface EmpresaContratoDAO extends GenericDAO<EmpresaContratoVO> {

    EmpresaContratoVO buscarContratoVigente(RequisicaoVO vo) throws AppException;

}
