package br.gov.caixa.gitecsa.sired.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.BaseAtendimentoVO;

public interface BaseAtendimentoDAO extends GenericDAO<BaseAtendimentoVO> {
    BaseAtendimentoVO findByIdEager(Long id);
}
