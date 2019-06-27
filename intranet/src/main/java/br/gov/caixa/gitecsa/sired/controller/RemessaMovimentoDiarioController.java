package br.gov.caixa.gitecsa.sired.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.context.RequestContext;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoLotericoEnum;
import br.gov.caixa.gitecsa.sired.service.DocumentoService;
import br.gov.caixa.gitecsa.sired.service.FeriadoService;
import br.gov.caixa.gitecsa.sired.service.RemessaMovimentoDiarioService;
import br.gov.caixa.gitecsa.sired.service.RemessaService;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class RemessaMovimentoDiarioController implements Serializable {

  /** Serial. */
  private static final long serialVersionUID = 8390317751198699423L;
  
  @Inject
  protected FacesMensager facesMessager;

  @Inject
  protected transient Logger logger;
  
  @Inject
  private UnidadeService unidadeService;

  @Inject
  private DocumentoService documentoService;
  
  @Inject
  private br.gov.caixa.gitecsa.sired.service.UnidadeService unidadeServiceRef;

  @Inject
  private RemessaService remessaService;
  
  @Inject
  private FeriadoService feriadoService;

  @Inject
  private RemessaMovimentoDiarioService remessaMovimentoDiarioService;
  
  private RemessaMovimentoDiarioVO movimentoDiario;
  
  private RemessaVO remessa;

  private DocumentoVO documento;
  
  private String codigoUnidadeFiltro;

  private String nomeUnidadeFiltro;

  private String lacreFiltro;
  
  private Date dataMovimento;
  
  private UsuarioLdap usuario;
  
  private UnidadeVO unidadeSolicitante;

  private UnidadeVO unidadeGeradora;
  
  private List<RemessaMovimentoDiarioVO> itensMovimentoDiarioList;
  
  private Boolean modoEdicao;
  
  private Boolean modoIncluirNovoMovDiario;

  @PostConstruct
  protected void init() {
    this.limparCampos();
    try {
      this.usuario = (UsuarioLdap) JSFUtil.getSessionMapValue("usuario");
      /** Edição de remessa da tela de rascunho. */
      if (JSFUtil.getSessionMapValue("remessa") != null && JSFUtil.getSessionMapValue("itemRemessa") != null) {
        carregarDadosModoEdicaoRemessa();
      } else if (JSFUtil.getSessionMapValue("remessa") != null) {      
        carregarDadosModoNovoMovimentoRemessaExistente();
      } else {      
        carregarDadosModoNovaRemessa();
      }
      JSFUtil.setSessionMapValue("remessa",null);
      JSFUtil.setSessionMapValue("itemRemessa",null);
    } catch (DataBaseException e) {
      this.logger.error(e.getMessage(), e);
    }
  }

  private void carregarDadosModoNovoMovimentoRemessaExistente() throws DataBaseException {
    this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
    this.documento = this.remessa.getDocumento();
    this.unidadeSolicitante = this.remessa.getUnidadeSolicitante();
    this.unidadeGeradora = unidadeServiceRef.findUnidadeLotacaoUsuarioLogado();
    this.montarListaDeItensMovimentosDiarios();
    this.movimentoDiario = new RemessaMovimentoDiarioVO();
    this.definirUnidadeGeradoraPadrao();
    this.modoIncluirNovoMovDiario = Boolean.TRUE;
  }

  private void carregarDadosModoNovaRemessa() throws DataBaseException {
    /** Nova remessa. */
    this.documento = documentoService.buscarMovimentoDiario();
    this.unidadeSolicitante = unidadeServiceRef.findUnidadeLotacaoUsuarioLogado();
    this.unidadeGeradora = unidadeServiceRef.findUnidadeLotacaoUsuarioLogado();
    this.montarListaDeItensMovimentosDiarios();
    this.remessa = new RemessaVO();
    this.movimentoDiario = new RemessaMovimentoDiarioVO();
    this.definirUnidadeGeradoraPadrao();
  }

  private void carregarDadosModoEdicaoRemessa() {
    this.modoEdicao = Boolean.TRUE;
    RemessaMovimentoDiarioVO itemRemessa = (RemessaMovimentoDiarioVO) JSFUtil.getSessionMapValue("itemRemessa");
    this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
    this.remessa = remessaService.obterRemessaComMovimentosDiarios( (Long) this.remessa.getId());
    this.documento = this.remessa.getDocumento();
    this.unidadeSolicitante = this.remessa.getUnidadeSolicitante();
    this.unidadeGeradora = itemRemessa.getUnidadeGeradora();
    this.dataMovimento = itemRemessa.getDataMovimento();
    this.movimentoDiario = itemRemessa;
    this.codigoUnidadeFiltro = itemRemessa.getUnidadeGeradora().getId().toString();
    this.nomeUnidadeFiltro = itemRemessa.getUnidadeGeradora().getNome();
    this.montarListaDeItensMovimentosDiariosParaEdicao(this.dataMovimento);
  }

  private void montarListaDeItensMovimentosDiariosParaEdicao(Date dataMovimento) {
    this.itensMovimentoDiarioList = new ArrayList<>();
    for (RemessaMovimentoDiarioVO item : this.remessa.getMovimentosDiarioList()) {
      Hibernate.initialize(item);
      if (item.getDataMovimento().equals(dataMovimento) && item.getUnidadeGeradora().getId().equals(this.unidadeGeradora.getId())) {        
        this.itensMovimentoDiarioList.add(item);
      }
    }   
  }

  private void montarListaDeItensMovimentosDiarios() {
    this.itensMovimentoDiarioList = new ArrayList<>();
    RemessaMovimentoDiarioVO temp;
    for (int i = 1; i < 11; i++) {
      temp = new RemessaMovimentoDiarioVO();
      temp.setNuItem(i);
      this.itensMovimentoDiarioList.add(temp);
    }
  }

  private void limparCampos() {
    this.movimentoDiario = null;
    this.dataMovimento = null;
    this.codigoUnidadeFiltro = null;
    this.nomeUnidadeFiltro = null;
    this.remessa = null;
    this.documento = null;
    this.itensMovimentoDiarioList = new ArrayList<>();
    this.modoEdicao = Boolean.FALSE;
    this.modoIncluirNovoMovDiario = Boolean.FALSE;
  }

  public void pesquisarUnidadeGeradora() {
    try {
      if (StringUtils.isBlank(codigoUnidadeFiltro)) {
        this.movimentoDiario.setUnidadeGeradora(null);
        this.nomeUnidadeFiltro = "";
      } else {
        UnidadeVO unidadeFiltro = new UnidadeVO();
        unidadeFiltro.setId(Long.parseLong(codigoUnidadeFiltro));
        List<UnidadeVO> unidades = unidadeService.findByParameters(unidadeFiltro);
        
        if (!Util.isNullOuVazio(unidades)) {
          this.movimentoDiario.setUnidadeGeradora(unidades.get(0));
          this.nomeUnidadeFiltro = this.movimentoDiario.getUnidadeGeradora().getNome().trim();
        } else {
          this.nomeUnidadeFiltro = "";
          this.movimentoDiario.setUnidadeGeradora(null);
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
        }
      }
      this.unidadeGeradora = this.movimentoDiario.getUnidadeGeradora();
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "RemessaController", "pesquisarUnidadeGeradora"));
    }
  }
  
  public String salvar() {
    try {
      if (validarCampos()) {
        persistirRemessaC17();
        /* Persistir as filhas preenchidas da C21*/
        List<RemessaMovimentoDiarioVO> lista = new ArrayList<>();
        Integer count = 1;
        for (RemessaMovimentoDiarioVO item : this.itensMovimentoDiarioList) {
          if (item.getGrupo1() != null) {
            item.setNuItem(count);
            lista.add(item);
            count++;
          }
        }
        if (!lista.isEmpty()) {          
          for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : lista) {
            persistirItensRemessaDiarioC21(remessaMovimentoDiarioVO);
          }
        }
      } else {
        return null;
      }
      JSFUtil.setSessionMapValue("remessa", this.remessa);
      return "/paginas/remessa/rascunhoRemessaTipoC.xhtml?faces-redirect=true";
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "RemessaMovimentoDiarioController", "Salvar"));
      return null;
    }
  }
  
  private void persistirItensRemessaDiarioC21(RemessaMovimentoDiarioVO remessaMovimentoDiarioVO) {
    remessaMovimentoDiarioVO.setDataMovimento(this.dataMovimento);
    remessaMovimentoDiarioVO.setCodigoUsuarioUltimaAlteracao(this.usuario.getNuMatricula());
    remessaMovimentoDiarioVO.setDataHoraUltimaAlteracao(new Date());
    remessaMovimentoDiarioVO.setRemessa(this.remessa);
    remessaMovimentoDiarioVO.setUnidadeGeradora(this.unidadeGeradora);
    remessaMovimentoDiarioVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
    remessaMovimentoDiarioService.salvarItemMovimentoDiario(remessaMovimentoDiarioVO);
  }

  /** 
   * Persistir a Remessa C17.
   */
  private void persistirRemessaC17() throws Exception {
    if (this.remessa == null || this.remessa.getId() == null) {      
      this.remessa = new RemessaVO();
      this.remessa.setDataHoraAbertura(new Date());
      this.remessa.setDocumento(this.documento);
      this.remessa.setCodigoUsuarioAbertura(this.usuario.getNuMatricula());
      this.remessa.setUnidadeSolicitante(this.unidadeSolicitante);
      this.remessa = remessaService.salvarRascunho(this.remessa);
    }
  }
  
  private boolean validarCampos() throws Exception {
    if (ObjectUtils.isNullOrEmpty(this.unidadeGeradora)) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.unidadeGeradora"));
      return false;
    }
    if (ObjectUtils.isNullOrEmpty(this.nomeUnidadeFiltro)) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.unidadeGeradora"));
      return false;
    }
    if (this.dataMovimento == null) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "remessa.filtro.dataGeracao"));
      return false;
    }
    if (!feriadoService.isDataUtil(this.dataMovimento, this.unidadeGeradora)) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("remessa.mensagemErro.diaUtil"));
      return false;      
    }
    
    if (this.dataMovimento.after(new Date())) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("remessa.mensagemErro.dataSuperioAtual"));
      return false;      
    }
    if (this.remessa.getMovimentosDiarioList() != null && !this.modoEdicao) {      
      for (RemessaMovimentoDiarioVO mov : this.remessa.getMovimentosDiarioList()) {
        if (mov.getDataMovimento().equals(this.dataMovimento) && mov.getUnidadeGeradora().getId().equals(this.unidadeGeradora.getId())) {
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MA062"));
          return false;      
        }
      }
    }
    if (ObjectUtils.isNullOrEmpty(this.unidadeGeradora)) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.unidadeGeradora"));
      return false;
    }
    if (ObjectUtils.isNullOrEmpty(this.codigoUnidadeFiltro)) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.unidadeGeradora"));
      return false;
    }
    if (ObjectUtils.isNullOrEmpty(this.nomeUnidadeFiltro)) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.unidadeGeradora"));
      return false;
    }
    for (RemessaMovimentoDiarioVO item : this.itensMovimentoDiarioList) {
      if (!ObjectUtils.isNullOrEmpty(item.getIcLoterico()) &&  !ObjectUtils.isNullOrEmpty(item.getNuTerminal()) && !ObjectUtils.isNullOrEmpty(item.getGrupo1()) && 
          !ObjectUtils.isNullOrEmpty(item.getGrupo2()) && !ObjectUtils.isNullOrEmpty(item.getGrupo3())) {
        continue;        
      } else if (ObjectUtils.isNullOrEmpty(item.getNuTerminal()) && ObjectUtils.isNullOrEmpty(item.getGrupo1()) && 
                  ObjectUtils.isNullOrEmpty(item.getGrupo2()) && ObjectUtils.isNullOrEmpty(item.getGrupo3())) {
        continue;
      } else if ( !ObjectUtils.isNullOrEmpty(item.getNuTerminal()) || !ObjectUtils.isNullOrEmpty(item.getGrupo1()) || 
                  !ObjectUtils.isNullOrEmpty(item.getGrupo2()) || !ObjectUtils.isNullOrEmpty(item.getGrupo3())) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("ME031"));
        return false;
      }
    }
    return true;
  }

  /**
   * Define a unidade geradora (unidade filtro e instância) como a unidade do usuário logado. Sempre que for necessário
   * resetar a unidade, deve-se chamar este método.
   */
  public void definirUnidadeGeradoraPadrao() {
    try {
      UnidadeVO unidade = consultarUnidadeLotacao();
      this.codigoUnidadeFiltro = unidade.getId().toString();
      this.nomeUnidadeFiltro = unidade.getNome();
      this.movimentoDiario.setUnidadeGeradora(unidade);
    } catch (AppException e) {
      facesMessager.addMessageError(e.getMessage());
      logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), "RequisicaoController","definirUnidadeGeradoraPadrao"));
    }
  }

  public void adicionarTerminal() {
    RemessaMovimentoDiarioVO temp = new RemessaMovimentoDiarioVO();
    temp.setNuItem(this.itensMovimentoDiarioList.size() + 1);
    this.itensMovimentoDiarioList.add(temp);
  }
  
  public UnidadeVO consultarUnidadeLotacao() throws AppException {
    UnidadeVO unidadeLotacao = null;
    UnidadeVO filtro = new UnidadeVO();
    UsuarioLdap usuario = (UsuarioLdap) JSFUtil.getSessionMapValue("usuario");
    filtro.setId(usuario.getCoUnidade().longValue());
    unidadeLotacao = unidadeService.carregarLazyPropertiesUnidade(filtro);
    if (Util.isNullOuVazio(unidadeLotacao)) {
      throw new AppException();
    }
    return unidadeLotacao;
  }

  /**
   * Mostra o componente do widgetvar especificado
   * @param widgetvar
   */
  protected void showDialog(String widgetvar) {
    RequestContext.getCurrentInstance().execute(widgetvar + ".show()");
  }

  public int sortByString(Object msg1, Object msg2) {
    return ((String) msg1).compareToIgnoreCase(((String) msg2));
  }
  
  public List<TipoLotericoEnum> getTipoLotericoList() {
    List<TipoLotericoEnum> retorno = new ArrayList<>();
    for (int i = 0; i < TipoLotericoEnum.values().length; i++) {
      retorno.add(TipoLotericoEnum.values() [i]);
    }    
    return retorno;
  }
  
  public String cancelar() {
    if (this.modoEdicao || this.modoIncluirNovoMovDiario) {
      JSFUtil.setSessionMapValue("remessa", this.remessa);
      return "/paginas/remessa/rascunhoRemessaTipoC.xhtml?faces-redirect=true";
    } else {      
      return "/paginas/remessa/abertura.xhtml?faces-redirect=true";
    }
  }

  /**
   * Carga de Ajax.
   * @throws Exception - Exception.
   */
  public void prepareToResult() throws Exception {
    
  }
  
  public void validarDataMovimentoExiste() throws Exception {
    if (this.remessa.getId() != null) {
      this.remessa = remessaService.obterRemessaComMovimentosDiarios((Long) this.remessa.getId());
      for (RemessaMovimentoDiarioVO mov : this.remessa.getMovimentosDiarioList()) {
        if (mov.getDataMovimento().equals(this.dataMovimento) && mov.getUnidadeGeradora().getId().equals(this.unidadeGeradora.getId())) {
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MA062"));
        }
      }
    }
  }
  
  /**
   * Método do remote command para auxiliar na validação do produto DLE Comp.
   */
  public void alterarNumeroTerminal() {
    try {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      String linha = params.get("linha");
      String numeroTerminal = params.get("numeroTerminal");
      Long valor = Long.parseLong(numeroTerminal);
      
      if (linha != null && numeroTerminal != null) {
        if (StringUtils.isBlank(numeroTerminal)) {
          this.itensMovimentoDiarioList.get(Integer.parseInt(linha)).setNuTerminal(null);
        } else {
          this.itensMovimentoDiarioList.get(Integer.parseInt(linha)).setNuTerminal(valor);        
        }
      }      
    } catch (NumberFormatException e) {
      facesMessager.addMessageError("Só é permitido preenchimento de caracteres numéricos");
    }
  }
  
  /**
   * Método do remote command alterar os campos devido ao bug do JSF que não limpa o campo no Bean.
   */
  public void rmcAlterarGrupo1() {
    try {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      String linha = params.get("linha");
      String grupo = params.get("grupo");
      if (linha != null && grupo != null) {
        if (StringUtils.isBlank(grupo)) {
          this.itensMovimentoDiarioList.get(Integer.parseInt(linha)).setGrupo1(null);
        } else {
          this.itensMovimentoDiarioList.get(Integer.parseInt(linha)).setGrupo1(Integer.parseInt(grupo));        
        }
      }
    } catch (NumberFormatException e) {
      facesMessager.addMessageError("Só é permitido preenchimento de caracteres numéricos");
    }
  }
  
  /**
   * Método do remote command alterar os campos devido ao bug do JSF que não limpa o campo no Bean.
   */
  public void rmcAlterarGrupo2() {
    try {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      String linha = params.get("linha");
      String grupo = params.get("grupo");
      if (linha != null && grupo != null) {
        if (StringUtils.isBlank(grupo)) {
          this.itensMovimentoDiarioList.get(Integer.parseInt(linha)).setGrupo2(null);
        } else {
          this.itensMovimentoDiarioList.get(Integer.parseInt(linha)).setGrupo2(Integer.parseInt(grupo));        
        }
      }
    } catch (NumberFormatException e) {
      facesMessager.addMessageError("Só é permitido preenchimento de caracteres numéricos");
    }
  }
  
  /**
   * Método do remote command alterar os campos devido ao bug do JSF que não limpa o campo no Bean.
   */
  public void rmcAlterarGrupo3() {
    try {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      String linha = params.get("linha");
      String grupo = params.get("grupo");
      if (linha != null && grupo != null) {
        if (StringUtils.isBlank(grupo)) {
          this.itensMovimentoDiarioList.get(Integer.parseInt(linha)).setGrupo3(null);
        } else {
          this.itensMovimentoDiarioList.get(Integer.parseInt(linha)).setGrupo3(Integer.parseInt(grupo));        
        }
      }
    } catch (NumberFormatException e) {
      facesMessager.addMessageError("Só é permitido preenchimento de caracteres numéricos");
    }
  }
  
  /**
   * @return the codigoUnidadeFiltro
   */
  public String getCodigoUnidadeFiltro() {
    return codigoUnidadeFiltro;
  }

  /**
   * @param codigoUnidadeFiltro the codigoUnidadeFiltro to set
   */
  public void setCodigoUnidadeFiltro(String codigoUnidadeFiltro) {
    this.codigoUnidadeFiltro = codigoUnidadeFiltro;
  }

  /**
   * @return the nomeUnidadeFiltro
   */
  public String getNomeUnidadeFiltro() {
    return nomeUnidadeFiltro;
  }

  /**
   * @param nomeUnidadeFiltro the nomeUnidadeFiltro to set
   */
  public void setNomeUnidadeFiltro(String nomeUnidadeFiltro) {
    this.nomeUnidadeFiltro = nomeUnidadeFiltro;
  }

  /**
   * @return the remessa
   */
  public RemessaVO getRemessa() {
    return remessa;
  }

  /**
   * @param remessa the remessa to set
   */
  public void setRemessa(RemessaVO remessa) {
    this.remessa = remessa;
  }

  /**
   * @return the documento
   */
  public DocumentoVO getDocumento() {
    return documento;
  }

  /**
   * @param documento the documento to set
   */
  public void setDocumento(DocumentoVO documento) {
    this.documento = documento;
  }

  /**
   * @return the usuario
   */
  public UsuarioLdap getUsuario() {
    return usuario;
  }

  /**
   * @param usuario the usuario to set
   */
  public void setUsuario(UsuarioLdap usuario) {
    this.usuario = usuario;
  }

  /**
   * @return the itensMovimentoDiarioList
   */
  public List<RemessaMovimentoDiarioVO> getItensMovimentoDiarioList() {
    return itensMovimentoDiarioList;
  }

  /**
   * @param itensMovimentoDiarioList the itensMovimentoDiarioList to set
   */
  public void setItensMovimentoDiarioList(List<RemessaMovimentoDiarioVO> itensMovimentoDiarioList) {
    this.itensMovimentoDiarioList = itensMovimentoDiarioList;
  }

  /**
   * @return the lacreFiltro
   */
  public String getLacreFiltro() {
    return lacreFiltro;
  }

  /**
   * @param lacreFiltro the lacreFiltro to set
   */
  public void setLacreFiltro(String lacreFiltro) {
    this.lacreFiltro = lacreFiltro;
  }

  /**
   * @return the movimentoDiario
   */
  public RemessaMovimentoDiarioVO getMovimentoDiario() {
    return movimentoDiario;
  }

  /**
   * @param movimentoDiario the movimentoDiario to set
   */
  public void setMovimentoDiario(RemessaMovimentoDiarioVO movimentoDiario) {
    this.movimentoDiario = movimentoDiario;
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

  /**
   * @return the dataMovimento
   */
  public Date getDataMovimento() {
    return dataMovimento;
  }

  /**
   * @param dataMovimento the dataMovimento to set
   */
  public void setDataMovimento(Date dataMovimento) {
    this.dataMovimento = dataMovimento;
  }

  /**
   * @return the modoEdicao
   */
  public Boolean getModoEdicao() {
    return modoEdicao;
  }

  /**
   * @param modoEdicao the modoEdicao to set
   */
  public void setModoEdicao(Boolean modoEdicao) {
    this.modoEdicao = modoEdicao;
  }

  /**
   * @return the unidadeGeradora
   */
  public UnidadeVO getUnidadeGeradora() {
    return unidadeGeradora;
  }

  /**
   * @param unidadeGeradora the unidadeGeradora to set
   */
  public void setUnidadeGeradora(UnidadeVO unidadeGeradora) {
    this.unidadeGeradora = unidadeGeradora;
  }

  /**
   * @return the modoIncluirNovoMovDiario
   */
  public Boolean getModoIncluirNovoMovDiario() {
    return modoIncluirNovoMovDiario;
  }

  /**
   * @param modoIncluirNovoMovDiario the modoIncluirNovoMovDiario to set
   */
  public void setModoIncluirNovoMovDiario(Boolean modoIncluirNovoMovDiario) {
    this.modoIncluirNovoMovDiario = modoIncluirNovoMovDiario;
  }
 
}
