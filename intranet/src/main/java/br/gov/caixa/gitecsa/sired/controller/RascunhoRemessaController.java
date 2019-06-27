package br.gov.caixa.gitecsa.sired.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.context.RequestContext;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.service.RemessaMovimentoDiarioService;
import br.gov.caixa.gitecsa.sired.service.RemessaService;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class RascunhoRemessaController implements Serializable {

  /** Serial. */
  private static final long serialVersionUID = 5732371421970887959L;

  @Inject
  protected FacesMensager facesMessager;

  @Inject
  protected transient Logger logger;

  @Inject
  private UnidadeService unidadeService;

  @Inject
  private RemessaService remessaService;

  @Inject
  private br.gov.caixa.gitecsa.sired.service.UnidadeService unidadeServiceRef;

  @Inject
  private RemessaMovimentoDiarioService remessaMovimentoDiarioService;

  private RemessaVO remessa;

  private RemessaMovimentoDiarioVO movimentoDiario;

  private UnidadeVO unidadeGeradora;

  private String codigoUnidadeFiltro;

  private String nomeUnidadeFiltro;

  private List<MovimentoDiarioRemessaCDTO> dataMovimentosList;

  @PostConstruct
  protected void init() {
    try {
      this.limparCampos();
      if (JSFUtil.getSessionMapValue("remessa") != null) {
        this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
        this.remessa = remessaService.obterRemessaComMovimentosDiarios((Long) this.remessa.getId());
        this.unidadeGeradora = this.remessa.getUnidadeSolicitante();
        this.carregarAgrupamentoMovimentosPorDia();
        JSFUtil.setSessionMapValue("remessa", null);
      } else {
        FacesContext.getCurrentInstance().getExternalContext().redirect("mantem-remessa");
      }
      this.definirUnidadeGeradoraPadrao();
      this.unidadeGeradora = unidadeServiceRef.findUnidadeLotacaoUsuarioLogado();
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
    }
  }

  private void carregarAgrupamentoMovimentosPorDia() {
    this.dataMovimentosList = remessaService.obterAgrupamentoDeItensDeRemessaPorDiaUnidade(this.remessa);
  }

  private void limparCampos() {
    this.movimentoDiario = null;
    this.codigoUnidadeFiltro = null;
    this.nomeUnidadeFiltro = null;
    this.remessa = null;
    this.dataMovimentosList = new ArrayList<>();
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
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
        }
      }
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "RemessaController", "pesquisarUnidadeGeradora"));
    }
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
    } catch (AppException e) {
      facesMessager.addMessageError(e.getMessage());
      logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), "RequisicaoController",
          "definirUnidadeGeradoraPadrao"));
    }
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

  public String incluirMovimento() {
    JSFUtil.setSessionMapValue("remessa", this.remessa);
    return "/paginas/remessa/aberturaMovDiario.xhtml?faces-redirect=true";
  }

  public String excluirItemRemessa(RemessaMovimentoDiarioVO item) {
    this.movimentoDiario = item;
    return null;
  }

  public String confirmarExclusaoItemRemessa() {
    remessaMovimentoDiarioService.excluirItemMovimentoDiarioPorId(this.movimentoDiario);
    List<RemessaMovimentoDiarioVO> listMovimentoDiario = this.remessa.getMovimentosDiarioList();
    Iterator<RemessaMovimentoDiarioVO> iterator = listMovimentoDiario.iterator();
    while (iterator.hasNext()) {
      RemessaMovimentoDiarioVO movimentoDiario = iterator.next();
      if (this.movimentoDiario.getId().equals(movimentoDiario.getId())) {
        iterator.remove();
        movimentoDiario.setRemessa(null);
      }
    }
    carregarAgrupamentoMovimentosPorDia();
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS016"));
    return null;
  }

  public String cancelarExclusaoItemRemessa() {
    this.movimentoDiario = null;
    return null;
  }

  public String salvarRascunho() {
    try {
      if (this.remessa.getLacre() == null) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.lacreFiltro"));
        return null;
      }
      recarregarListadeRemessasParaPersistir();
      remessaService.atualizarRascunhoRemessa(this.remessa);
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS046"));
      return null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
      return null;
    }
  }

  private void recarregarListadeRemessasParaPersistir() {
    Long lacre = this.remessa.getLacre();
    this.remessa = remessaService.obterRemessaComMovimentosDiarios((Long) this.remessa.getId());
    this.remessa.setLacre(lacre);
  }

  public String prepareConcluirRemessa() {
    try {
      if (this.remessa.getLacre() == null) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.lacreFiltro"));
        return null;
      }
      recarregarListadeRemessasParaPersistir();
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
      if (this.remessa.getMovimentosDiarioList().isEmpty()) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA064"));
        return null;
      }
      recarregarListadeRemessasParaPersistir();
      remessaService.concluirRemessaMovimentoDiarioTipoC(this.remessa);
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS028", this.remessa.getId()));
      return "/paginas/remessa/mantem-remessa.xhtml?faces-redirect=true";
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
      return null;
    }
  }

  public String editarRemessa(MovimentoDiarioRemessaCDTO itemRemessaDTO) {
    JSFUtil.setSessionMapValue("remessa", this.remessa);
    JSFUtil.setSessionMapValue("itemRemessa", itemRemessaDTO.getRemessaMovDiarioList().get(0));
    return "/paginas/remessa/aberturaMovDiario.xhtml?faces-redirect=true";
  }

  public String obterNomeUnidadeComData(MovimentoDiarioRemessaCDTO itemRemessaDTO) {
    String titulo = "";
    UnidadeVO unidade = unidadeServiceRef.findById(itemRemessaDTO.getUnidade());
    titulo += unidade.getDescricaoCompleta();
    titulo += " - DATA MOVIMENTO: " + itemRemessaDTO.getDataFormatada();
    return titulo;
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
   * @return the dataMovimentosList
   */
  public List<MovimentoDiarioRemessaCDTO> getDataMovimentosList() {
    return dataMovimentosList;
  }

  /**
   * @param dataMovimentosList the dataMovimentosList to set
   */
  public void setDataMovimentosList(List<MovimentoDiarioRemessaCDTO> dataMovimentosList) {
    this.dataMovimentosList = dataMovimentosList;
  }

  public boolean isRascunho() {
    TramiteRemessaVO tramiteRemessaAtual = new TramiteRemessaVO();
    tramiteRemessaAtual = this.remessa.getTramiteRemessaAtual();
    SituacaoRemessaVO situacaoAtual = tramiteRemessaAtual.getSituacao();

    if (situacaoAtual.getId().equals(1L)) {
      return true;
    }
    return false;
  }

  public boolean isCorrecao() {
    TramiteRemessaVO tramiteRemessaAtual = new TramiteRemessaVO();
    tramiteRemessaAtual = this.remessa.getTramiteRemessaAtual();
    SituacaoRemessaVO situacaoAtual = tramiteRemessaAtual.getSituacao();

    if (situacaoAtual.getId().equals(8L)) {
      return true;
    }
    return false;
  }

}
