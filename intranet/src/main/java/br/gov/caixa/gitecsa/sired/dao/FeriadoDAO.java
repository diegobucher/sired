package br.gov.caixa.gitecsa.sired.dao;

import java.util.Date;
import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface FeriadoDAO extends GenericDAO<FeriadoVO> {
    Date findProximaDataUtil(final Date data, final Integer dias, final UnidadeVO unidadeVO) throws DataBaseException;

    Boolean isFeriado(final Date data, final UnidadeVO unidadeVO) throws DataBaseException;

    List<FeriadoVO> findByPeriodo(final Date dtInicio, final Date dtFim, final UnidadeVO unidadeVO) throws DataBaseException;
    
    void update(List<FeriadoVO> listFeriado);

    void persist(List<FeriadoVO> listFeriado);
}
