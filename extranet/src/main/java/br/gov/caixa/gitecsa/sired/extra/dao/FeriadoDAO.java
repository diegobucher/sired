package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.Date;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface FeriadoDAO extends GenericDAO<FeriadoVO> {
    Boolean isFeriado(final Date data, final UnidadeVO unidadeVO);
}
