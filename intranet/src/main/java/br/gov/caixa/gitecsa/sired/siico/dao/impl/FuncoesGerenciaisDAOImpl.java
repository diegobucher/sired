package br.gov.caixa.gitecsa.sired.siico.dao.impl;

import java.io.Serializable;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.SiicoDAOImpl;
import br.gov.caixa.gitecsa.sired.siico.dao.FuncoesGerenciaisDAO;
import br.gov.caixa.gitecsa.sired.siico.vo.ViewFuncoesGerenciaisVO;

public class FuncoesGerenciaisDAOImpl extends SiicoDAOImpl<ViewFuncoesGerenciaisVO> implements FuncoesGerenciaisDAO {

  @Override
  public ViewFuncoesGerenciaisVO findById(Serializable id) {
    
    return (ViewFuncoesGerenciaisVO) getSessionSiico().get(getPersistentClass(), id);
  }
    
}
