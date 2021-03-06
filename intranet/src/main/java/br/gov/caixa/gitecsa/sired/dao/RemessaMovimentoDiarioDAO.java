package br.gov.caixa.gitecsa.sired.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;

public interface RemessaMovimentoDiarioDAO  extends GenericDAO<RemessaMovimentoDiarioVO>{

  RemessaMovimentoDiarioVO findByUnidadeDtMovimento(RemessaMovimentoDiarioVO remessaMovDiarioVO);

}
