package br.gov.caixa.gitecsa.sired.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.sired.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.sired.service.RemessaService;
import br.gov.caixa.gitecsa.sired.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
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
      this.unidadeSolicitante = unidadeService.findUnidadeLotacaoUsuarioLogado();
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

  public String confirmarExclusaoItemRemessa() {
    remessaDocumentoService.excluirremessaDocumento(this.remessaDocumento);
    this.remessa = remessaService.obterRemessaComListaDocumentos((Long) this.remessa.getId());
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("remessa.tipoAB.exclusao.mensagem.sucesso"));
    return null;
  }
  
  public String cancelarExclusaoItemRemessa() {
    this.remessaDocumento = null;
    return null;
  }
  
  public void excluirDocumentoRemessa(RemessaDocumentoVO doc){
    this.remessaDocumento = doc;
  }

  /**
   * Método de gegração da Etiqueta de remessa de movimento Diário.
   * @return Stream
   */
  public StreamedContent gerarTermoResponsabilidade() {
    try {
      return remessaService.gerarTermoResponsabilidade(this.remessa);
    } catch (Exception e) {
      return null;
    }
  }
  
  public StreamedContent gerarEtiquetaDocumentoABIndividual(RemessaDocumentoVO docAB) {
      try {
        return remessaService.gerarEtiquetaDocumentoABIndividual(docAB);
      } catch (Exception e) {
        return null;
      }
    }
  
  public String editarDocumentoRemessa(RemessaDocumentoVO doc) {
    JSFUtil.setSessionMapValue("edicaoDocumentoRemessa", Boolean.TRUE);
    JSFUtil.setSessionMapValue("remessaDocumento", doc);
    return "/paginas/remessa/aberturaDocAB.xhtml?faces-redirect=true";
  }

  public String prepareConcluirRemessa() {
    try {
      if (this.remessa.getRemessaDocumentos().isEmpty()) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA065"));
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
  
  public String prepareCorrigirRemessa() {
    try {
      if (this.remessa.getRemessaDocumentos().isEmpty()) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA065"));
        return null;
      }
      RequestContext.getCurrentInstance().execute("modalCorrigir.show()");
      return null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);      
      return null;
    }
  }
  
  public String concluirRemessa() {
    MensagemUtils.setKeepMessages(true);
    try {
      this.remessa = remessaService.concluirRemessaDocumentosTipoAB(this.remessa);
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS028", this.remessa.getId()));
      return "/paginas/remessa/mantem-remessa.xhtml?faces-redirect=true";
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);      
      return null;
    }
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
    
    if(situacaoAtual.getId().equals(1L) || ObjectUtils.isNullOrEmpty(situacaoAtual)) {
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

}
