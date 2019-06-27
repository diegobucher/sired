package br.gov.caixa.gitecsa.sired.extra.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.TramiteRemessaDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class TramiteRemessaService extends AbstractService<TramiteRemessaVO>{

  private static final long serialVersionUID = 1L;
  
  @Inject
  private TramiteRemessaDAO tramiteRemessaDAO;
  
  @Override
  protected void validaCamposObrigatorios(TramiteRemessaVO entity) throws BusinessException {
    /**
     * Método herdado não utilizado
     */
  }

  @Override
  protected void validaRegras(TramiteRemessaVO entity) throws BusinessException {
    /**
     * Método herdado não utilizado
     */
  }

  @Override
  protected void validaRegrasExcluir(TramiteRemessaVO entity) throws BusinessException {
    /**
     * Método herdado não utilizado
     */
  }

  @Override
  protected GenericDAO<TramiteRemessaVO> getDAO() {
    return tramiteRemessaDAO;
  }
  
  public TramiteRemessaVO salvarTramiteRemessaAlterada(RemessaVO remessaVO) {
    return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.ALTERADA.getId()));
  }
  
  public TramiteRemessaVO salvarTramiteRemessaEmAlteracao(RemessaVO remessaVO) {
    return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.EM_ALTERACAO.getId()));
  }
  
  public TramiteRemessaVO salvarTramiteRemessaRecebida(RemessaVO remessaVO) {
    return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.RECEBIDA.getId()));
  }
  
  public TramiteRemessaVO salvarTramiteRemessaConferida(RemessaVO remessaVO) {
    return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.CONFERIDA.getId()));
  }
  
  public SituacaoRemessaVO buscarSituacaoRemessa(Long id) {
    return tramiteRemessaDAO.buscarSituacaoRemessa(id);
  }
  
  private TramiteRemessaVO salvarTramiteRemesssa(RemessaVO remessaVO, SituacaoRemessaVO situacao) {
    TramiteRemessaVO vo = new TramiteRemessaVO();
    if(JSFUtil.getUsuario() != null) {
      vo.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
    }
    vo.setDataTramiteRemessa(new Date());
    vo.setRemessa(remessaVO);
    vo.setSituacao(situacao);
    return tramiteRemessaDAO.save(vo);
  }
  
  public TramiteRemessaVO salvarTramiteRemessaRascunho(RemessaVO remessaVO) {
    return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.RASCUNHO.getId()));
  }
  
  public List<TramiteRemessaVO> findByRemessa(RemessaVO remessa) throws AppException{
    return tramiteRemessaDAO.findByRemessa(remessa);
  }
  
  public TramiteRemessaVO salvarTramiteRemessa(TramiteRemessaVO tramiteRemessaVO) {
    return tramiteRemessaDAO.save(tramiteRemessaVO);
  }

}
