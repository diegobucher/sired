package br.gov.caixa.gitecsa.sired.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.web.PaginatorModel;
import br.gov.caixa.gitecsa.sired.dto.AcaoAvaliacaoDTO;
import br.gov.caixa.gitecsa.sired.dto.FiltroRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.enumerator.FormatoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.OcorrenciaAtendimentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.exporter.ExportarRequisicaoXLS;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.AuditoriaService;
import br.gov.caixa.gitecsa.sired.service.AvaliacaoRequisicaoService;
import br.gov.caixa.gitecsa.sired.service.BaseService;
import br.gov.caixa.gitecsa.sired.service.MinhasRequisicoesService;
import br.gov.caixa.gitecsa.sired.service.MotivoAvaliacaoService;
import br.gov.caixa.gitecsa.sired.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.service.TramiteRequisicaoService;
import br.gov.caixa.gitecsa.sired.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.JavaScriptUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.AuditoriaVO;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.MotivoAvaliacaoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named("requisicaoDocumentoController")
@ViewScoped
public class RequisicaoController implements Serializable {

    private static final String URL_PARAM_MINHASREQUISICOES = "minhasrequisicoes";

    private static final String URL_PARAM_SITUACAO = "situacao";

    private static final String URL_PARAM_REQUISICOES = "requisicoes";

    private static final long serialVersionUID = 4003341888367769611L;

    private static final int INTERVALO_DIAS_FILTRO = -60;

    private static final int REGISTROS_POR_PAGINA = 10;

    private static final String SEPARADOR_REQUISICOES = ",";

    private static final String ID_MODAL_AVALIACAO = "mdlAvaliacao";

    private static final String ID_MODAL_DATA = "mdlDtNaoInformada";

    private static final String ID_MODAL_HISTORICO = "mdlHistorico";

    private static final String ID_MODAL_AUTORIZACAO = "mdlAutorizar";

    private static final String ID_MODAL_TRATAMENTO = "mdlTratar";

    private static final String ID_MODAL_INFORMACOES = "mdlInfo";
    
    private static final String ID_MODAL_CLONAR = "mdlClonar";
    
    private static final String ID_MODAL_CANCELAR_RASCUNHO = "mdlCancelarRascunho";

    private static final String ID_DATALIST_RESULTADOS = "formConsulta:tabela";

    @Inject
    protected FacesMensager facesMessager;

    @Inject
    private transient Logger logger;

    @Inject
    private RequisicaoService requisicaoService;

    @Inject
    private MinhasRequisicoesService minhasRequisicaoService;

    @Inject
    private BaseService baseService;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private TramiteRequisicaoService tramiteService;

    @Inject
    private MotivoAvaliacaoService motivoAvaliacaoService;

    @Inject
    private AvaliacaoRequisicaoService avaliacaoService;
    
    @Inject
    private AuditoriaService auditoriaService;

    private FiltroRequisicaoDTO filtroRequisicao;

    private LazyDataModel<RequisicaoVO> listRequisicaoModel;

    private List<BaseVO> listBase;

    private List<MotivoAvaliacaoVO> listMotivoAvaliacao;

    private Set<GrupoCampoVO> listGrupoCampos;

    private List<TramiteRequisicaoVO> listAtendimentos;

    private Boolean apenasMinhasRequisicoes;

    private List<TramiteRequisicaoVO> listTramiteRequisicao;

    private RequisicaoVO requisicaoSelecionada;

    private AvaliacaoRequisicaoVO avaliacaoRequisicaoSelecionada;

    private String notificaoUnidadeNaoPermitida;

    private Boolean confirmarCancelamento;

    private String motivoCancelamento;
    
    private String situacaoUrlParam;

	private List<AuditoriaVO> listAuditoria;

    @PostConstruct
    public void init() {
        try {
            this.limparCamposFiltros();
            this.listBase = baseService.findAll();
            Collections.sort(this.listBase);

            this.listMotivoAvaliacao = motivoAvaliacaoService.findAll();
            Collections.sort(this.listMotivoAvaliacao);

            this.setApenasMinhasRequisicoes(false);

            if (RequestUtils.getParameter(URL_PARAM_MINHASREQUISICOES) != null) {
                this.setApenasMinhasRequisicoes(true);
                this.localizarMinhasRequisicoes();
            } else if (RequestUtils.getParameter(URL_PARAM_SITUACAO) != null) {
                this.localizarPorSituacao();
            } else if (RequestUtils.getParameter(URL_PARAM_REQUISICOES) != null) {
                this.localizarPorNumero();
            } else {
                this.filtroRequisicao.setDataInicio(Util.getDataMesPassado());
            }
        } catch (DataBaseException e) {
        	this.logger.error(e.getMessage(), e);
			this.facesMessager.addMessageError("MA048");
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }
    }

    /**
     * Consulta as unidades solicitantes autorizadas através do código da unidade
     */
    public void pesquisarUnidadeSolicitante() {

        try {
            UsuarioLdap usuario = getUsuarioAutenticado();
            if (usuario != null) {
                UnidadeVO unidade = this.unidadeService.findUnidadeAutorizada(this.filtroRequisicao.getUnidadeSolicitante(), usuario);
                if (ObjectUtils.isNullOrEmpty(unidade)) {
                    unidade = new UnidadeVO();
                }
                this.filtroRequisicao.setUnidadeSolicitante(unidade);
                JavaScriptUtils.update("formConsulta:pnlBotoes");
            }
        } catch (BusinessException e) {
            UnidadeVO unidade = this.unidadeService.findById((Long) this.filtroRequisicao.getUnidadeSolicitante().getId());
            if (!ObjectUtils.isNullOrEmpty(unidade)) {
                this.filtroRequisicao.setUnidadeSolicitante(unidade);
            }
            facesMessager.addMessageError(e.getMessage());
        } catch (DataBaseException e) {
        	this.logger.error(e.getMessage(), e);
			this.facesMessager.addMessageError("MA048");
		}
    }

    /**
     * Remove a mensagem de notificação de unidade não pertmidida/cadastrada
     */
    public void limparNotificacaoUnidade() {
        this.notificaoUnidadeNaoPermitida = null;
    }

    /**
     * Exibe uma mensagem informando que a data de inicio é obrigatória. Utilizada ao responder "Não" no modal de confirmação para
     * preenchimento automático do período de consulta.
     */
    public void informarDataInicioObrigatoria() {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.dataInicio"));
    }

    /**
     * Realiza a consulta de requisições
     */
    public void localizar() {

        if (this.validarCampos()) {

            Map<String, Object> filtro = new HashMap<String, Object>();

            if (StringUtils.isBlank(this.filtroRequisicao.getNumeroRequisicoes())) {
                if (this.isDatasFiltroPreenchidas()) {
                    this.ajustarPeriodoFiltro();
                    filtro.put("filtroDTO", this.filtroRequisicao);
                } else {
                    JavaScriptUtils.showModal(ID_MODAL_DATA);
                    return;
                }
            } else {
                FiltroRequisicaoDTO filtroPorNumero = new FiltroRequisicaoDTO();
                filtroPorNumero.setNumeroRequisicoes(this.filtroRequisicao.getNumeroRequisicoes());
                filtro.put("filtroDTO", filtroPorNumero);
            }

            this.resetarDataListResultados();

            UsuarioLdap usuario = getUsuarioAutenticado();
            if (!this.unidadeService.isUnidadeAutorizada(this.filtroRequisicao.getUnidadeSolicitante(), usuario)) {
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA040"));
            } else {
                PaginatorModel<RequisicaoVO> paginator = new PaginatorModel<RequisicaoVO>(this.requisicaoService, filtro);
                this.listRequisicaoModel = paginator.getListModel();
                this.listRequisicaoModel.load(0, 1, null, null, null);

                this.setApenasMinhasRequisicoes(false);
                JavaScriptUtils.update("formConsulta");
            }
        }
        
        String agrupamento = (String) RequestUtils.getSessionValue(Constantes.AGRUPAMENTO_SITUACAO_REQUISICAO);
        if (!ObjectUtils.isNullOrEmpty(agrupamento) && agrupamento.equals(Constantes.PEND_CAIXA)) {
            JavaScriptUtils.execute("$('.filtro-situacao option:eq(1)').prop('selected', true);");
        } else if (!ObjectUtils.isNullOrEmpty(agrupamento) && agrupamento.equals(Constantes.PEND_ATENDIMENTO)) {
            JavaScriptUtils.execute("$('.filtro-situacao option:eq(2)').prop('selected', true);");
        } else if (ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getSituacao()) && ObjectUtils.isNullOrEmpty(agrupamento)) {
            JavaScriptUtils.execute("$('.filtro-situacao option:eq(0)').prop('selected', true);");
        }
    }

    /**
     * Obtém o usuário autenticado
     * 
     * @return UsuarioLdap Usuário autenticado e registrado na sessão
     */
    public UsuarioLdap getUsuarioAutenticado() {
        UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        return usuario;
    }

    /**
     * Realiza a consulta de requisições criadas nos últimos 60 dias
     */
    public void localizarPorPeriodoPadrao() {

        Calendar calendar = Calendar.getInstance();
        this.filtroRequisicao.setDataFim(calendar.getTime());

        calendar.add(Calendar.DAY_OF_YEAR, INTERVALO_DIAS_FILTRO);
        this.filtroRequisicao.setDataInicio(calendar.getTime());
        this.localizar();
    }

    /**
     * Consulta as requisições permitidas para o usuário autenticado
     */
    public void localizarMinhasRequisicoes() {

        PaginatorModel<RequisicaoVO> paginator = new PaginatorModel<RequisicaoVO>(this.minhasRequisicaoService, null);
        this.listRequisicaoModel = paginator.getListModel();
        this.listRequisicaoModel.load(0, REGISTROS_POR_PAGINA, null, null, null);

        List<String> ids = new ArrayList<String>();
        if (paginator.getList() != null) {
            Iterator<RequisicaoVO> it = paginator.getList().iterator();
            while (it.hasNext()) {

                RequisicaoVO requisicao = it.next();
                ids.add(requisicao.getCodigoRequisicao().toString());
            }
        }

        this.filtroRequisicao.setNumeroRequisicoes(StringUtils.join(ids, SEPARADOR_REQUISICOES));
    }

    /**
     * Consulta as requisições por situação
     */
    public void localizarPorSituacao() {

        Map<String, Object> filtro = new HashMap<String, Object>();

        this.filtroRequisicao.setSituacao(SituacaoRequisicaoEnum.valueOf(RequestUtils.getParameter(URL_PARAM_SITUACAO).toUpperCase()));
        filtro.put("filtroDTO", this.filtroRequisicao);

        this.resetarDataListResultados();

        PaginatorModel<RequisicaoVO> paginator = new PaginatorModel<RequisicaoVO>(this.requisicaoService, filtro);
        this.listRequisicaoModel = paginator.getListModel();
        this.listRequisicaoModel.load(0, REGISTROS_POR_PAGINA, null, null, null);
    }

    /**
     * Consulta as requisições por número
     */
    public void localizarPorNumero() {

        this.resetarDataListResultados();

        Map<String, Object> filtro = new HashMap<String, Object>();

        this.filtroRequisicao.setNumeroRequisicoes(RequestUtils.getParameter(URL_PARAM_REQUISICOES));
        filtro.put("filtroDTO", this.filtroRequisicao);

        PaginatorModel<RequisicaoVO> paginator = new PaginatorModel<RequisicaoVO>(this.requisicaoService, filtro);
        this.listRequisicaoModel = paginator.getListModel();
        this.listRequisicaoModel.load(0, REGISTROS_POR_PAGINA, null, null, null);
    }

    /**
     * Redefine os filtros da consulta para o estado inicial
     * @throws DataBaseException 
     */
    public void limparCamposFiltros() throws DataBaseException {

        this.filtroRequisicao = new FiltroRequisicaoDTO();
        UsuarioLdap usuario = getUsuarioAutenticado();
        if (!ObjectUtils.isNullOrEmpty(usuario)) {
            this.filtroRequisicao.setMatriculaUsuario(usuario.getNuMatricula());
            this.filtroRequisicao.setUnidadeSolicitante(unidadeService.findUnidadeLotacaoUsuarioLogado());
        }

        this.listRequisicaoModel = null;
        this.setApenasMinhasRequisicoes(false);
        this.limparNotificacaoUnidade();
        RequestUtils.unsetSessionValue(Constantes.AGRUPAMENTO_SITUACAO_REQUISICAO);
    }

    /**
     * Exibe o modal que contém o histórico dos tramites da requisição selecionada
     * 
     * @param requisicao
     *            Requisição selecionada
     */
    public void visualizarHistoricoTramite(RequisicaoVO requisicao) {

        this.requisicaoSelecionada = requisicao;
        this.listTramiteRequisicao = this.tramiteService.pesquisarHistorico(requisicao);
        this.listAuditoria = this.auditoriaService.findAllByIdentificador(((Long) requisicao.getId()).intValue());

        JavaScriptUtils.showModal(ID_MODAL_HISTORICO);
    }

    /**
     * Exibe o modal para avaliação da requisição selecionada
     * 
     * @param requisicao
     *            Requisição selecionada
     */
    public void visualizarAvaliacaoRequisicao(RequisicaoVO requisicao) {

        this.requisicaoSelecionada = requisicao;

        SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());

        if (SituacaoRequisicaoEnum.ATENDIDA.equals(situacaoAtual) || SituacaoRequisicaoEnum.REATENDIDA.equals(situacaoAtual)) {

            this.avaliacaoRequisicaoSelecionada = new AvaliacaoRequisicaoVO();
            this.avaliacaoRequisicaoSelecionada.setTramite(requisicao.getTramiteRequisicaoAtual());

            UsuarioLdap usuarioLogado = getUsuarioAutenticado();
            if (!ObjectUtils.isNullOrEmpty(usuarioLogado)) {
                this.avaliacaoRequisicaoSelecionada.setCodigoUsuario(usuarioLogado.getNuMatricula());
            }

            // Não deve permitir reabertura
            if (SituacaoRequisicaoEnum.REATENDIDA.equals(situacaoAtual)) {
                this.avaliacaoRequisicaoSelecionada.setIcReabertura(SimNaoEnum.NAO);
            }

        } else if (SituacaoRequisicaoEnum.FECHADA.equals(situacaoAtual)) {
            this.avaliacaoRequisicaoSelecionada = avaliacaoService.findByRequisicao(requisicao);
        }

        JavaScriptUtils.showModal(ID_MODAL_AVALIACAO);
    }

    /**
     * Obtém os valores dos campos dinâmicos do formulário
     */
    public void getValorCamposDinamicos() {
        try {
            this.listGrupoCampos = GrupoCamposHelper.getValorCamposDinamicos(this.requisicaoSelecionada, this.listGrupoCampos);
        } catch (AppException e) {
            facesMessager.addMessageError(e.getMessage());
        }
    }

    /**
     * Exibe o modal que exibe as informações da requisição selecionada
     * 
     * @param requisicao
     *            Requisição selecionada
     */
    public void visualizarRequisicao(RequisicaoVO requisicao) {

        this.requisicaoSelecionada = this.requisicaoService.findByIdEager((Long) requisicao.getId());
        this.listAtendimentos = this.tramiteService.findAtendimentosRequisicao(this.requisicaoSelecionada);
        this.listGrupoCampos = this.requisicaoSelecionada.getDocumento().getGrupo().getGrupoCampos();

        this.getValorCamposDinamicos();

        JavaScriptUtils.showModal(ID_MODAL_INFORMACOES);
    }

    /**
     * Realiza a avaliação de uma requisição
     */
    public void avaliarRequisicao() {
        try {

            String msg = StringUtils.EMPTY;

            this.requisicaoService.avaliar(this.requisicaoSelecionada, this.avaliacaoRequisicaoSelecionada);
            msg = (this.avaliacaoRequisicaoSelecionada.getIcReabertura().equals(SimNaoEnum.NAO)) ? "MS021" : "MS022";
            this.avaliacaoRequisicaoSelecionada = null;
            this.requisicaoSelecionada = null;

            facesMessager.addMessageInfo(MensagemUtils.obterMensagem(msg));
            this.localizar();

            JavaScriptUtils.update("formConsulta");
            JavaScriptUtils.hideModal(ID_MODAL_AVALIACAO);

        } catch (Exception e) {
            facesMessager.addMessageError(e.getMessage());
        }
    }

    /**
     * Exibe modal de confirmação de autorização
     * 
     * @param requisicao
     *            Requisição selecionada
     */
    public void confirmarAutorizacaoRequisicao(RequisicaoVO requisicao) {
        this.requisicaoSelecionada = requisicao;

        if (this.isRequisicaoCancelada(requisicao)) {
            this.confirmarCancelamento = true;
            this.motivoCancelamento = this.requisicaoSelecionada.getTramiteRequisicaoAtual().getObservacao();
        } else {
            this.confirmarCancelamento = false;
            this.motivoCancelamento = null;
        }

        JavaScriptUtils.showModal(ID_MODAL_AUTORIZACAO);
    }

    public String autorizarRequisicao() {

        try {

            this.requisicaoService.autorizar(this.requisicaoSelecionada);

            // FIXME: #RC-SIRED Definir código da mensagem
            facesMessager.addMessageInfo(String.format("Requisição Código %s autorizada com sucesso", this.requisicaoSelecionada.getCodigoRequisicao()
                    .toString()));
            
            this.requisicaoSelecionada = null;

            if (this.isDatasFiltroPreenchidas()) {
                this.ajustarPeriodoFiltro();
            }
            
            SituacaoRequisicaoEnum situacao = this.getSituacaoUrlParamAsEnum();
            
            if (!ObjectUtils.isNullOrEmpty(situacao) && SituacaoRequisicaoEnum.EM_AUTORIZACAO.equals(situacao)) {
                this.facesMessager.setKeepMessages(true);
                this.localizarPorSituacao();
            }
            
            //this.setSituacaoUrlParam(null);
            JavaScriptUtils.update("@form", "pnlResultados", "pnlBotoes");

        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
            facesMessager.addMessageError(e.getMessage());
        }
        
        return null;
    }
    
    public void cancelarRequisicao() {
        try {

            if (!ObjectUtils.isNullOrEmpty(this.motivoCancelamento)) {

                this.requisicaoService.cancelar(this.requisicaoSelecionada, this.motivoCancelamento);

                facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS017", this.requisicaoSelecionada.getCodigoRequisicao().toString()));
                this.requisicaoSelecionada = null;

                if (this.isDatasFiltroPreenchidas()) {
                    this.ajustarPeriodoFiltro();
                }
                
                SituacaoRequisicaoEnum situacao = this.getSituacaoUrlParamAsEnum();
                
                if (!ObjectUtils.isNullOrEmpty(situacao) && SituacaoRequisicaoEnum.EM_AUTORIZACAO.equals(situacao)) {
                    this.facesMessager.setKeepMessages(true);
                    this.localizarPorSituacao();
                }
                
                //this.setSituacaoUrlParam(null);
                JavaScriptUtils.execute("mdlAutorizar.hide();");
                JavaScriptUtils.update("formConsulta", "pnlResultados", "pnlBotoes");

            } else {

                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "Motivo de cancelamento"));
                JavaScriptUtils.update("frmModalAutorizar");
            }
        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
            facesMessager.addMessageError(e.getMessage());
        }
    }

    /**
     * Exibe modal de confirmação de autorização
     * 
     * @param requisicao
     *            Requisição selecionada
     */
    public void confirmarTratamentoRequisicao(RequisicaoVO requisicao) {
        this.requisicaoSelecionada = requisicao;
        JavaScriptUtils.showModal(ID_MODAL_TRATAMENTO);
    }

    public void tratarRequisicao() {

        try {

            this.requisicaoService.tratar(this.requisicaoSelecionada);

            // FIXME: #RC-SIRED Definir código da mensagem
            facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS042", this.requisicaoSelecionada.getCodigoRequisicao().toString()));

            this.requisicaoSelecionada = null;

            if (this.isDatasFiltroPreenchidas()) {
                this.ajustarPeriodoFiltro();
            }
            
            SituacaoRequisicaoEnum situacao = this.getSituacaoUrlParamAsEnum();
            
            if (!ObjectUtils.isNullOrEmpty(situacao) && SituacaoRequisicaoEnum.EM_TRATAMENTO.equals(situacao)) {
                this.facesMessager.setKeepMessages(true);
                this.localizarPorSituacao();
            }

            JavaScriptUtils.update("@form", "pnlResultados", "pnlBotoes");

        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
            facesMessager.addMessageError(e.getMessage());
        }
    }
    
    public void confirmarClonagemRequisicao(RequisicaoVO requisicao) {
        this.requisicaoSelecionada = requisicao;
        JavaScriptUtils.showModal(ID_MODAL_CLONAR);
    }
    
    public void confirmarCancelarRascunho(RequisicaoVO requisicao) {
    	this.requisicaoSelecionada = requisicao;
    	JavaScriptUtils.showModal(ID_MODAL_CANCELAR_RASCUNHO);
    }
    
    public String clonarRequisicao() {
        try {
            RequisicaoVO requisicao = this.requisicaoService.clonar(this.requisicaoSelecionada);
            return this.editar(requisicao);
        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
            facesMessager.addMessageError(e.getMessage());
        }
        
        return null;
    }
    
    public void cancelarRascunho() {
    	try {

    		this.requisicaoService.cancelarRascunho(this.requisicaoSelecionada);

            facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS017", this.requisicaoSelecionada.getCodigoRequisicao().toString()));
            this.requisicaoSelecionada = null;

            if (this.isDatasFiltroPreenchidas()) {
                this.ajustarPeriodoFiltro();
            }
            
            SituacaoRequisicaoEnum situacao = this.getSituacaoUrlParamAsEnum();
            
            if (!ObjectUtils.isNullOrEmpty(situacao) && SituacaoRequisicaoEnum.RASCUNHO.equals(situacao)) {
                this.facesMessager.setKeepMessages(true);
                this.localizarPorSituacao();
            }
            
            JavaScriptUtils.execute("mdlCancelarRascunho.hide();");
            JavaScriptUtils.update("formConsulta", "pnlResultados", "pnlBotoes");
            
        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
            facesMessager.addMessageError(e.getMessage());
        }
    }

    /**
     * Redireciona o usuário para o formulário de edição de rascunho de requisição
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return URL
     */
    public String editar(RequisicaoVO requisicao) {
        return AberturaRequisicaoController.VIEW_ID_RASCUNHO + "?faces-redirect=true&id=" + requisicao.getId();
    }

    /**
     * Realiza o download do documento anexado
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return Uma instancia de StreamedContent
     */
    public StreamedContent downloadDocumento(RequisicaoVO requisicao) {

        try {

            String caminho = System.getProperty(Constantes.CAMINHO_UPLOAD);
            String nomeDocumento = requisicao.getTramiteRequisicaoAtual().getArquivoDisponibilizado();
            File arquivo = new File(caminho + nomeDocumento);
            
            this.auditoriaService.gravar(requisicao, this.getUsuarioAutenticado());

            return RequestUtils.download(arquivo, nomeDocumento);
        } catch (BusinessException e) {
        	this.logger.error(e.getMessage(), e);
            facesMessager.addMessageError(e.getMessage());
        } catch (DataBaseException e) {
        	if (e.getCause() != null) {
        		this.logger.error(e.getMessage(), e);
        	}
        	
        	this.logger.error(e.getMessage(), e);
			this.facesMessager.addMessageError("MA048");
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }

        return null;
    }
    
    public StreamedContent downloadJustificativa(RequisicaoVO requisicao) {
		try {
			File arquivo = Paths.get(
					System.getProperty(Constantes.CAMINHO_UPLOAD_JUSTIFICATIVA),
					requisicao.getArquivoJustificativa()).toFile();

	        if (!arquivo.exists()) {
	        	throw new BusinessException(MensagemUtils.obterMensagem("MI028"));
	        }
	        
	        return RequestUtils.download(arquivo, arquivo.getName());
		} catch (Exception e){
			e.printStackTrace();
			this.logger.error(e.getMessage(), e);
			this.facesMessager.addMessageError(e.getMessage());
		}
		
		return null;
    }

    /**
     * Verifica se deve exibir o botão para avaliar a requisição informada
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return <b>True</b> se o tramite atual da requisição é ATENDIDA ou REATENDIDA, <b>false</b> caso contrário
     */
    public Boolean hasBotaoAvaliar(RequisicaoVO requisicao) {

        SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());
        if (SituacaoRequisicaoEnum.ATENDIDA.equals(situacaoAtual) || SituacaoRequisicaoEnum.REATENDIDA.equals(situacaoAtual)) {
            return true;
        }

        return false;
    }

    /**
     * Verifica se deve exibir o botão para visualizar avaliação da requisição informada
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return <b>True</b> se o tramite atual da requisição é FECHADA e <b>false</b> caso contrário
     */
    public Boolean hasBotaoVisualizarAvaliacao(RequisicaoVO requisicao) {

        SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());
        if (this.avaliacaoService.hasAvaliacao(requisicao) && SituacaoRequisicaoEnum.FECHADA.equals(situacaoAtual)) {
            return true;
        }

        return false;
    }

    /**
     * Verifica se deve exibir o botão para tratar a requisição informada
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return <b>True</b> se permite tratar a requisição e <b>false</b> caso contrário
     */
    public Boolean hasBotaoTratar(RequisicaoVO requisicao) {

        SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());
        if ((SituacaoRequisicaoEnum.EM_TRATAMENTO.equals(situacaoAtual) || SituacaoRequisicaoEnum.REABERTA_EM_TRATAMENTO.equals(situacaoAtual))
                && JSFUtil.isPerfil(Constantes.PERFIL_GESTOR)) {
            return true;
        }

        return false;
    }

    /**
     * Verifica se deve exibir o botão para download do documento solicitado
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return <b>True</b> se permite realizar o download do documento e <b>false</b> caso contrário
     */
    public Boolean hasBotaoDownload(RequisicaoVO requisicao) {

        if (!ObjectUtils.isNullOrEmpty(requisicao.getTramiteRequisicaoAtual().getArquivoDisponibilizado())
                && !ObjectUtils.isNullOrEmpty(requisicao.getTramiteRequisicaoAtual().getOcorrencia())) {

            OcorrenciaAtendimentoEnum ocorrencia = OcorrenciaAtendimentoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getOcorrencia().getId());
            if (OcorrenciaAtendimentoEnum.DOC_DIGITAL.equals(ocorrencia)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * 
     * 
     * @param requisicao
     * @return
     */
    public Boolean hasBotaoDownloadJustificativa(RequisicaoVO requisicao) {
		if (FormatoDocumentoEnum.ORIGINAL.equals(requisicao.getFormato())
				&& !ObjectUtils.isNullOrEmpty(requisicao.getArquivoJustificativa())) {
			return true;
		}
    	
    	return false;
    }

    /**
     * Verifica se deve exibir o botão para editar um rascunho da requisição informada
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return <b>True</b> se permite editar o rascunho da requisição e <b>false</b> caso contrário
     */
    public Boolean hasBotaoRascunho(RequisicaoVO requisicao) {

        SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());
        if (SituacaoRequisicaoEnum.RASCUNHO.equals(situacaoAtual)) {
            return true;
        }

        return false;
    }

    /**
     * Verifica se deve exibir o botão para autorizar a requisição
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return <b>True</b> se permite autorizar a requisição e <b>false</b> caso contrário
     */
    public Boolean hasBotaoAutorizar(RequisicaoVO requisicao) {

        SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());
        if (JSFUtil.isPerfil(Constantes.PERFIL_GESTOR) && SituacaoRequisicaoEnum.EM_AUTORIZACAO.equals(situacaoAtual)) {
            return true;
        }

        return false;
    }

    /**
     * Verifica se deve exibir o botão para visualizar cancelamento da requisição informada
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return <b>True</b> se o tramite atual da requisição é CANCELADA e <b>false</b> caso contrário
     */
    public Boolean hasBotaoVisualizarCancelamento(RequisicaoVO requisicao) {
        return this.isRequisicaoCancelada(requisicao);
    }
    
	/**
	 * Verifica se deve exibir o botão para cancelar a requisição informada.
	 * 
	 * @param requisicao
	 *            Requisição selecionada
	 * @return <b>True</b> se a requisição pode ser cancelada e <b>false</b>
	 *         caso contrário.
	 */
    public Boolean hasBotaoCancelarRascunho(RequisicaoVO requisicao) {
    	UsuarioLdap usuarioLogado = getUsuarioAutenticado();
    	SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());
    	
        if (SituacaoRequisicaoEnum.RASCUNHO.equals(situacaoAtual) 
        		&& requisicao.getCodigoUsuarioAbertura().equalsIgnoreCase(usuarioLogado.getNuMatricula())) {
            return true;
        }
        
        return false;
    }

    /**
     * Verifica se a requisição informada está cancelada
     * 
     * @param requisicao
     *            Requisição selecionada
     * @return <b>True</b> se o tramite atual da requisição é CANCELADA e <b>false</b> caso contrário
     */
    public Boolean isRequisicaoCancelada(RequisicaoVO requisicao) {

        if (!ObjectUtils.isNullOrEmpty(requisicao)) {
            SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());
            if (SituacaoRequisicaoEnum.CANCELADA.equals(situacaoAtual)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica se o usuário tem permissão para selecionar uma base.
     * 
     * @return <b>True</b> se o usuário tem permissão para selecionar uma base e <b>false</b> caso contrário
     */
    public Boolean isPermitidoAlterarBaseUnidade() {
        return (JSFUtil.isPerfil(Constantes.PERFIL_GESTOR) || JSFUtil.isPerfil(Constantes.PERFIL_AUDITOR));
    }

    /**
     * Realiza a exportação da consulta
     */
    public StreamedContent exportar() {

        try {

            List<RequisicaoVO> datasource = null;

            if (this.getApenasMinhasRequisicoes()) {
                datasource = this.minhasRequisicaoService.pesquisar(0, REGISTROS_POR_PAGINA, null);
            } else {

                Map<String, Object> filtro = new HashMap<String, Object>();

                // if (RequestUtils.getParameter(URL_PARAM_REQUISICOES) != null) {
                if (RequestUtils.getParameter(URL_PARAM_REQUISICOES) != null || !StringUtils.isBlank(this.filtroRequisicao.getNumeroRequisicoes())) {
                    FiltroRequisicaoDTO filtroPorNumero = new FiltroRequisicaoDTO();
                    filtroPorNumero.setNumeroRequisicoes(this.filtroRequisicao.getNumeroRequisicoes());
                    filtro.put("filtroDTO", filtroPorNumero);
                } else {
                    if (!ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataInicio())) {
                        this.ajustarPeriodoFiltro();
                    }
                    filtro.put("filtroDTO", this.filtroRequisicao);
                }

                datasource = this.requisicaoService.pesquisar(filtro);
            }

            ExportarRequisicaoXLS exportador = new ExportarRequisicaoXLS();
            exportador.setData(datasource);

            String caminho = System.getProperty(Constantes.CAMINHO_UPLOAD);
            String filename = FileUtils.appendDateTimeToFileName(MensagemUtils.obterMensagem("requisicao.consulta.label.arquivoExportacao"), new Date());
            File relatorio = exportador.export(caminho + filename);

            return RequestUtils.download(relatorio, relatorio.getName());

        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
        } catch (FileNotFoundException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI028"));
        } catch (IOException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI028"));
        } catch (AppException e) {
            facesMessager.addMessageError(e.getMessage());
        }

        return null;
    }

    /**
     * Reseta o datalist que contém os resultados da consulta
     */
    private void resetarDataListResultados() {
        DataTable dataList = (DataTable) JavaScriptUtils.findComponentById(ID_DATALIST_RESULTADOS);
        if (!ObjectUtils.isNullOrEmpty(dataList)) {
            dataList.setFirst(0);
            dataList.setRows(REGISTROS_POR_PAGINA);
        }
    }

    /**
     * Realiza a validação dos filtros da consulta de requisições
     * 
     * @return <b>True</b> caso o formulário seja válido e <b>false</b> caso contrário.
     */
    private Boolean validarCampos() {

        if (!ObjectUtils.isNullOrEmpty(this.notificaoUnidadeNaoPermitida)) {
            return false;
        }

        if (ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getNumeroRequisicoes())
                && ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getMatriculaUsuario())
                && ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataInicio())
                && ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataFim())
                && (ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getUnidadeSolicitante()) || ObjectUtils.isNullOrEmpty(this.filtroRequisicao
                        .getUnidadeSolicitante().getId())) && ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getMotivo())
                && ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getSituacao()) && ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getBase())) {
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA054"));
            return false;
        }

        if (!ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataInicio()) && !ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataFim())) {

            Calendar dtInicio = Calendar.getInstance();
            dtInicio.setLenient(false);
            dtInicio.setTime(this.filtroRequisicao.getDataInicio());

            if (dtInicio.after(Calendar.getInstance())) {
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA038"), "Data Início");
                return false;
            }

            Calendar dtFim = Calendar.getInstance();
            dtFim.setLenient(false);
            dtFim.setTime(this.filtroRequisicao.getDataFim());

            if (dtInicio.after(dtFim)) {
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA018"));
                return false;
            }
        }

        return true;
    }
    
    public void setAgrupamentoSituacaoFromRemoteCommand() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        
        if (!ObjectUtils.isNullOrEmpty(params.get("agrupamento"))) {
            RequestUtils.setSessionValue(Constantes.AGRUPAMENTO_SITUACAO_REQUISICAO, params.get("agrupamento"));
        } else {
            RequestUtils.unsetSessionValue(Constantes.AGRUPAMENTO_SITUACAO_REQUISICAO);
        }
    }

    /**
     * Verifica se os campos do tipo data foram preenchidos
     * 
     * @return <b>True</b> caso tenham sido preenchidos e <b>false</b> caso contrário.
     */
    private Boolean isDatasFiltroPreenchidas() {
        if (ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataInicio())) {
            return false;
        }

        return true;
    }

    /**
     * Realiza o ajuste no horário das datas e seta a data padrão para o campo "data fim", caso não esteja preenchido
     */
    private void ajustarPeriodoFiltro() {

        if (!ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataInicio()) && ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataFim())) {
            this.filtroRequisicao.setDataFim(Calendar.getInstance().getTime());
        }

        this.filtroRequisicao.setDataInicio(DateUtils.fitAtStart(this.filtroRequisicao.getDataInicio()));
        this.filtroRequisicao.setDataFim(DateUtils.fitAtEnd(this.filtroRequisicao.getDataFim()));
    }
    
    private SituacaoRequisicaoEnum getSituacaoUrlParamAsEnum() {
        if (!ObjectUtils.isNullOrEmpty(this.situacaoUrlParam) && !this.situacaoUrlParam.equalsIgnoreCase("null")) {
            SituacaoRequisicaoEnum situacao = SituacaoRequisicaoEnum.valueOf(this.situacaoUrlParam.toUpperCase());
            return situacao;
        }
        
        return null;
    }

    public FiltroRequisicaoDTO getFiltroRequisicao() {
        return filtroRequisicao;
    }

    public void setFiltroRequisicao(FiltroRequisicaoDTO filtroRequisicao) {
        this.filtroRequisicao = filtroRequisicao;
    }

    public LazyDataModel<RequisicaoVO> getListRequisicaoModel() {
        return listRequisicaoModel;
    }

    public void setListRequisicaoModel(LazyDataModel<RequisicaoVO> listRequisicaoModel) {
        this.listRequisicaoModel = listRequisicaoModel;
    }

    public SituacaoRequisicaoEnum[] getListSituacaoRequisicao() {
        // return SituacaoRequisicaoEnum.values();
        return SituacaoRequisicaoEnum.getListaSituacoesOrdemAlfabetica();
    }

    public List<BaseVO> getListBase() {
        return listBase;
    }

    public List<MotivoAvaliacaoVO> getListMotivoAvaliacao() {
        return listMotivoAvaliacao;
    }

    public void setListMotivoAvaliacao(List<MotivoAvaliacaoVO> listMotivoAvaliacao) {
        this.listMotivoAvaliacao = listMotivoAvaliacao;
    }

    public Boolean getApenasMinhasRequisicoes() {
        return apenasMinhasRequisicoes;
    }

    public void setApenasMinhasRequisicoes(Boolean apenasMinhasRequisicoes) {
        this.apenasMinhasRequisicoes = apenasMinhasRequisicoes;
    }

    public List<TramiteRequisicaoVO> getListTramiteRequisicao() {
        return listTramiteRequisicao;
    }

    public void setListTramiteRequisicao(List<TramiteRequisicaoVO> listTramiteRequisicao) {
        this.listTramiteRequisicao = listTramiteRequisicao;
    }

    public List<AcaoAvaliacaoDTO> getListAcaoAvaliacao() {
        List<AcaoAvaliacaoDTO> listAcaoAvaliacao = new ArrayList<AcaoAvaliacaoDTO>();

        listAcaoAvaliacao.add(new AcaoAvaliacaoDTO("Fechar a requisição", SimNaoEnum.NAO));
        listAcaoAvaliacao.add(new AcaoAvaliacaoDTO("Reabrir a requisição", SimNaoEnum.SIM));

        return listAcaoAvaliacao;
    }

    public RequisicaoVO getRequisicaoSelecionada() {
        return requisicaoSelecionada;
    }

    public void setRequisicaoSelecionada(RequisicaoVO requisicaoSelecionada) {
        this.requisicaoSelecionada = requisicaoSelecionada;
    }

    public AvaliacaoRequisicaoVO getAvaliacaoRequisicaoSelecionada() {
        return avaliacaoRequisicaoSelecionada;
    }

    public void setAvaliacaoRequisicaoSelecionada(AvaliacaoRequisicaoVO avaliacaoRequisicaoSelecionada) {
        this.avaliacaoRequisicaoSelecionada = avaliacaoRequisicaoSelecionada;
    }

    public List<GrupoCampoVO> getListGrupoCampos() {
        if (!ObjectUtils.isNullOrEmpty(listGrupoCampos)) {
            return CollectionUtils.asSortedList(listGrupoCampos);
        }

        return new ArrayList<GrupoCampoVO>();
    }

    public List<TramiteRequisicaoVO> getListAtendimentos() {
        return listAtendimentos;
    }

    public Boolean isPerfilGeral() {
        return JSFUtil.isPerfil("G");
    }

    public String getNotificaoUnidadeNaoPermitida() {
        return notificaoUnidadeNaoPermitida;
    }

    public Boolean getConfirmarCancelamento() {
        return this.confirmarCancelamento;
    }

    public void setConfirmarCancelamento(Boolean confirmarCancelamento) {
        this.confirmarCancelamento = confirmarCancelamento;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public String getSituacaoUrlParam() {
        return situacaoUrlParam;
    }

    public void setSituacaoUrlParam(String situacaoUrlParam) {
        this.situacaoUrlParam = situacaoUrlParam;
    }

	public List<AuditoriaVO> getListAuditoria() {
		return listAuditoria;
	}

	public void setListAuditoria(List<AuditoriaVO> listAuditoria) {
		this.listAuditoria = listAuditoria;
	}
}
