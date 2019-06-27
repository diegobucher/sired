package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.TipoUnidadeVO;

public interface TipoUnidadeDAO extends GenericDAO<TipoUnidadeVO> {

    void update(List<TipoUnidadeVO> listTipoUnidade);

    void persist(List<TipoUnidadeVO> listTipoUnidade);
    
}
