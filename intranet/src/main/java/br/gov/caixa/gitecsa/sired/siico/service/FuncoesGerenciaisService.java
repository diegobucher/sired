package br.gov.caixa.gitecsa.sired.siico.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.siico.dao.FuncoesGerenciaisDAO;
import br.gov.caixa.gitecsa.sired.siico.vo.ViewFuncoesGerenciaisVO;

@Stateless(name = "funcoesGerenciaisService")
public class FuncoesGerenciaisService extends AbstractService<ViewFuncoesGerenciaisVO> {

  private static final long serialVersionUID = 9113982017947330374L;

  @Inject
  private FuncoesGerenciaisDAO dao;
  
  @Override
  protected GenericDAO<ViewFuncoesGerenciaisVO> getDAO() {
    return this.dao;
  }
  
  @Override
  protected void validaCamposObrigatorios(ViewFuncoesGerenciaisVO entity) throws BusinessException {
    // TODO Auto-generated method stub

  }

  @Override
  protected void validaRegras(ViewFuncoesGerenciaisVO entity) throws BusinessException {
    // TODO Auto-generated method stub

  }

  @Override
  protected void validaRegrasExcluir(ViewFuncoesGerenciaisVO entity) throws BusinessException {
    // TODO Auto-generated method stub

  }
}
