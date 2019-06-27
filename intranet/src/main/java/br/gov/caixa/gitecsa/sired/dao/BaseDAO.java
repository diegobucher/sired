package br.gov.caixa.gitecsa.sired.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface BaseDAO extends GenericDAO<BaseVO> {
    BaseVO findByLoteSequenciaEUnidade(String numLoteSequencia, UnidadeVO unidade);
}
