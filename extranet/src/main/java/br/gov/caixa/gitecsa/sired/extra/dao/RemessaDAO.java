package br.gov.caixa.gitecsa.sired.extra.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;

public interface RemessaDAO extends GenericDAO<RemessaVO>{

  RemessaVO obterRemessaComMovimentosDiarios(long id);

  RemessaVO obterRemessaComListaDocumentos(Long id);

}
