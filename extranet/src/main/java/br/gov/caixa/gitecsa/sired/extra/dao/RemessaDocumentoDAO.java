package br.gov.caixa.gitecsa.sired.extra.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;

public interface RemessaDocumentoDAO extends GenericDAO<RemessaDocumentoVO> {

  Integer consultaRemessaDocumentoAtrelado(RemessaDocumentoVO remessaDocumentoVO);

}
