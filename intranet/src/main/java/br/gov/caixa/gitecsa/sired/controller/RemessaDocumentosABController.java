package br.gov.caixa.gitecsa.sired.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.GrupoService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.sired.service.RemessaService;
import br.gov.caixa.gitecsa.sired.service.SequencialRemessaService;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class RemessaDocumentosABController implements Serializable {

  /** Serial. */
  private static final long serialVersionUID = 4817614317093973294L;

  @Inject
  protected FacesMensager facesMessager;

  @Inject
  protected transient Logger logger;

  @Inject
  private RemessaService remessaService;

  @Inject
  private RemessaDocumentoService remessaDocumentoService;

  @Inject
  private br.gov.caixa.gitecsa.sired.service.UnidadeService unidadeServiceRef;

  @Inject
  private GrupoService grupoService;

  @Inject
  private SequencialRemessaService sequencialRemessaService;

  private RemessaVO remessa;

  private DocumentoVO documento;
  
  private RemessaDocumentoVO remessaDocumento;

  private UsuarioLdap usuario;

  private GrupoVO grupo;

  private String codigoUnidadeFiltro;

  private String nomeUnidadeFiltro;
  
  private Boolean modoEdicaoRemessaDocumento;
  
  private Boolean modoNovaRemessaDocumento;

  @PostConstruct
  private void init() {
    try {
      this.usuario = (UsuarioLdap) JSFUtil.getSessionMapValue("usuario");
      limparCampos();
      if (JSFUtil.getSessionMapValue("edicaoDocumentoRemessa") != null ) {
        edicaoRemessaDocumento();
   
      } else if (JSFUtil.getSessionMapValue("novoDocumentoRemessa") != null ) {
        inclusaoDocumentoRemessaExistente();
      } else {
        novaRemessaNovoDocumento();
      }
    } catch (Exception e) {
      this.logger.error(e.getMessage(), e);
    }
  }

  private void novaRemessaNovoDocumento() throws DataBaseException {
    this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
    this.remessaDocumento.setUnidadeGeradora(unidadeServiceRef.findById(this.usuario.getCoUnidade().longValue()));
    this.codigoUnidadeFiltro = this.remessaDocumento.getUnidadeGeradora().getId().toString();
    this.nomeUnidadeFiltro = this.remessaDocumento.getUnidadeGeradora().getNome();
    if (JSFUtil.getSessionMapValue("documentoRemessa") != null) {
      this.documento = (DocumentoVO) JSFUtil.getSessionMapValue("documentoRemessa");
      this.remessa = new RemessaVO();
      this.remessaDocumento.setUnidadeGeradora(unidadeServiceRef.findUnidadeLotacaoUsuarioLogado());
      
    } else {
      redirectTelaPesquisaDocumento();
      return;
    }
    this.grupo = grupoService.obterGrupo(this.documento);
  }

  private void inclusaoDocumentoRemessaExistente() {
    this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
    this.documento = (DocumentoVO) JSFUtil.getSessionMapValue("documentoRemessa");
    this.modoNovaRemessaDocumento = (Boolean) JSFUtil.getSessionMapValue("novoDocumentoRemessa");
    this.remessaDocumento.setUnidadeGeradora(unidadeServiceRef.findById(this.usuario.getCoUnidade().longValue()));
    this.codigoUnidadeFiltro = this.remessaDocumento.getUnidadeGeradora().getId().toString();
    this.nomeUnidadeFiltro = this.remessaDocumento.getUnidadeGeradora().getNome();
    this.grupo = grupoService.obterGrupo(this.documento);
    JSFUtil.setSessionMapValue("novoDocumentoRemessa", null);
    JSFUtil.setSessionMapValue("remessa", null);
    JSFUtil.setSessionMapValue("documento", null);
  }

  private void edicaoRemessaDocumento() throws AppException {
    this.remessaDocumento = (RemessaDocumentoVO) JSFUtil.getSessionMapValue("remessaDocumento");
    this.modoEdicaoRemessaDocumento = Boolean.TRUE;
    this.remessaDocumento.setUnidadeGeradora(remessaDocumento.getUnidadeGeradora());
    this.codigoUnidadeFiltro = this.remessaDocumento.getUnidadeGeradora().getId().toString();
    this.nomeUnidadeFiltro = this.remessaDocumento.getUnidadeGeradora().getNome();
    this.documento = remessaDocumento.getDocumento();
    this.remessa = this.remessaDocumento.getRemessa();
    this.grupo = grupoService.obterGrupo(this.documento);
    if (grupo != null) {
        this.recuperarCamposDinamicos();
    }
    JSFUtil.setSessionMapValue("edicaoDocumentoRemessa", null);
    JSFUtil.setSessionMapValue("remessaDocumento", null);
  }

  private void limparCampos() {
    this.modoEdicaoRemessaDocumento = Boolean.FALSE;
    this.modoNovaRemessaDocumento = Boolean.FALSE;
    this.codigoUnidadeFiltro = null;
    this.nomeUnidadeFiltro = null;
    this.documento = null;
    this.remessa = new RemessaVO();
    this.remessaDocumento = new RemessaDocumentoVO();
  }
  
  private void recuperarCamposDinamicos() throws AppException {
    if (grupo != null) {
      try {
        GrupoCamposHelper.getValorCamposDinamicos(this.remessaDocumento, grupo.getGrupoCampos());
      } catch (br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException e) {
        throw new AppException(e.getMessage(), e);
      }
    }
  }

  public void pesquisarUnidadeGeradora() {
    try {
      if (StringUtils.isBlank(this.codigoUnidadeFiltro)) {
        this.nomeUnidadeFiltro = "";
        this.remessaDocumento.setUnidadeGeradora(null);
      } else {
        this.remessaDocumento.setUnidadeGeradora(unidadeServiceRef.findById(Long.parseLong(codigoUnidadeFiltro)));
        if (this.remessaDocumento.getUnidadeGeradora() != null) {
          this.nomeUnidadeFiltro = this.remessaDocumento.getUnidadeGeradora().getNome();
        } else {
          this.nomeUnidadeFiltro = "";
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
        }
      }
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "RemessaDocumentosABController", "pesquisarUnidadeGeradora"));
    }
  }

  public String cancelar() {
    if (this.modoEdicaoRemessaDocumento || this.modoNovaRemessaDocumento) {
      JSFUtil.setSessionMapValue("remessa", this.remessa);
      return "/paginas/remessa/rascunhoRemessaTipoAB.xhtml?faces-redirect=true";
    } else {
      return "/paginas/remessa/abertura.xhtml?faces-redirect=true";
    }
  }

  public void limparForm() {
    this.remessaDocumento.setUnidadeGeradora(unidadeServiceRef.findById(this.usuario.getCoUnidade().longValue()));
    if (!Util.isNull(this.grupo)) {
      for (GrupoCampoVO item : this.grupo.getGrupoCampos()) {
        item.setValor(null);
        item.setValorData(null);
      }
    }
  }

  public String gravar() {
    MensagemUtils.setKeepMessages(true);
    try {
      validarCamposObrigatorios();
      persistirRemessaC17();        
      persistirDemessaDocumentoAB();
      JSFUtil.setSessionMapValue("remessa", this.remessa);
      
      if (modoEdicaoRemessaDocumento) {
        facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MA061"));
      } else {
        facesMessager.addMessageInfo(MensagemUtils.obterMensagem("remessa.tipoAB.inclusao.mensagem.sucesso"));        
      }
      return "/paginas/remessa/rascunhoRemessaTipoAB.xhtml?faces-redirect=true";
    } catch (RequiredException e) {
      e.printStackTrace();
      for (String message : e.getErroList()) {
        facesMessager.addMessageError(message);
    }
    } catch (Exception e) {
      e.printStackTrace();
      this.logger.error(e.getMessage(), e);
    }
    return null;
  }
  
  /** 
   * Persistir o filho da Remessa - RemessaDocumento.
   * @throws AppException 
   */
  private void persistirDemessaDocumentoAB() throws AppException {
    if (this.remessaDocumento == null || this.remessaDocumento.getId() == null) {      
      this.remessaDocumento.setCodigoRemessa(sequencialRemessaService.generate(remessa.getUnidadeSolicitante()));
      this.remessaDocumento.setCodigoUsuarioUltimaAlteracao(this.usuario.getNuMatricula());
      this.remessaDocumento.setRemessa(this.remessa);
      this.remessaDocumento.setDocumento(this.documento);
      this.remessaDocumento.setDataUltimaAlteracao(new Date());
      this.remessaDocumento.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
      this.remessaDocumento = remessaDocumentoService.salvar(this.remessaDocumento);
      this.salvarCamposDinamicos(this.remessaDocumento);
      this.remessaDocumento = remessaDocumentoService.salvar(this.remessaDocumento);
    } else {
      this.remessaDocumento.setCodigoUsuarioUltimaAlteracao(this.usuario.getNuMatricula());
      this.remessaDocumento.setDataUltimaAlteracao(new Date());
      this.remessaDocumento = remessaDocumentoService.salvar(this.remessaDocumento);
      this.salvarCamposDinamicos(this.remessaDocumento);
      this.remessaDocumento = remessaDocumentoService.salvar(this.remessaDocumento);
    }
  }

  /** 
   * Persistir a Remessa C17.
   */
  private void persistirRemessaC17() throws Exception {
    if (this.remessa == null || this.remessa.getId() == null) {      
      this.remessa = new RemessaVO();
      this.remessa.setDataHoraAbertura(new Date());
      this.remessa.setCodigoUsuarioAbertura(this.usuario.getNuMatricula());
      this.remessa.setUnidadeSolicitante(unidadeServiceRef.findUnidadeLotacaoUsuarioLogado());
      this.remessa = remessaService.salvarRascunhoRemessaAB(this.remessa);
    }
  }
  
  public boolean isContemMascara(String mascara) {
    return (mascara.contains(".") || mascara.contains("/") || mascara.contains("-")) && !mascara.contains("0,00");
  }

  public boolean isMascaraNumerica(String mascara) {
    if (isContemMascara(mascara))
      return false;

    return mascara.matches("^[0-9]{1,}$");
  }

  public boolean isMascaraMoeda(String mascara) {
    return !isContemMascara(mascara) && mascara.contains("0,00");
  }

  public String calculaTamanhoCampo(String legenda, String descricao, int tamanhoCampo, SimNaoEnum obrigatorio) {
    int tamanhoLabel = (!Util.isNullOuVazio(legenda) ? legenda.length() : descricao.length());
    if (tamanhoLabel < tamanhoCampo) {
      return "width: " + (tamanhoCampo * 10) + "px";
    } else {
      return "width: " + (tamanhoLabel * 6) + "px";
    }
  }

  public List<GrupoCampoVO> getListGrupoCampo() {
    if (!ObjectUtils.isNullOrEmpty(grupo) && !ObjectUtils.isNullOrEmpty(grupo.getGrupoCampos())) {
      return CollectionUtils.asSortedList(grupo.getGrupoCampos());
    }
    return new ArrayList<GrupoCampoVO>();
  }

  private void salvarCamposDinamicos(RemessaDocumentoVO vo) throws AppException {
    if (grupo == null)
      return;

    try {
      GrupoCamposHelper.setValorCamposDinamicos(vo, grupo.getGrupoCampos());
    } catch (br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException e) {
      throw new AppException(e.getMessage(), e);
    }
  }
  
  public void validarCamposObrigatorios() throws RequiredException {
    List<String> camposObrigatorios = new ArrayList<String>();
    if (Util.isNullOuVazio(this.remessaDocumento.getUnidadeGeradora())) {
      camposObrigatorios.add(MensagemUtils.obterMensagem("MA001", "geral.label.unidadeGeradora"));
    }
    // Campos dinâmicos
    if (!ObjectUtils.isNullOrEmpty(this.grupo)) {
      for (GrupoCampoVO grupoCampo : grupo.getGrupoCampos()) {
        if (grupoCampo.getCampoObrigatorio().equals(SimNaoEnum.SIM)) {
          if (!grupoCampo.getCampo().getNome().equals("NU_OPERACAO_A11") && ObjectUtils.isNullOrEmpty(grupoCampo.getValor())
              && ObjectUtils.isNullOrEmpty(grupoCampo.getValorData())) {
            camposObrigatorios.add(MensagemUtils.obterMensagem("MA001",
                StringUtils.defaultIfEmpty(grupoCampo.getLegenda(), grupoCampo.getCampo().getDescricao())));
          }
        }
      }
    }

    if (!camposObrigatorios.isEmpty()) {
      throw new RequiredException(camposObrigatorios);
    }
  }
  
  private void redirectTelaPesquisaDocumento() {
      try {
        FacesContext.getCurrentInstance().getExternalContext().redirect("abertura");
      } catch (IOException e) {
      }
  }
  
  /* Getters and Setters*/

  public DocumentoVO getDocumento() {
    return documento;
  }

  public void setDocumento(DocumentoVO documento) {
    this.documento = documento;
  }

  public RemessaVO getRemessa() {
    return remessa;
  }

  public void setRemessa(RemessaVO remessa) {
    this.remessa = remessa;
  }

  public String getCodigoUnidadeFiltro() {
    return codigoUnidadeFiltro;
  }

  public void setCodigoUnidadeFiltro(String codigoUnidadeFiltro) {
    this.codigoUnidadeFiltro = codigoUnidadeFiltro;
  }

  public String getNomeUnidadeFiltro() {
    return nomeUnidadeFiltro;
  }

  public void setNomeUnidadeFiltro(String nomeUnidadeFiltro) {
    this.nomeUnidadeFiltro = nomeUnidadeFiltro;
  }

  public UsuarioLdap getUsuario() {
    return usuario;
  }

  public void setUsuario(UsuarioLdap usuario) {
    this.usuario = usuario;
  }

  public GrupoVO getGrupo() {
    return grupo;
  }

  public void setGrupo(GrupoVO grupo) {
    this.grupo = grupo;
  }

  public RemessaDocumentoVO getRemessaDocumento() {
    return remessaDocumento;
  }

  public void setRemessaDocumento(RemessaDocumentoVO remessaDocumento) {
    this.remessaDocumento = remessaDocumento;
  }

  public Boolean getModoEdicaoRemessaDocumento() {
    return modoEdicaoRemessaDocumento;
  }

  public void setModoEdicaoRemessaDocumento(Boolean modoEdicaoRemessaDocumento) {
    this.modoEdicaoRemessaDocumento = modoEdicaoRemessaDocumento;
  }

  public Boolean getModoNovaRemessaDocumento() {
    return modoNovaRemessaDocumento;
  }

  public void setModoNovaRemessaDocumento(Boolean modoNovaRemessaDocumento) {
    this.modoNovaRemessaDocumento = modoNovaRemessaDocumento;
  }

}
