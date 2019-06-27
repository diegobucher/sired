package br.gov.caixa.gitecsa.sired.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.dao.RemessaMovimentoDiarioDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;

@Stateless
public class RemessaMovimentoDiarioService implements Serializable {

  /** Serial. */
  private static final long serialVersionUID = -3543598754995772753L;

  @Inject
  private RemessaMovimentoDiarioDAO remessaMovimentoDiarioDAO;

  public RemessaMovimentoDiarioVO salvarItemMovimentoDiario(RemessaMovimentoDiarioVO item) {
    if (item.getId() == null) {
      remessaMovimentoDiarioDAO.save(item);
    } else {
      remessaMovimentoDiarioDAO.update(item);
    }
    return item;
  }

  public void excluirItemMovimentoDiarioPorId(RemessaMovimentoDiarioVO item) {
    remessaMovimentoDiarioDAO.delete(item);
  }

  public RemessaMovimentoDiarioVO findByUnidadeDtMovimento(RemessaMovimentoDiarioVO item) {
    RemessaMovimentoDiarioVO remessaMovDiario = new RemessaMovimentoDiarioVO();
    remessaMovDiario = remessaMovimentoDiarioDAO.findByUnidadeDtMovimento(item);
    return remessaMovDiario;
  }

}
