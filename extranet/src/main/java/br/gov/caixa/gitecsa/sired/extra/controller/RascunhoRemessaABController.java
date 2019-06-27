package br.gov.caixa.gitecsa.sired.extra.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.context.RequestContext;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.service.RemessaService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class RascunhoRemessaABController implements Serializable {

  /** Serial. */
  private static final long serialVersionUID = 2104792185636445056L;
  
  @Inject
  protected FacesMensager facesMessager;

  @Inject
  protected transient Logger logger;
  
  @Inject
  private UnidadeService unidadeService;
  
  @Inject
  private RemessaService remessaService;
  
  @Inject
  private RemessaDocumentoService remessaDocumentoService;

  private RemessaVO remessa;
  
  private RemessaDocumentoVO remessaDocumento; 
  
  private UnidadeVO unidadeSolicitante;
  
  private List<RemessaDocumentoVO> listaDocumentos;

  @PostConstruct
  protected void init() {
    try {
      this.limparCampos();
      if (JSFUtil.getSessionMapValue("remessa") != null) {
        this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
        this.remessa = remessaService.obterRemessaComListaDocumentos((Long) this.remessa.getId());
        JSFUtil.setSessionMapValue("remessa", null);
      } else {
        FacesContext.getCurrentInstance().getExternalContext().redirect("mantem-remessa");
      }
      this.unidadeSolicitante = null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
    }
  }

  private void limparCampos() {
    this.remessa = null;
    this.remessaDocumento = null;
  }

  public String incluirDocumento() {
    JSFUtil.setSessionMapValue("modoEdicaoRemessaAB", Boolean.TRUE);
    JSFUtil.setSessionMapValue("remessa", this.remessa);
    return "/paginas/remessa/abertura.xhtml?faces-redirect=true";
  }
  
  public String incluirEmLote() {
    JSFUtil.setSessionMapValue("modoEdicaoRemessaAB", Boolean.TRUE);
    JSFUtil.setSessionMapValue("remessa", this.remessa);
    return "/paginas/remessa/aberturaLote.xhtml?faces-redirect=true";
  }

  public String cancelarExclusaoItemRemessa() {
    this.remessaDocumento = null;
    return null;
  }
  
  public String cancelarCorrecao() {
    return "/paginas/remessa/consulta";
  }
  
  public void excluirDocumentoRemessa(RemessaDocumentoVO doc){
    this.remessaDocumento = doc;
  }
  
  public String confirmarExclusaoItemRemessa() {
    RemessaDocumentoVO remessaDocumentoPai = new RemessaDocumentoVO();
    remessaDocumentoPai = this.remessaDocumento.getNumeroRemessaTipoAB();
    remessaDocumentoPai.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
    remessaDocumentoService.salvar(remessaDocumentoPai);
    remessaDocumentoService.excluirremessaDocumento(this.remessaDocumento);
    this.remessa = remessaService.obterRemessaComListaDocumentos((Long) this.remessa.getId());
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("remessa.tipoAB.exclusao.mensagem.sucesso"));
    return null;
  }
  
  public String editarDocumentoRemessa(RemessaDocumentoVO doc) {
    JSFUtil.setSessionMapValue("edicaoDocumentoRemessa", Boolean.TRUE);
    JSFUtil.setSessionMapValue("remessaDocumento", doc);
    return "/paginas/remessa/aberturaDocAB.xhtml?faces-redirect=true";
  }

  public String prepareConcluirRemessa() {
    try {
      if (this.remessa.getRemessaDocumentos().isEmpty()) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA068"));
        return null;
      }
      RequestContext.getCurrentInstance().execute("modalConfirmar.show()");
      return null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);      
      return null;
    }
  }  
  
  public String prepareAlterarRemessa() {
    try {
      if (this.remessa.getRemessaDocumentos().isEmpty()) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA068"));
        return null;
      }
      RequestContext.getCurrentInstance().execute("modalAlterar.show()");
      return null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);      
      return null;
    }
  }
  
  public String alterarRemessa() {
    MensagemUtils.setKeepMessages(true);
    try {
      this.remessa = remessaService.alterarRemessaDocumento(this.remessa);
      alterarSituacaoRemessaParaAlterada(this.listaDocumentos);
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS030", this.remessa.getId()));
      return "/paginas/remessa/consulta.xhtml?faces-redirect=true";
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);      
      return null;
    }
  }
  
  private void alterarSituacaoRemessaParaAlterada(List<RemessaDocumentoVO> listaDocumentos) {
    for (RemessaDocumentoVO remessaDocumentoVO : listaDocumentos) {
      if(remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA)) {
        remessaDocumentoVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA);
        remessaDocumentoService.salvar(remessaDocumentoVO);
      }
    }
    
  }

  public String prepareCancelarCorrecaoRemessa() {
    try {
      if (this.remessa.getRemessaDocumentos().isEmpty()) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA068"));
        return null;
      }
      if(!this.remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.RECEBIDA.getId())) {
        RequestContext.getCurrentInstance().execute("modalCancelar.show()");
      }else {
        return "/paginas/remessa/consulta.xhtml?faces-redirect=true";
      }
      return null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);      
      return null;
    }
  }
  
  public String cancelarAlteracoesRemessa() {
    MensagemUtils.setKeepMessages(true);
    try {
      removeAlteracoes(this.remessa);
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS031", this.remessa.getId()));
      return "/paginas/remessa/consulta.xhtml?faces-redirect=true";
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);      
      return null;
    }
  }
  
  private void removeAlteracoes(RemessaVO remessaVO) throws Exception {
    List<RemessaDocumentoVO> remessaDocumentoList = new ArrayList<RemessaDocumentoVO>();
    remessaDocumentoList = this.remessa.getRemessaDocumentos();
    
    for (RemessaDocumentoVO remessaDocumentoVO : remessaDocumentoList) {
      if(remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA)) {
        remessaDocumentoService.delete(remessaDocumentoVO);
      }
      if(remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
        remessaDocumentoVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
        remessaDocumentoVO = remessaDocumentoService.salvar(remessaDocumentoVO);
      }
    }
    RemessaVO remessa = new RemessaVO();
    remessa = remessaService.getById(this.remessa.getId());
    remessaService.recebidaRemessaDocumento(remessa);
  }

  public RemessaVO getRemessa() {
    return remessa;
  }

  public void setRemessa(RemessaVO remessa) {
    this.remessa = remessa;
  }

  /**
   * @return the unidadeSolicitante
   */
  public UnidadeVO getUnidadeSolicitante() {
    return unidadeSolicitante;
  }

  /**
   * @param unidadeSolicitante the unidadeSolicitante to set
   */
  public void setUnidadeSolicitante(UnidadeVO unidadeSolicitante) {
    this.unidadeSolicitante = unidadeSolicitante;
  }
  
  public boolean isRascunho() {
    TramiteRemessaVO tramiteRemessaAtual = new TramiteRemessaVO();
    tramiteRemessaAtual = this.remessa.getTramiteRemessaAtual();
    SituacaoRemessaVO situacaoAtual = tramiteRemessaAtual.getSituacao();
    
    if(situacaoAtual.getId().equals(1L)) {
      return true;
    }
    return false;
  }
  
  public boolean isCorrecao() {
    TramiteRemessaVO tramiteRemessaAtual = new TramiteRemessaVO();
    tramiteRemessaAtual = this.remessa.getTramiteRemessaAtual();
    SituacaoRemessaVO situacaoAtual = tramiteRemessaAtual.getSituacao();
    
    if(situacaoAtual.getId().equals(8L)) {
      return true;
    }
    return false;
  }
  
  public boolean exibeBotaoExcluir(RemessaDocumentoVO remessaDocumento) {
    if(remessaDocumento.getNumeroRemessaTipoAB() != null) {
      return true;
    }else {
    return false;
      }
  }
  
  public boolean exibeBotaoAlterarRemessa() {
    List<RemessaDocumentoVO> remessaDocumentoList = new ArrayList<RemessaDocumentoVO>();
    remessaDocumentoList = this.remessa.getRemessaDocumentos();
    for (RemessaDocumentoVO remessaDocumentoVO : remessaDocumentoList) {
      if(remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA)
          || remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean exibeBotaoEditar(RemessaDocumentoVO remessaDocumento) {
    if(remessaDocumento.getIcAlteracaoValida() != SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO) {
      return true;
    }else {
    return false;
      }
  }
  
  public List<RemessaDocumentoVO> exibeListaDocumentosTratados(){
    List<RemessaDocumentoVO> listaAux = new ArrayList<RemessaDocumentoVO>();
    listaAux.addAll(this.remessa.getRemessaDocumentos());
    listaDocumentos = listaAux;
    Iterator<RemessaDocumentoVO> itr = listaDocumentos.iterator();
    
    while(itr.hasNext()) {
      RemessaDocumentoVO remessaDocumentoAux = itr.next();
      if(remessaDocumentoAux.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
        itr.remove();
      }
    }
    
    return listaDocumentos;
  }

  /**
   * @return the listaDocumentos
   */
  public List<RemessaDocumentoVO> getListaDocumentos() {
    return listaDocumentos;
  }

  /**
   * @param listaDocumentos the listaDocumentos to set
   */
  public void setListaDocumentos(List<RemessaDocumentoVO> listaDocumentos) {
    this.listaDocumentos = listaDocumentos;
  }

}
