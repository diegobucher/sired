package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.component.datalist.DataList;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsulta;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.GrupoService;
import br.gov.caixa.gitecsa.service.RemessaService;
import br.gov.caixa.gitecsa.service.TramiteRemessaService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.comparator.RemessaDocDiarioNumItemComparator;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.exporter.ExportarRemessaXLS;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.sired.service.RemessaMovimentoDiarioService;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.JavaScriptUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class MantemRemessaController extends BaseConsulta<RemessaVO> {

  private static final long serialVersionUID = -4717183403767743995L;

  @Inject
  private UnidadeService unidadeService;

  @Inject
  private BaseService baseService;

  @Inject
  private RemessaService remessaService;

  @Inject
  private TramiteRemessaService tramiteRemessaService;

  @Inject
  private br.gov.caixa.gitecsa.sired.service.RemessaService refcRemessaService;

  @Inject
  private GrupoService grupoService;

  @Inject
  private RemessaDocumentoService remessaDocumentoService;

  @Inject
  private RemessaMovimentoDiarioService remessaMovimentoDiarioService;

  private Long nuRemessa;

  private String coUsuarioFiltro;

  private Date dataInicioFiltro = Util.getDataMesPassado();

  private Date dataFimFiltro;

  private UnidadeVO unidadeFiltro;

  private String codigoUnidadeFiltro;

  private String nomeUnidadeFiltro;

  private SituacaoRemessaEnum situacaoFiltro;

  private String filtroHistorico;

  private BaseVO baseFiltro;

  private RemessaVO remessa;

  private RemessaDocumentoVO remessaDocumento;

  private TramiteRemessaVO tramiteRemessaVO;

  private List<RequisicaoVO> listaFiltro;

  private List<BaseVO> listaBase;

  private GrupoVO grupo;

  private GrupoVO grupoOriginal;

  private int anoFragmentacao;

  private DocumentoVO documento;

  private StreamedContent termoResponsabilidade;

  private StreamedContent capaLote;

  private LazyDataModel<RemessaVO> listaRemessaModel;

  private LazyDataModel<RemessaVO> listaRemessaModelTratada;

  private List<TramiteRemessaVO> listaTramitesRemessa;

  private List<TramiteRemessaVO> filtredListaTramitesRemessa;

  private boolean pesquisaSucesso;

  private boolean pesquisaPorNumeroRemessa = false;

  private RemessaMovimentoDiarioVO itemDetalheTipoC;

  private MovimentoDiarioRemessaCDTO itemDetalheTipoCCDTO;

  private List<RemessaDocumentoVO> listaDocumentos;

  private boolean flagExibeBotaoConfirmarAlteracoesTipoC;

  private boolean flagExibeBotaoEmDisputaTipoC;

  private boolean flagExibeBotaoDesfazerAlteracoesTipoC;

  private boolean flagExibeBotaoInvalidarAlteracoesTipoC;

  private String[] listaAcaoRemessa;

  private String acaoRemessa;
  
  private StreamedContent etiquetaMovDiario;

  private boolean quantitativoZerado = false;

  @PostConstruct
  protected void init() throws AppException {
    try {
      UsuarioLdap usuario = JSFUtil.getUsuario();

      if (usuario != null) {
        coUsuarioFiltro = usuario.getNuMatricula();

        filtroHistorico = StringUtils.EMPTY;

        listaBase = baseService.findAll();

        initUnidade();

        initTable();
      }
    } catch (Exception e) {
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError("MA012");
    }
  }

  private void initUnidade() {
    UsuarioLdap usuario = (UsuarioLdap) JSFUtil.getSessionMapValue("usuario");
    if (!Util.isNullOuVazio(usuario)) {
      codigoUnidadeFiltro = usuario.getCoUnidade().toString();
      nomeUnidadeFiltro = usuario.getNoUnidade();

      unidadeFiltro = new UnidadeVO();
      unidadeFiltro.setId(Long.valueOf(usuario.getCoUnidade()));
      unidadeFiltro.setNome(usuario.getNoUnidade());
    }
  }

  private void initTable() {
    listaRemessaModel = new LazyDataModel<RemessaVO>() {

      private static final long serialVersionUID = 3528646700234313883L;

      @Override
      public List<RemessaVO> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        try {
          List<RemessaVO> list = pesquisar(first, pageSize);
          if (!Util.isNullOuVazio(list)) {
            setRowCount(countConsultaRemessa());
            pesquisaSucesso = Boolean.TRUE;
            return list;
          } else {
            setRowCount(0);
            pesquisaSucesso = Boolean.TRUE;
            return null;
          }
        } catch (AppException e) {
          pesquisaSucesso = Boolean.FALSE;
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
          logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "load"));
        }

        return null;
      }
    };
  }

  private int countConsultaRemessa() throws AppException {
    return remessaService.countConsultarRemessa(getNuRemessa(), getDataInicioFiltro(), getDataFimFiltro(), getCoUsuarioFiltro(),
        getUnidadeFiltro(), getSituacaoFiltro(), getBaseFiltro());
  }

  private List<RemessaVO> pesquisar(int first, int pageSize) throws AppException {
    List<RemessaVO> listaRetorno = new ArrayList<>();
    listaRetorno =
        remessaService.consultarRemessa(getNuRemessa(), getDataInicioFiltro(), getDataFimFiltro(), getCoUsuarioFiltro(),
            getUnidadeFiltro(), getSituacaoFiltro(), getBaseFiltro(), first, pageSize);

    for (RemessaVO remessaVO : listaRetorno) {
      if (remessaVO.getTipoRemessaMoviMentoDiario()) {
        remessaVO.setDataMovimentosList(refcRemessaService.obterAgrupamentoDeItensDeRemessaPorDiaUnidade(remessaVO));
      }
    }

    return listaRetorno;
  }

  public void handleNumeroRemessa() {
    if (nuRemessa != null && nuRemessa > 0L) {
      this.coUsuarioFiltro = null;
      this.dataInicioFiltro = null;
      this.dataFimFiltro = null;
      this.unidadeFiltro = null;
      this.codigoUnidadeFiltro = null;
      this.nomeUnidadeFiltro = null;
      this.baseFiltro = null;
      this.situacaoFiltro = SituacaoRemessaEnum.TODAS;
      updateComponentes("pnlCamposFiltro");
    }
  }

  public void localizar() {
    if (!validarParametrosDeConsulta()) {
      initTable();
      updateComponentes("formConsulta");
      updateComponentes("pnlAccordion");
      updateComponentes("tabela");
      return;
    }

    DataList dataList = (DataList) FacesContext.getCurrentInstance().getViewRoot().findComponent("formConsulta:tabela");
    dataList.setFirst(0);
    listaRemessaModel.load(0, 1, null, null, null);
    // setListaRemessaModelTratada(exibeListaDocumentosTratados(listaRemessaModel));

    if (listaRemessaModel.getRowCount() <= 0 && pesquisaSucesso) {
      JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA010"));
    } else {
      updateComponentes("formConsulta");
    }
  }

  @Override
  public void limparFiltro() {
    this.nuRemessa = null;
    this.coUsuarioFiltro = JSFUtil.getUsuario().getNuMatricula();
    this.dataInicioFiltro = Util.getDataMesPassado();
    this.dataFimFiltro = null;
    this.situacaoFiltro = null;
    this.baseFiltro = null;

    initUnidade();
    initTable();
  }

  private boolean validarParametrosDeConsulta() {

    if (Util.isNullOuVazio(nuRemessa)) {

      if (Util.isNullOuVazio(dataInicioFiltro)) {
        super.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.dataInicio"));
        return false;
      }

      if (Util.isNullOuVazio(dataFimFiltro) && !Util.isNullOuVazio(dataInicioFiltro)) {
        dataFimFiltro = Calendar.getInstance().getTime();
      }

      if (dataInicioFiltro.after(dataFimFiltro)) {
        super.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA018"));
        return false;
      }
    }

    return true;
  }

  public void pesquisarUnidadeSolicitante() {
    if (!Util.isNullOuVazio(codigoUnidadeFiltro)) {
      try {
        unidadeFiltro = new UnidadeVO();
        unidadeFiltro.setId(Long.parseLong(codigoUnidadeFiltro));
        List<UnidadeVO> unidades = unidadeService.findByParameters(unidadeFiltro);

        if (!Util.isNullOuVazio(unidades)) {
          unidadeFiltro = unidades.get(0);
          nomeUnidadeFiltro = unidadeFiltro.getNome().trim();
        } else {
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
        }

      } catch (Exception e) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
        logger
            .error(LogUtils.getMensagemPadraoLog(e.getMessage(), "ConsultaRequisicaoController", "pesquisarUnidadeSolicitante"));
      }

    } else {
      unidadeFiltro = null;
      nomeUnidadeFiltro = "";
    }

  }

  /**
   * Prepara os objetos necessários para o modal de bloqueio de uma remessa ABERTA.
   * @param remessa
   */
  public void preparaBloqueioRemessa(RemessaVO remessa) {
    this.remessa = remessa;
    tramiteRemessaVO = new TramiteRemessaVO();
    tramiteRemessaVO.setRemessa(remessa);
    tramiteRemessaVO.setSituacao(new SituacaoRemessaVO());
    tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.BLOQUEADA.getId());
    tramiteRemessaVO.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());
    showDialog("modalBloqueio");
  }

  /**
   * Salva o trâmite da remessa com situação BLOQUEADA e atualiza o trâmite atual da remessa para o trâmite salvo no
   * banco de dados.
   */
  public void gravarBloqueioRemessa() {
    try {

      if (validarMotivoBloqueio()) {

        tramiteRemessaVO.setDataTramiteRemessa(new Date());
        tramiteRemessaService.save(tramiteRemessaVO);
        remessa.setTramiteRemessaAtual(tramiteRemessaVO);
        remessaService.saveOrUpdate(remessa);
        hideDialog("modalBloqueio");
        facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS037", remessa.getId().toString()));
        updateComponentes("formConsulta");
      }
    } catch (AppException e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "gravarBloqueioRemessa"));
    }

  }

  private boolean validarMotivoBloqueio() {

    Integer maxLength = 200;

    if (tramiteRemessaVO.getObservacao().length() > maxLength) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA055", "Motivo Bloqueio", maxLength.toString()));
      return false;
    }

    return true;
  }

  /**
   * Prepara os objetos necessários para o modal de desbloqueio de uma remessa BLOQUEADA.
   * @param remessa
   */
  public void preparaDesbloqueioRemessa(RemessaVO remessa) {
    this.remessa = remessa;
    tramiteRemessaVO = new TramiteRemessaVO();
    tramiteRemessaVO = remessa.getTramiteRemessaAtual();
    showDialog("modalDesbloqueio");
  }

  public void prepararAcao(RemessaVO remessa) {

    this.remessa = remessa;
    acaoRemessa = null;
    tramiteRemessaVO = new TramiteRemessaVO();
    tramiteRemessaVO.setDataTramiteRemessa(new Date());
    tramiteRemessaVO.setRemessa(remessa);
    tramiteRemessaVO.setSituacao(new SituacaoRemessaVO());
    tramiteRemessaVO.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());

    if (remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.ALTERADA.getId())) {
      String[] listaAcao = { Constantes.CONFIRMAR_ALTERACAO, Constantes.EM_DISPUTA };
      listaAcaoRemessa = listaAcao;
    } else {
      String[] listaAcao = { Constantes.CONFIRMAR_ALTERACAO, Constantes.DESFAZER_ALTERACAO, Constantes.INVALIDAR_REMESSA };
      listaAcaoRemessa = listaAcao;
    }
    showDialog("modalAcoesRemessa");

  }

  public void definirSituacaoTramite() throws Exception {
		try {
			if (Util.isNullOuVazio(acaoRemessa)) {
				super.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.acao"));
			} else {
				if (acaoRemessa.equals(Constantes.CONFIRMAR_ALTERACAO)) {
					if (!ObjectUtils.isNullOrEmpty(this.remessa.getCodigoRemessaTipoC())) {
						tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.ALTERACAO_CONFIRMADA.getId());
						confirmarAlteracoesTipoC();
					} else {
						tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.ALTERACAO_CONFIRMADA.getId());
						confirmarAlteracoes(this.remessa);
					}
				} else if (acaoRemessa.equals(Constantes.EM_DISPUTA)) {
					tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.EM_DISPUTA.getId());
					confirmarEmDisputa();
				} else if (acaoRemessa.equals(Constantes.DESFAZER_ALTERACAO)) {
					tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.ALTERACAO_DESFEITA.getId());
					if (!ObjectUtils.isNullOrEmpty(this.remessa.getCodigoRemessaTipoC())) {
						desfazerAlteracoesTipoC();
					} else {
						desfazerAlteracoes();
					}
				} else if (acaoRemessa.equals(Constantes.INVALIDAR_REMESSA)) {
					tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.INVALIDA.getId());
					invalidarAlteracoes();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
  }

  /**
   * Salva o trâmite da remessa com situação ABERTA após o usuário acionar o botão DESBLOQUEAR Remessa e atualiza o
   * trâmite atual da remessa para o trâmite salvo no banco de dados.
   */
  public void gravarDesbloqueioRemessa() {
    try {
      TramiteRemessaVO tramite = new TramiteRemessaVO();
      tramite.setRemessa(remessa);
      tramite.setSituacao(new SituacaoRemessaVO());
      tramite.getSituacao().setId(SituacaoRemessaEnum.ABERTA.getId());
      tramite.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());
      tramite.setDataTramiteRemessa(new Date());
      tramiteRemessaService.save(tramite);
      remessa.setTramiteRemessaAtual(tramite);
      remessaService.saveOrUpdate(remessa);
      hideDialog("modalDesbloqueio");
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS038", remessa.getId().toString()));
      updateComponentes("formConsulta");
    } catch (AppException e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "gravarBloqueioRemessa"));
    }
  }

  /**
   * Prepara os objetos necessários para o modal de desbloqueio de uma remessa INCONSISTENTE.
   * @param remessa
   */
  public void preparaFechamentoRemessaInvalida(RemessaVO remessa) {
    this.remessa = remessa;
    tramiteRemessaVO = new TramiteRemessaVO();
    tramiteRemessaVO = remessa.getTramiteRemessaAtual();
    showDialog("modalFechamento");
  }

  /**
   * Salva o trâmite da remessa com situação FECHADA INCONSISTENTE após o usuário acionar o botão Fevhar Remessa e
   * atualiza o trâmite atual da remessa para o trâmite salvo no banco de dados.
   */
  public void gravarFechamentoRemessaInconsistente() {
    try {
      TramiteRemessaVO tramite = new TramiteRemessaVO();
      tramite.setRemessa(remessa);
      tramite.setSituacao(new SituacaoRemessaVO());
      tramite.getSituacao().setId(SituacaoRemessaEnum.FECHADA_INCONSISTENTE.getId());
      tramite.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());
      tramite.setDataTramiteRemessa(new Date());
      tramiteRemessaService.save(tramite);
      remessa.setTramiteRemessaAtual(tramite);
      remessaService.saveOrUpdate(remessa);
      hideDialog("modalFechamento");
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS039", remessa.getId().toString()));
      updateComponentes("formConsulta");
    } catch (AppException e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "gravarFechamentoRemessaInconsistente"));
    }

  }

  /**
   * Prepara os objetos necessários para o modal de correção de uma remessa DEVOLVIDA.
   * @param remessa
   */
  public void preparaCorrecaoRemessa(RemessaVO remessa) {
    this.remessa = remessa;
    tramiteRemessaVO = new TramiteRemessaVO();
    tramiteRemessaVO = remessa.getTramiteRemessaAtual();
    showDialog("modalCorrecao");
  }

  /**
   * @param remessaVO
   */
  public void corrigirRemessa() {
    try {
      RequestUtils.setSessionValue("remessa", this.remessa);
      if (this.remessa.getTipoRemessaMoviMentoDiario()) {
        FacesContext.getCurrentInstance().getExternalContext().redirect("rascunhoRemessaTipoC");
      } else {
        FacesContext.getCurrentInstance().getExternalContext().redirect("rascunhoRemessaTipoAB");
      }
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "corrigirRemessa"));
    }
  }

  public void preparaClonarRemessa(RemessaVO remessa) {
    this.remessa = remessa;
    JavaScriptUtils.showModal("mdlClonar");
  }

  public void clonarRemessa() {
    try {
      RemessaVO remessaClonada = refcRemessaService.clonar(remessa);
      RequestUtils.setSessionValue("remessa", remessaClonada);
      if (remessaClonada.getTipoRemessaMoviMentoDiario()) {
        FacesContext.getCurrentInstance().getExternalContext().redirect("rascunhoRemessaTipoC");
      } else {
        FacesContext.getCurrentInstance().getExternalContext().redirect("rascunhoRemessaTipoAB");
      }

    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "clonarRemessa"));
    }
  }

  /**
   * Prepara remessa e redireciona sistema para a pagina de Rascunho de Remesssa para edição da mesma.
   * @param remessaVO
   */
  public void editarRemessa(RemessaVO remessaVO) {
    try {
      JSFUtil.setSessionMapValue("remessa", remessaVO);
      JSFUtil.setSessionMapValue("origem", "manutencao");
      if (remessaVO.getTipoRemessaMoviMentoDiario()) {
        FacesContext.getCurrentInstance().getExternalContext().redirect("rascunhoRemessaTipoC");
      } else {
        FacesContext.getCurrentInstance().getExternalContext().redirect("rascunhoRemessaTipoAB");
      }
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "editarRemessa"));
    }
  }

  public void visualizarRemessaDocumento(RemessaDocumentoVO remessaDocumentoVO) {

    try {
      this.remessaDocumento = remessaDocumentoVO;
      this.remessa = remessaDocumentoVO.getRemessa();
      grupo = grupoService.obterGrupo(remessaDocumento.getDocumento());

      if (grupo != null && (this.remessaDocumento.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)
          || this.remessaDocumento.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA))) {
        recuperarCamposDinamicos(remessaDocumento);
        showDialog("modalRemessaDocumento");
      } else if (grupo != null && this.remessaDocumento.getIcAlteracaoValida()
          .equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
        grupoOriginal = grupoService.obterGrupo(remessaDocumento.getDocumento());
        recuperarCamposDinamicos(this.remessaDocumento);
        recuperarCamposDinamicosOriginais(this.remessaDocumento.getNumeroRemessaTipoAB());
        validarValoresIguais();
        showDialog("modalRemessaDocumentoAlteracao");
      } else {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      }

    } catch (AppException e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "visualizarRemessaDocumento"));
    }

  }

  public void visualizarRemessaDocumentoMovimentoDiario(MovimentoDiarioRemessaCDTO mov) {
    this.itemDetalheTipoCCDTO = mov;
    RemessaVO remessaVO = new RemessaVO();
    List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioList = this.itemDetalheTipoCCDTO.getRemessaMovDiarioList();
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioList) {
      if (remessaMovimentoDiarioVO.getId() != null) {
        remessaVO = remessaMovimentoDiarioVO.getRemessa();
        if (remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.ALTERADA.getId())
            || remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId())) {
          exibirBotaoConfirmarAlteracoesTipoC(remessaVO);
          exibirBotaoEmDisputaTipoC(remessaVO);
          exibirBotaoDesfazerAlteracoesTipoC(remessaVO);
          exibirBotaoInvalidarAlteracoesTipoC(remessaVO);
          verificaValoresAlteradosTipoC();
          showDialog("modalDetalharRemessaDocumentoAlteracao");
          break;
        }
        if (remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)
            || remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
          carregarListaMovDiarioTratado();
          showDialog("modalDetalharRemessaDocumento");
          break;
        } else {
          carregarListaMovDiarioTratado();
          showDialog("modalDetalharRemessaDocumento");
          break;
        }
      }
    }
  }

  private List<RemessaMovimentoDiarioVO> carregarListaMovDiarioTratado() {
    List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioListTratada = new ArrayList<RemessaMovimentoDiarioVO>();
    List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioListAux = new ArrayList<RemessaMovimentoDiarioVO>();
    if (this.itemDetalheTipoCCDTO != null) {
      List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioList = this.itemDetalheTipoCCDTO.getRemessaMovDiarioList();
      List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioSubstituido = new ArrayList<RemessaMovimentoDiarioVO>();
      remessaMovimentoDiarioListTratada.addAll(remessaMovimentoDiarioList);
      remessaMovimentoDiarioListTratada.addAll(remessaMovimentoDiarioSubstituido);
      for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioListTratada) {
        if (remessaMovimentoDiarioVO.getId() != null) {
          if (remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)
              || remessaMovimentoDiarioVO.getIcAlteracaoValida()
                  .equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA) || 
                  remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
            remessaMovimentoDiarioListAux.add(remessaMovimentoDiarioVO);
          }
        }
      }
    }
    return remessaMovimentoDiarioListAux;
  }
  
  public List<RemessaMovimentoDiarioVO> carregarListasAlteracaoSubstituido() {
    if (this.itemDetalheTipoCCDTO != null) {
      List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioList = this.itemDetalheTipoCCDTO.getRemessaMovDiarioList();
      List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioSubstituido = new ArrayList<RemessaMovimentoDiarioVO>();
      for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioList) {
        if (remessaMovimentoDiarioVO.getId() != null) {
          if (remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)
              || remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO) || 
              remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
            remessaMovimentoDiarioSubstituido.add(remessaMovimentoDiarioVO);
          }
        }
      }
      Collections.sort(remessaMovimentoDiarioSubstituido, new RemessaDocDiarioNumItemComparator());
      return remessaMovimentoDiarioSubstituido;
    }
    return null;
  }

  public List<RemessaMovimentoDiarioVO> carregarListasAlteracaoAlterado() {
	  
	this.quantitativoZerado = false;
	  
    if (this.itemDetalheTipoCCDTO != null) {
      List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioList = this.itemDetalheTipoCCDTO.getRemessaMovDiarioList();
      List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioSubstituido = new ArrayList<RemessaMovimentoDiarioVO>();
      for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioList) {
        if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
            .equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
          remessaMovimentoDiarioSubstituido.add(remessaMovimentoDiarioVO);
          
          this.identificarQuantitativoZerado(remessaMovimentoDiarioVO);
        }
      }
      Collections.sort(remessaMovimentoDiarioSubstituido, new RemessaDocDiarioNumItemComparator());
      return remessaMovimentoDiarioSubstituido;
    }
    return null;
  }
  
  private void identificarQuantitativoZerado(final RemessaMovimentoDiarioVO itemRemessa) {
		
		if (! this.quantitativoZerado) {
			
			if(itemRemessa.isFlagValorDiferenteNuTerminal() && itemRemessa.getNuTerminal() == 0L) {
				this.quantitativoZerado = true;
			} else if (itemRemessa.isFlagValorDiferenteIcGrupo1() && itemRemessa.getGrupo1() == 0) {
				this.quantitativoZerado = true;
			} else if (itemRemessa.isFlagValorDiferenteIcGrupo2() && itemRemessa.getGrupo2() == 0) {
				this.quantitativoZerado = true;
			} else if (itemRemessa.isFlagValorDiferenteIcGrupo3() && itemRemessa.getGrupo3() == 0) {
				this.quantitativoZerado = true;
			}
		}
	}

  public void verificaValoresAlteradosTipoC() {
    List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioSubstituido = carregarListasAlteracaoSubstituido();
    List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioAlterado = carregarListasAlteracaoAlterado();
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVOAlterado : remessaMovimentoDiarioAlterado) {
      for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVOSubstituido : remessaMovimentoDiarioSubstituido) {
        if (remessaMovimentoDiarioVOSubstituido.getId()
            .equals(remessaMovimentoDiarioVOAlterado.getNumeroRemessaTipoC().getId())) {
          if (!remessaMovimentoDiarioVOAlterado.getIcLoterico().equals(remessaMovimentoDiarioVOSubstituido.getIcLoterico())) {
            remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteIcLoterico(true);
          }
          if (!remessaMovimentoDiarioVOAlterado.getNuTerminal().equals(remessaMovimentoDiarioVOSubstituido.getNuTerminal())) {
            remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteNuTerminal(true);
          }
          if (!remessaMovimentoDiarioVOAlterado.getGrupo1().equals(remessaMovimentoDiarioVOSubstituido.getGrupo1())) {
            remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteIcGrupo1(true);
          }
          if (!remessaMovimentoDiarioVOAlterado.getGrupo2().equals(remessaMovimentoDiarioVOSubstituido.getGrupo2())) {
            remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteIcGrupo2(true);
          }
          if (!remessaMovimentoDiarioVOAlterado.getGrupo3().equals(remessaMovimentoDiarioVOSubstituido.getGrupo3())) {
            remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteIcGrupo3(true);
          }
        }
      }
    }
  }
  

  private void recuperarCamposDinamicos(RemessaDocumentoVO vo) throws AppException {
    if (grupo == null) {
      return;
    }

    try {
      GrupoCamposHelper.getValorCamposDinamicos(vo, grupo.getGrupoCampos());
    } catch (br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException e) {
      throw new AppException(e.getMessage(), e);
    }
  }

  private void recuperarCamposDinamicosOriginais(RemessaDocumentoVO vo) throws AppException {
    if (grupoOriginal == null) {
      return;
    }

    try {
      GrupoCamposHelper.getValorCamposDinamicos(vo, grupoOriginal.getGrupoCampos());
    } catch (br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException e) {
      throw new AppException(e.getMessage(), e);
    }
  }

  /**
   * Retorna <code>true</code> caso a situação da Remessa seja passível de Geração do Termo de Responsabilidade,
   * <code>false</code> caso contrário.
   * @param remessa
   * @return <code>true</code> caso a situação da Remessa seja passível de Geração do Termo de Responsabilidade,
   *         <code>false</code> caso contrário.
   */
  public boolean exibirTermoResponsabilidade(RemessaVO remessa) {
    return Util.exibirTermoResponsabilidadeNaRemessa(remessa);
  }

  /**
   * Retorna <code>true</code> caso a situação da Remessa seja passível de Geração da Caoa de Lote, <code>false</code>
   * caso contrário.
   * @param remessa
   * @return <code>true</code> caso a situação da Remessa seja passível de Geração da Caoa de Lote, <code>false</code>
   *         caso contrário.
   */
  public boolean exibirCapaLote(RemessaVO remessa) {
    return Util.exibirCapaLoteNaRemessa(remessa);
  }

  public boolean exibirEditarRemessa(RemessaVO remessa) {

    SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
    if (situacao.getId().equals(SituacaoRemessaEnum.RASCUNHO.getId())) {
      return true;
    }
    return false;
  }

  public boolean exibirBotaoAlteracao(RemessaDocumentoVO remessaDocumentoVO) {
    if (remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
      return true;
    }
    return false;
  }

  public boolean exibirBotaoAlteracaoTipoC(MovimentoDiarioRemessaCDTO mov) {
    List<RemessaMovimentoDiarioVO> listaMovimentos = new ArrayList<RemessaMovimentoDiarioVO>();
    listaMovimentos.addAll(mov.getRemessaMovDiarioList());
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : listaMovimentos) {
      if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
          .equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean exibirBotaoAdicaoTipoC(MovimentoDiarioRemessaCDTO mov) {
    List<RemessaMovimentoDiarioVO> listaMovimentos = new ArrayList<RemessaMovimentoDiarioVO>();
    listaMovimentos.addAll(mov.getRemessaMovDiarioList());
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : listaMovimentos) {
      if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
          .equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
        return true;
      }
    }
    return false;
  }

  public boolean exibirDemaisSituacoesRemessa(RemessaVO remessa) {

    SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
    if (situacao.getId().equals(SituacaoRemessaEnum.AGENDADA.getId())
        || situacao.getId().equals(SituacaoRemessaEnum.CONFERIDA.getId())
        || situacao.getId().equals(SituacaoRemessaEnum.FECHADA.getId())
        || situacao.getId().equals(SituacaoRemessaEnum.FECHADA_INCONSISTENTE.getId())
        || situacao.getId().equals(SituacaoRemessaEnum.RASCUNHO.getId())
        || situacao.getId().equals(SituacaoRemessaEnum.RECEBIDA.getId())
        || situacao.getId().equals(SituacaoRemessaEnum.EM_ALTERACAO.getId())
        || situacao.getId().equals(SituacaoRemessaEnum.ALTERACAO_CONFIRMADA.getId())
        || situacao.getId().equals(SituacaoRemessaEnum.ALTERACAO_DESFEITA.getId())) {
      return true;
    }
    return false;
  }

  public boolean exibirSituacaoAberta(RemessaVO remessa) {

    SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
    if (situacao.getId().equals(SituacaoRemessaEnum.ABERTA.getId())) {
      return true;
    }
    return false;
  }

  public boolean exibirSituacaoAlterada(RemessaVO remessa) {

    SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
    if (situacao.getId().equals(SituacaoRemessaEnum.ALTERADA.getId())) {
      return true;
    }
    return false;
  }

  public boolean exibirSituacaoEmDisputaUsuarioComum(RemessaVO remessa) {

    SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
    if (situacao.getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId()) && !getPerfilGestor()) {
      return true;
    }
    return false;
  }

  public boolean exibirSituacaoEmDisputa(RemessaVO remessa) {

    SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
    if (situacao.getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId()) && getPerfilGestor()) {
      return true;
    }
    return false;
  }

  public boolean exibirSituacaoBloqueada(RemessaVO remessa) {

    SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
    if (situacao.getId().equals(SituacaoRemessaEnum.BLOQUEADA.getId())) {
      return true;
    }
    return false;
  }

  public boolean exibirSituacaoInvalida(RemessaVO remessa) {

    SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
    if (situacao.getId().equals(SituacaoRemessaEnum.INVALIDA.getId())) {
      return true;
    }
    return false;
  }

  public String calculaTamanhoCampo(String legenda, String descricao, int tamanhoCampo, SimNaoEnum obrigatorio) {
    int tamanhoLabel = (!Util.isNullOuVazio(legenda) ? legenda.length() : descricao.length());
    if (tamanhoLabel < tamanhoCampo) {
      return "width: " + (tamanhoCampo * 10) + "px";
    } else {
      return "width: " + (tamanhoLabel * 3) + "px";
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

  public void verHistoricoRemessa(RemessaVO remessaVO) {
    try {
      this.remessa = remessaVO;
      TramiteRemessaVO tramite = new TramiteRemessaVO();
      tramite.setRemessa(remessaVO);
      this.listaTramitesRemessa = new ArrayList<TramiteRemessaVO>();
      this.listaTramitesRemessa.addAll(tramiteRemessaService.findByRemessa(remessa));
      this.filtredListaTramitesRemessa = this.listaTramitesRemessa;
      filtroHistorico = "";
      showDialog("modalHistorico");
    } catch (AppException e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "verHistoricoRemessa"));
    }
  }

  public void limparDetalheMovimentotipoC() {
    this.itemDetalheTipoC = null;
  }

  /**
   * Exporta o Relatorio De Acordo com o nome Informado
   * @param document
   */
  public void exportarDatagrid() {
    HttpServletResponse res = JSFUtil.getServletResponse();
    res.setContentType("application/vnd.ms-excel");
    res.setHeader("Content-disposition",
        "attachment; filename=RelatorioRemessa_" + Util.formatDataHoraNomeArquivo(new Date()) + ".xls");
    try {
      ServletOutputStream outputStream = res.getOutputStream();

      List<RemessaVO> listaRemessa = pesquisar(0, 0);
      
      ExportarRemessaXLS exportador = new ExportarRemessaXLS();
      exportador.exportarTabelaXLS(outputStream, listaRemessa);
      outputStream.flush();
      outputStream.close();
      FacesContext faces = FacesContext.getCurrentInstance();

      faces.responseComplete();
    } catch (Exception e) {
      RequestContext.getCurrentInstance().execute("hideStatus();");
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "exportarDatagrid"));
    }

  }

  public SituacaoRemessaEnum[] getSituacoesRemessaFiltro() {
    return SituacaoRemessaEnum.valuesSituacaoOrdenados();
  }

  /**
   * Verifica se o perfil do usuário logado é Geral
   */
  public boolean isUsuarioPerfilGeral() {
    if (JSFUtil.isPerfil("G")) {
      return true;
    }
    return false;
  }

  /**
   * Verifica se o perfil do usuário logado é Gestor
   */
  public Boolean getPerfilGestor() {

    if (JSFUtil.isPerfil("GEST")) {
      return true;
    }
    return false;
  }

  /**
   * Método de gegração da Etiqueta de remessa de movimento Diário.
   * @return Stream
   */
  public StreamedContent gerarTermoResponsabilidade(RemessaVO remessa) {
    try {
      return refcRemessaService.gerarTermoResponsabilidade(remessa);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Método de gegração da Etiqueta de remessa de movimento Diário.
   * @return Stream
   */
  public StreamedContent gerarEtiquetaMovimentoDiario(RemessaVO remessa) {
    try {
     this.etiquetaMovDiario = refcRemessaService.imprimirEtiquetaMovDiario(remessa);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return this.etiquetaMovDiario;
  }

  /**
   * Método de gegração da Etiqueta de remessa de movimento Diário.
   * @return Stream
   */
  public StreamedContent gerarEtiquetaDocumentoAB(RemessaVO remessa) {
    try {
      return refcRemessaService.gerarEtiquetaDocumentoAB(remessa);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Método de gegração da Etiqueta de remessa de movimento Diário.
   * @return Stream
   */
  public StreamedContent gerarEtiquetaDocumentoABIndividual(RemessaDocumentoVO docAB) {
    try {
      return refcRemessaService.gerarEtiquetaDocumentoABIndividual(docAB);
    } catch (Exception e) {
      return null;
    }
  }

  public List<GrupoCampoVO> getListGrupoCampo() {
    if (!ObjectUtils.isNullOrEmpty(grupo) && !ObjectUtils.isNullOrEmpty(grupo.getGrupoCampos())) {
      return CollectionUtils.asSortedList(grupo.getGrupoCampos());
    }

    return new ArrayList<GrupoCampoVO>();
  }

  public List<GrupoCampoVO> getListGrupoCampoOriginal() {
    if (!ObjectUtils.isNullOrEmpty(grupoOriginal) && !ObjectUtils.isNullOrEmpty(grupoOriginal.getGrupoCampos())) {
      return CollectionUtils.asSortedList(grupoOriginal.getGrupoCampos());
    }

    return new ArrayList<GrupoCampoVO>();
  }

  public void validarValoresIguais() {

    List<GrupoCampoVO> gruposOriginais = new ArrayList<GrupoCampoVO>();
    gruposOriginais.addAll(grupoOriginal.getGrupoCampos());
    List<GrupoCampoVO> gruposAlterados = new ArrayList<GrupoCampoVO>();
    gruposAlterados.addAll(grupo.getGrupoCampos());

    for (GrupoCampoVO grupoCampoOriginal : gruposOriginais) {
      for (GrupoCampoVO grupoCampoAlterado : gruposAlterados) {
        if (grupoCampoOriginal.getCampo().equals(grupoCampoAlterado.getCampo())) {
          if (!ObjectUtils.isNullOrEmpty(grupoCampoOriginal.getValor())
              && !ObjectUtils.isNullOrEmpty(grupoCampoAlterado.getValor())) {
            if (!grupoCampoOriginal.getValor().equals(grupoCampoAlterado.getValor())) {
              grupoCampoAlterado.setValoresDiferentes(true);
            }
          } else if ((!ObjectUtils.isNullOrEmpty(grupoCampoOriginal.getValor())
              && ObjectUtils.isNullOrEmpty(grupoCampoAlterado.getValor()))
              || (ObjectUtils.isNullOrEmpty(grupoCampoOriginal.getValor())
                  && !ObjectUtils.isNullOrEmpty(grupoCampoAlterado.getValor()))) {
            grupoCampoAlterado.setValoresDiferentes(true);
          }
          else if ((!ObjectUtils.isNullOrEmpty(grupoCampoOriginal.getValorData())
              && !ObjectUtils.isNullOrEmpty(grupoCampoAlterado.getValorData()))) {
            if (!grupoCampoOriginal.getValorData().equals(grupoCampoAlterado.getValorData())) {
              grupoCampoAlterado.setValoresDiferentes(true);
            }
          }
        }
      }
    }
  }

  public void confirmarAlteracoes(RemessaVO remessaVO) throws AppException {
    List<RemessaDocumentoVO> remessaDocumentoList = remessaVO.getRemessaDocumentos();
    for (RemessaDocumentoVO remessaDocumentoVO : remessaDocumentoList) {
      if (remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
        remessaDocumentoVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
        remessaDocumentoService.salvar(remessaDocumentoVO);
      }
    }
    tramiteRemessaService.save(tramiteRemessaVO);
    this.remessa.setTramiteRemessaAtual(tramiteRemessaVO);
    remessaService.update(this.remessa);
    updateComponentes("formConsulta");
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS047", this.remessa.getId()));
  }

  public void confirmarAlteracoesTipoC() throws AppException {
    List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioAlterado = new ArrayList<RemessaMovimentoDiarioVO>();
    List<MovimentoDiarioRemessaCDTO> listaRemessaDTO = this.remessa.getDataMovimentosList();
    for (MovimentoDiarioRemessaCDTO movimentoDiarioRemessaCDTO : listaRemessaDTO) {
      remessaMovimentoDiarioAlterado.addAll(movimentoDiarioRemessaCDTO.getRemessaMovDiarioList());
    }
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioAlterado) {
      if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
          .equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA) || 
          remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
        remessaMovimentoDiarioVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
        remessaMovimentoDiarioService.salvarItemMovimentoDiario(remessaMovimentoDiarioVO);
      }
    }
    TramiteRemessaVO tramiteRemessa = tramiteRemessaService.save(this.tramiteRemessaVO);
    this.remessa.setTramiteRemessaAtual(tramiteRemessa);
    remessaService.update(this.remessa);
    updateComponentes("formConsulta");
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS047", this.remessa.getId()));
  }

  public void confirmarEmDisputa() throws AppException {
    TramiteRemessaVO tramiteRemessa = tramiteRemessaService.save(this.tramiteRemessaVO);
    this.remessa.setTramiteRemessaAtual(tramiteRemessa);
    remessaService.update(this.remessa);
    updateComponentes("formConsulta");
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS047", this.remessa.getId()));
  }

  public void desfazerAlteracoes() throws AppException {
    List<RemessaDocumentoVO> listaMovimentos = this.remessa.getRemessaDocumentos();
    for (RemessaDocumentoVO remessaDocumentoVO : listaMovimentos) {
      if (remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
        remessaDocumentoVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.ALTERACAO_BASE_ARQUIVO);
        remessaDocumentoService.salvar(remessaDocumentoVO);
      }
      if (remessaDocumentoVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
        remessaDocumentoVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
        remessaDocumentoService.salvar(remessaDocumentoVO);
      }
    }
    TramiteRemessaVO tramiteRemessa = tramiteRemessaService.save(this.tramiteRemessaVO);
    this.remessa.setTramiteRemessaAtual(tramiteRemessa);
    remessaService.update(this.remessa);
    updateComponentes("formConsulta");
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS047", this.remessa.getId()));
  }

  public void desfazerAlteracoesTipoC() throws AppException {
    List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioAlterado = new ArrayList<RemessaMovimentoDiarioVO>();
    List<MovimentoDiarioRemessaCDTO> listaRemessaDTO = this.remessa.getDataMovimentosList();
    for (MovimentoDiarioRemessaCDTO movimentoDiarioRemessaCDTO : listaRemessaDTO) {
      remessaMovimentoDiarioAlterado.addAll(movimentoDiarioRemessaCDTO.getRemessaMovDiarioList());
    }
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioAlterado) {
      if (remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
        remessaMovimentoDiarioVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
        remessaMovimentoDiarioService.salvarItemMovimentoDiario(remessaMovimentoDiarioVO);
      }
      if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
          .equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA) || 
          remessaMovimentoDiarioVO.getIcAlteracaoValida()
          .equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
        remessaMovimentoDiarioVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.ALTERACAO_BASE_ARQUIVO);
        remessaMovimentoDiarioService.salvarItemMovimentoDiario(remessaMovimentoDiarioVO);
      }
    }
    TramiteRemessaVO tramiteRemessa = tramiteRemessaService.save(this.tramiteRemessaVO);
    this.remessa.setTramiteRemessaAtual(tramiteRemessa);
    remessaService.update(this.remessa);
    updateComponentes("formConsulta");
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS047", this.remessa.getId()));
  }

  public void invalidarAlteracoes() throws AppException {
    TramiteRemessaVO tramiteRemessa = tramiteRemessaService.save(this.tramiteRemessaVO);
    this.remessa.setTramiteRemessaAtual(tramiteRemessa);
    remessaService.update(this.remessa);
    updateComponentes("formConsulta");
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS047", this.remessa.getId()));
  }

  public boolean exibirBotaoConfirmarAlteracoes() {
    if (this.remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.ALTERADA.getId())) {
      return true;
    } else if (this.remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId())
        && getPerfilGestor()) {
      return true;
    }
    return false;
  }

  public void exibirBotaoConfirmarAlteracoesTipoC(RemessaVO remessaVO) {
    if (remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.ALTERADA.getId())) {
      flagExibeBotaoConfirmarAlteracoesTipoC = true;
    } else if (remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId())
        && getPerfilGestor()) {
      flagExibeBotaoConfirmarAlteracoesTipoC = true;
    } else {
      flagExibeBotaoConfirmarAlteracoesTipoC = false;
    }
  }
  
  public Boolean exibirMensagemInclusaoTipoC() {
    if(this.itemDetalheTipoCCDTO != null) {
      List<RemessaMovimentoDiarioVO> remessaMovDiarioList =  this.itemDetalheTipoCCDTO.getRemessaMovDiarioList();
      
      for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovDiarioList) {
        if(remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean exibirBotaoEmDisputa() {
    if (this.remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.ALTERADA.getId())) {
      return true;
    }
    return false;
  }

  public void exibirBotaoEmDisputaTipoC(RemessaVO remessaVO) {
    if (remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.ALTERADA.getId())) {
      flagExibeBotaoEmDisputaTipoC = true;
    } else {
      flagExibeBotaoEmDisputaTipoC = false;
    }
  }

  public boolean exibirBotaoDesfazerAlteracoes() {
    if (this.remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId())
        && getPerfilGestor()) {
      return true;
    }
    return false;
  }

  public void exibirBotaoDesfazerAlteracoesTipoC(RemessaVO remessaVO) {
    if (remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId())
        && getPerfilGestor()) {
      flagExibeBotaoDesfazerAlteracoesTipoC = true;
    } else {
      flagExibeBotaoDesfazerAlteracoesTipoC = false;
    }
  }

  public boolean exibirBotaoInvalidarAlteracoes() {
    if (this.remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId())
        && getPerfilGestor()) {
      return true;
    }
    return false;
  }

  public void exibirBotaoInvalidarAlteracoesTipoC(RemessaVO remessaVO) {
    if (remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId())
        && getPerfilGestor()) {
      flagExibeBotaoInvalidarAlteracoesTipoC = true;
    } else {
      flagExibeBotaoInvalidarAlteracoesTipoC = false;
    }
  }

  public boolean exibirBotaoTermoCapaLote(RemessaVO remessaVO) {
    if (remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.ABERTA.getId())
        || remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.AGENDADA.getId())) {
      return true;
    }
    return false;
  }

  public boolean exibirBotaoClonarRemessa(RemessaVO remessaVO) {
    if (remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.EM_ALTERACAO.getId())
        || remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.ALTERADA.getId())
        || remessaVO.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.INVALIDA.getId())) {
      return false;
    }
    return true;
  }

  public Long getNuRemessa() {
    return nuRemessa;
  }

  public void setNuRemessa(Long nuRemessa) {
    this.nuRemessa = nuRemessa;
  }

  public String getCoUsuarioFiltro() {
    return coUsuarioFiltro;
  }

  public void setCoUsuarioFiltro(String coUsuarioFiltro) {
    this.coUsuarioFiltro = coUsuarioFiltro;
  }

  public Date getDataInicioFiltro() {
    return dataInicioFiltro;
  }

  public void setDataInicioFiltro(Date dataInicioFiltro) {
    this.dataInicioFiltro = dataInicioFiltro;
  }

  public Date getDataFimFiltro() {
    return dataFimFiltro;
  }

  public void setDataFimFiltro(Date dataFimFiltro) {
    this.dataFimFiltro = dataFimFiltro;
  }

  public UnidadeVO getUnidadeFiltro() {
    return unidadeFiltro;
  }

  public void setUnidadeFiltro(UnidadeVO unidadeFiltro) {
    this.unidadeFiltro = unidadeFiltro;
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

  public String getFiltroHistorico() {
    return filtroHistorico;
  }

  public void setFiltroHistorico(String filtroHistorico) {
    this.filtroHistorico = filtroHistorico;
  }

  public BaseVO getBaseFiltro() {
    return baseFiltro;
  }

  public void setBaseFiltro(BaseVO baseFiltro) {
    this.baseFiltro = baseFiltro;
  }

  public List<RequisicaoVO> getListaFiltro() {
    return listaFiltro;
  }

  public void setListaFiltro(List<RequisicaoVO> listaFiltro) {
    this.listaFiltro = listaFiltro;
  }

  public List<BaseVO> getListaBase() {
    return listaBase;
  }

  public void setListaBase(List<BaseVO> listaBase) {
    this.listaBase = listaBase;
  }

  @Override
  protected RemessaVO newInstance() {
    return new RemessaVO();
  }

  @Override
  protected AbstractService<RemessaVO> getService() {
    return null;
  }

  @Override
  public String nomeFuncionalidade() {
    return "ManutencaoRemessa";
  }

  public boolean isPesquisaPorNumeroRemessa() {
    return pesquisaPorNumeroRemessa;
  }

  public void setPesquisaPorNumeroRemessa(boolean pesquisaPorNumeroRemessa) {
    this.pesquisaPorNumeroRemessa = pesquisaPorNumeroRemessa;
  }

  public LazyDataModel<RemessaVO> getListaRemessaModel() {
    return listaRemessaModel;
  }

  public void setListaRemessaModel(LazyDataModel<RemessaVO> listaRemessaModel) {
    this.listaRemessaModel = listaRemessaModel;
  }

  public SituacaoRemessaEnum getSituacaoFiltro() {
    return situacaoFiltro;
  }

  public void setSituacaoFiltro(SituacaoRemessaEnum situacaoFiltro) {
    this.situacaoFiltro = situacaoFiltro;
  }

  public RemessaVO getRemessa() {
    return remessa;
  }

  public void setRemessaVO(RemessaVO remessa) {
    this.remessa = remessa;
  }

  public boolean isPesquisaSucesso() {
    return pesquisaSucesso;
  }

  public void setPesquisaSucesso(boolean pesquisaSucesso) {
    this.pesquisaSucesso = pesquisaSucesso;
  }

  public TramiteRemessaVO getTramiteRemessaVO() {
    return tramiteRemessaVO;
  }

  public void setTramiteRemessaVO(TramiteRemessaVO tramiteRemessaVO) {
    this.tramiteRemessaVO = tramiteRemessaVO;
  }

  public GrupoVO getGrupo() {
    return grupo;
  }

  public void setGrupo(GrupoVO grupo) {
    this.grupo = grupo;
  }

  public void setRemessa(RemessaVO remessa) {
    this.remessa = remessa;
  }

  public RemessaDocumentoVO getRemessaDocumento() {
    return remessaDocumento;
  }

  public void setRemessaDocumento(RemessaDocumentoVO remessaDocumento) {
    this.remessaDocumento = remessaDocumento;
  }

  public int getAnoFragmentacao() {
    return anoFragmentacao;
  }

  public void setAnoFragmentacao(int anoFragmentacao) {
    this.anoFragmentacao = anoFragmentacao;
  }

  public StreamedContent getTermoResponsabilidade() {
    return termoResponsabilidade;
  }

  public void setTermoResponsabilidade(StreamedContent termoResponsabilidade) {
    this.termoResponsabilidade = termoResponsabilidade;
  }

  public StreamedContent getCapaLote() {
    return capaLote;
  }

  public void setCapaLote(StreamedContent capaLote) {
    this.capaLote = capaLote;
  }

  public List<TramiteRemessaVO> getFiltredListaTramitesRemessa() {
    return filtredListaTramitesRemessa;
  }

  public void setFiltredListaTramitesRemessa(List<TramiteRemessaVO> filtredListaTramitesRemessa) {
    this.filtredListaTramitesRemessa = filtredListaTramitesRemessa;
  }

  public List<TramiteRemessaVO> getListaTramitesRemessa() {
    return listaTramitesRemessa;
  }

  public void setListaTramitesRemessa(List<TramiteRemessaVO> listaTramitesRemessa) {
    this.listaTramitesRemessa = listaTramitesRemessa;
  }

  /**
   * @return the itemDetalheTipoC
   */
  public RemessaMovimentoDiarioVO getItemDetalheTipoC() {
    return itemDetalheTipoC;
  }

  /**
   * @param itemDetalheTipoC the itemDetalheTipoC to set
   */
  public void setItemDetalheTipoC(RemessaMovimentoDiarioVO itemDetalheTipoC) {
    this.itemDetalheTipoC = itemDetalheTipoC;
  }

  /**
   * @return the itemDetalheTipoCCOTO
   */
  public MovimentoDiarioRemessaCDTO getItemDetalheTipoCCDTO() {
    return itemDetalheTipoCCDTO;
  }

  /**
   * @param itemDetalheTipoCCDTO the itemDetalheTipoCCOTO to set
   */
  public void setItemDetalheTipoCCDTO(MovimentoDiarioRemessaCDTO itemDetalheTipoCCDTO) {
    this.itemDetalheTipoCCDTO = itemDetalheTipoCCDTO;
  }

  public LazyDataModel<RemessaVO> getListaRemessaModelTratada() {
    return listaRemessaModelTratada;
  }

  public void setListaRemessaModelTratada(LazyDataModel<RemessaVO> listaRemessaModelTratada) {
    this.listaRemessaModelTratada = listaRemessaModelTratada;
  }

  /**
   * @return the flagExibeBotaoEmDisputaTipoC
   */
  public boolean isFlagExibeBotaoEmDisputaTipoC() {
    return flagExibeBotaoEmDisputaTipoC;
  }

  /**
   * @param flagExibeBotaoEmDisputaTipoC the flagExibeBotaoEmDisputaTipoC to set
   */
  public void setFlagExibeBotaoEmDisputaTipoC(boolean flagExibeBotaoEmDisputaTipoC) {
    this.flagExibeBotaoEmDisputaTipoC = flagExibeBotaoEmDisputaTipoC;
  }

  /**
   * @return the flagExibeBotaoDesfazerAlteracoesTipoC
   */
  public boolean isFlagExibeBotaoDesfazerAlteracoesTipoC() {
    return flagExibeBotaoDesfazerAlteracoesTipoC;
  }

  /**
   * @param flagExibeBotaoDesfazerAlteracoesTipoC the flagExibeBotaoDesfazerAlteracoesTipoC to set
   */
  public void setFlagExibeBotaoDesfazerAlteracoesTipoC(boolean flagExibeBotaoDesfazerAlteracoesTipoC) {
    this.flagExibeBotaoDesfazerAlteracoesTipoC = flagExibeBotaoDesfazerAlteracoesTipoC;
  }

  /**
   * @return the flagExibeBotaoInvalidarAlteracoesTipoC
   */
  public boolean isFlagExibeBotaoInvalidarAlteracoesTipoC() {
    return flagExibeBotaoInvalidarAlteracoesTipoC;
  }

  /**
   * @param flagExibeBotaoInvalidarAlteracoesTipoC the flagExibeBotaoInvalidarAlteracoesTipoC to set
   */
  public void setFlagExibeBotaoInvalidarAlteracoesTipoC(boolean flagExibeBotaoInvalidarAlteracoesTipoC) {
    this.flagExibeBotaoInvalidarAlteracoesTipoC = flagExibeBotaoInvalidarAlteracoesTipoC;
  }

  /**
   * @return the flagExibeBotaoConfirmarAlteracoesTipoC
   */
  public boolean isFlagExibeBotaoConfirmarAlteracoesTipoC() {
    return flagExibeBotaoConfirmarAlteracoesTipoC;
  }

  /**
   * @param flagExibeBotaoConfirmarAlteracoesTipoC the flagExibeBotaoConfirmarAlteracoesTipoC to set
   */
  public void setFlagExibeBotaoConfirmarAlteracoesTipoC(boolean flagExibeBotaoConfirmarAlteracoesTipoC) {
    this.flagExibeBotaoConfirmarAlteracoesTipoC = flagExibeBotaoConfirmarAlteracoesTipoC;
  }

  /**
   * @return the acaoRemessa
   */
  public String getAcaoRemessa() {
    return acaoRemessa;
  }

  /**
   * @param acaoRemessa the acaoRemessa to set
   */
  public void setAcaoRemessa(String acaoRemessa) {
    this.acaoRemessa = acaoRemessa;
  }

  /**
   * @return the listaAcaoRemessa
   */
  public String[] getListaAcaoRemessa() {
    return listaAcaoRemessa;
  }

  /**
   * @param listaAcaoRemessa the listaAcaoRemessa to set
   */
  public void setListaAcaoRemessa(String[] listaAcaoRemessa) {
    this.listaAcaoRemessa = listaAcaoRemessa;
  }

  /**
   * @return the etiquetaMovDiario
   */
  public StreamedContent getEtiquetaMovDiario() {
    return etiquetaMovDiario;
  }

  /**
   * @param etiquetaMovDiario the etiquetaMovDiario to set
   */
  public void setEtiquetaMovDiario(StreamedContent etiquetaMovDiario) {
    this.etiquetaMovDiario = etiquetaMovDiario;
  }
  
  public Boolean hasQuantitativoZerado() {
		return quantitativoZerado;
  }

}
