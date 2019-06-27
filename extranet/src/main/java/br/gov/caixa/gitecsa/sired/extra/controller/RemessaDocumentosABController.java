package br.gov.caixa.gitecsa.sired.extra.controller;

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
import br.gov.caixa.gitecsa.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.service.RemessaService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.extra.service.SequencialRemessaService;
import br.gov.caixa.gitecsa.sired.extra.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
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
  private UnidadeService unidadeServiceRef;

  @Inject
  private GrupoService grupoService;

  @Inject
  private SequencialRemessaService sequencialRemessaService;

  private RemessaVO remessa;

  private DocumentoVO documento;
  
  private RemessaDocumentoVO remessaDocumento;
  
  private RemessaDocumentoVO remessaDocumentoLegado;

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
      this.remessaDocumentoLegado = (RemessaDocumentoVO) JSFUtil.getSessionMapValue("remessaDocumento");
      if (JSFUtil.getSessionMapValue("edicaoDocumentoRemessa") != null ) {
        edicaoRemessaDocumento();
      }
    } catch (Exception e) {
      this.logger.error(e.getMessage(), e);
    }
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
      persistirRemessaDocumentoAB();
      JSFUtil.setSessionMapValue("remessa", this.remessa);
      
     facesMessager.addMessageInfo(MensagemUtils.obterMensagem("remessa.tipoAB.alteracao.mensagem.sucesso"));
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
   * @throws Exception 
   */
  private void persistirRemessaDocumentoAB() throws Exception {
      RemessaDocumentoVO remessaDocumentoVO = new RemessaDocumentoVO();
        
      if(isAlteracao(remessaDocumentoVO, this.remessaDocumentoLegado) && remessaDocumentoLegado.getIcAlteracaoValida() != SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA) {
        remessaDocumentoVO.setCodigoRemessa(this.remessaDocumentoLegado.getCodigoRemessa());
        remessaDocumentoVO.setCodigoUsuarioUltimaAlteracao(JSFUtil.getUsuario().getEmail());
        remessaDocumentoVO.setRemessa(this.remessa);
        remessaDocumentoVO.setDocumento(this.documento);
        remessaDocumentoVO.setDataUltimaAlteracao(new Date());
        remessaDocumentoVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA);
        remessaDocumentoVO.setUnidadeGeradora(this.remessaDocumentoLegado.getUnidadeGeradora());
        remessaDocumentoVO = remessaDocumentoService.salvar(remessaDocumentoVO);
        remessaDocumentoVO.setNumeroRemessaTipoAB(this.remessaDocumentoLegado);
        this.salvarCamposDinamicos(remessaDocumentoVO);
        remessaDocumentoVO = remessaDocumentoService.salvar(remessaDocumentoVO);
        remessaDocumentoLegado.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO);
        remessaDocumentoLegado = remessaDocumentoService.salvar(remessaDocumentoLegado);
        if(!this.remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_ALTERACAO.getId())) {
          remessaService.emAlteracaoRemessaDocumento(this.remessa);
        }
      }else if(remessaDocumentoLegado.getIcAlteracaoValida() != SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO){
        this.remessaDocumento.setDataUltimaAlteracao(new Date());
        this.remessaDocumento = remessaDocumentoService.salvar(this.remessaDocumento);
        remessaDocumentoVO.setNumeroRemessaTipoAB(this.remessaDocumentoLegado);
        this.salvarCamposDinamicos(this.remessaDocumento);
        this.remessaDocumento = remessaDocumentoService.salvar(this.remessaDocumento);
      }
  }
  
  //Método que compara se os objetos possuem os mesmos atributos, caso contrario, é alteração
  public boolean isAlteracao(RemessaDocumentoVO remessaDocumentoVO, RemessaDocumentoVO remessaDocumentoLegado) throws AppException {
    GrupoCamposHelper.setValorCamposDinamicos(remessaDocumentoVO, grupo.getGrupoCampos());
    
    int contador = 0;
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getUnidadeGeradora())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getUnidadeGeradora())) {
      if (!remessaDocumentoVO.getUnidadeGeradora().equals(remessaDocumentoLegado.getUnidadeGeradora())) {
        contador++;
      }
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDescricaoLocalizacao())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDescricaoLocalizacao())) {
      if (!remessaDocumentoVO.getDescricaoLocalizacao().equals(remessaDocumentoLegado.getDescricaoLocalizacao())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDescricaoLocalizacao())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDescricaoLocalizacao())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDescricaoLocalizacao())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDescricaoLocalizacao()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuValor())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuValor())) {
      if (!remessaDocumentoVO.getNuValor().equals(remessaDocumentoLegado.getNuValor())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuValor())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuValor())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuValor())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuValor()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNoNome())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNoNome())) {
      if (!remessaDocumentoVO.getNoNome().equals(remessaDocumentoLegado.getNoNome())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNoNome())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNoNome())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNoNome())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNoNome()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuDocumento())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuDocumento())) {
      if (!remessaDocumentoVO.getNuDocumento().equals(remessaDocumentoLegado.getNuDocumento())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuDocumento())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuDocumento())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuDocumento())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuDocumento()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuVolume())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuVolume())) {
      if (!remessaDocumentoVO.getNuVolume().equals(remessaDocumentoLegado.getNuVolume())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuVolume())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuVolume())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuVolume())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuVolume()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuContaInicio())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuContaInicio())) {
      if (!remessaDocumentoVO.getNuContaInicio().equals(remessaDocumentoLegado.getNuContaInicio())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuContaInicio())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuContaInicio())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuContaInicio())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuContaInicio()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuContaFim())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuContaFim())) {
      if (!remessaDocumentoVO.getNuContaFim().equals(remessaDocumentoLegado.getNuContaFim())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuContaFim())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuContaFim())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuContaFim())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuContaFim()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuConta())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuConta())) {
      if (!remessaDocumentoVO.getNuConta().equals(remessaDocumentoLegado.getNuConta())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuConta())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuConta())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuConta())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuConta()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuOperacao())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuOperacao())) {
      if (!remessaDocumentoVO.getNuOperacao().equals(remessaDocumentoLegado.getNuOperacao())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuOperacao())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuOperacao())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuOperacao())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuOperacao()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuDigitoVerificador())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuDigitoVerificador())) {
      if (!remessaDocumentoVO.getNuDigitoVerificador().equals(remessaDocumentoLegado.getNuDigitoVerificador())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuDigitoVerificador())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuDigitoVerificador())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuDigitoVerificador())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuDigitoVerificador()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataGeracao())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataGeracao())) {
      if (!remessaDocumentoVO.getDataGeracao().equals(remessaDocumentoLegado.getDataGeracao())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataGeracao())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataGeracao())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataGeracao())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataGeracao()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataInicio())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataInicio())) {
      if (!remessaDocumentoVO.getDataInicio().equals(remessaDocumentoLegado.getDataInicio())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataInicio())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataInicio())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataInicio())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataInicio()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataFim())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataFim())) {
      if (!remessaDocumentoVO.getDataFim().equals(remessaDocumentoLegado.getDataFim())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataFim())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataFim())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataFim())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataFim()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getDataUltimaAlteracao())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getDataUltimaAlteracao())) {
      if (!remessaDocumentoVO.getDataUltimaAlteracao().equals(remessaDocumentoLegado.getDataUltimaAlteracao())) {
        contador++;
      }
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuEncerramento())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuEncerramento())) {
      if (!remessaDocumentoVO.getNuEncerramento().equals(remessaDocumentoLegado.getNuEncerramento())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuEncerramento())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuEncerramento())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNuEncerramento())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNuEncerramento()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroCnpj())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroCnpj())) {
      if (!remessaDocumentoVO.getNumeroCnpj().equals(remessaDocumentoLegado.getNumeroCnpj())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroCnpj())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroCnpj())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroCnpj())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroCnpj()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroCpf())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroCpf())) {
      if (!remessaDocumentoVO.getNumeroCpf().equals(remessaDocumentoLegado.getNumeroCpf())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroCpf())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroCpf())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroCpf())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroCpf()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNomeInicio())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNomeInicio())) {
      if (!remessaDocumentoVO.getNomeInicio().equals(remessaDocumentoLegado.getNomeInicio())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNomeInicio())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNomeInicio())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNomeInicio())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNomeInicio()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNomeFim())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNomeFim())) {
      if (!remessaDocumentoVO.getNomeFim().equals(remessaDocumentoLegado.getNomeFim())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNomeFim())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNomeFim())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNomeFim())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNomeFim()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroInicio())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroInicio())) {
      if (!remessaDocumentoVO.getNumeroInicio().equals(remessaDocumentoLegado.getNumeroInicio())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroInicio())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroInicio())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroInicio())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroInicio()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroFim())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroFim())) {
      if (!remessaDocumentoVO.getNumeroFim().equals(remessaDocumentoLegado.getNumeroFim())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroFim())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroFim())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroFim())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroFim()))) {
      contador++;
    }
    
    if (!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroFolder())
        && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroFolder())) {
      if (!remessaDocumentoVO.getNumeroFolder().equals(remessaDocumentoLegado.getNumeroFolder())) {
        contador++;
      }
    }if((!ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroFolder())
        && ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroFolder())) || 
        (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getNumeroFolder())
            && !ObjectUtils.isNullOrEmpty(remessaDocumentoLegado.getNumeroFolder()))) {
      contador++;
    }
    
    if(contador > 0) {
      return true;
    }else {
      return false;
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

  public boolean ehMascaraMoeda(String mascara) {
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
