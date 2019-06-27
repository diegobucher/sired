package br.gov.caixa.gitecsa.sired.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;

public interface ParametroSistemaDAO extends GenericDAO<ParametroSistemaVO> {
    ParametroSistemaVO findById(Long id) throws DataBaseException;

    ParametroSistemaVO findByNome(String nome) throws DataBaseException;
}
