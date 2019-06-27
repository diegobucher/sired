package br.gov.caixa.gitecsa.sired.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.vo.BaseAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface EmpresaContratoDAO extends GenericDAO<EmpresaContratoVO> {
    EmpresaContratoVO findByAbrangenciaUnidadeEager(UnidadeVO unidade) throws DataBaseException;
    EmpresaContratoVO findByBaseAtendimento(BaseAtendimentoVO baseAtendimento) throws DataBaseException;
}
