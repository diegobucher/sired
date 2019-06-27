package br.gov.caixa.gitecsa.sired.extra.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.google.gson.Gson;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.exporter.DataExportable;
import br.gov.caixa.gitecsa.sired.arquitetura.web.PaginatorModel;
import br.gov.caixa.gitecsa.sired.dto.FiltroRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.dto.RemoteCommandMessage;
import br.gov.caixa.gitecsa.sired.enumerator.OcorrenciaAtendimentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.RemoteCommandMessageTypeEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSuporteEnum;
import br.gov.caixa.gitecsa.sired.exporter.ExportarRequisicaoCSV;
import br.gov.caixa.gitecsa.sired.exporter.ExportarRequisicaoXLS;
import br.gov.caixa.gitecsa.sired.extra.dto.FiltroArquivoLoteDTO;
import br.gov.caixa.gitecsa.sired.extra.service.ArquivoLoteService;
import br.gov.caixa.gitecsa.sired.extra.service.AvaliacaoService;
import br.gov.caixa.gitecsa.sired.extra.service.BaseService;
import br.gov.caixa.gitecsa.sired.extra.service.EmpresaService;
import br.gov.caixa.gitecsa.sired.extra.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.extra.service.TramiteRequisicaoService;
import br.gov.caixa.gitecsa.sired.extra.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.extra.util.JSFUtil;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.AuditoriaService;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.JavaScriptUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.OcorrenciaAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Named(value = "terceirizadaRequisicaoController")
@ViewScoped
public class TerceirizadaRequisicaoController implements Serializable {

    private static final String ID_TIPO_SUPORTE_ATENDIMENTO = "cadastrarForm:idTipoSuporte";

    private static final String ID_QTD_DISPONIBILIZADA_ATENDIMENTO = "cadastrarForm:idQtdDisponibilizada";

    private static final String ID_OCORRENCIA_ATENDIMENTO = "cadastrarForm:idOcorrencia";

    private static final String EVENTO_PROCESSAMENTO_EM_LOTE = "Processamento em Lote";

    private static final long serialVersionUID = -9103881713998105263L;

    private static final String MODAL_DETALHE_DOCUMENTO = "visualizarDoc";

    private static final String MODAL_ATENDIMENTO_HIDE = "modalAtendimento.hide();";

    private static final String MODAL_HISTORICO = "modalHistorico";

    private static final String MODAL_REABERTURA = "modalMotivoReabertura";

    private static final String MODAL_RESUMO = "mdlInfo";

    private static final String EVENTO_PESQUISAR_UNIDADE_SOLICITANTE = "pesquisarUnidadeSolicitante";

    private static final String FUNCIONALIDADE_CONSULTA_REQUISICAO = "ConsultaRequisicaoController";

    private static final String ID_MODAL_DATA = "mdlDtNaoInformada";

    private static final int INTERVALO_DIAS_FILTRO = -60;

    private static final String ID_DATATABLE_RESULTADOS = "formConsulta:tabela";

    private static final String ID_DATATABLE_RELATORIOS_LOTE = "formConsulta:tblRelatorios";

    private static final int REGISTROS_POR_PAGINA = 10;

    private static final String MODAL_ATENDIMENTO = "modalAtendimento";

    @Inject
    private transient Logger logger;

    @Inject
    private RequisicaoService requisicaoService;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private TramiteRequisicaoService tramiteRequisicaoService;

    @Inject
    private FacesMensager facesMessager;

    @Inject
    private AvaliacaoService avaliacaoService;

    @Inject
    private BaseService baseService;

    @Inject
    private EmpresaService empresaService;

    @Inject
    private ArquivoLoteService arquivoLoteService;

    @Inject
    private ParametroSistemaService parametroSistemaService;
    
    @Inject
    private TramiteRequisicaoService tramiteService;
    
    @Inject
    private AuditoriaService auditoriaService;

    private LazyDataModel<RequisicaoVO> listRequisicaoModel;

    private LazyDataModel<ArquivoLoteVO> listRelatorioAtendimentoLote;

    private boolean pesquisaRealizada;

    private UploadedFile file;

    private String idRequisicao;

    private String numItem;

    private Boolean possuiItens = false;

    private OcorrenciaAtendimentoEnum[] ocorrenciasAtendimento;

    private TipoSuporteEnum[] tipoSuportes;

    byte[] arquivoDownload;

    private FiltroRequisicaoDTO filtroRequisicao;

    private String coUsuarioFiltro;

    private String filtroHistorico;

    private String nomeArquivoLogLote;

    private List<RequisicaoVO> listaFiltro;

    private RequisicaoVO requisicaoSelecionada;

    private RequisicaoDocumentoVO requisicaoDocumentoVO;

    private List<TramiteRequisicaoVO> listaTramiteRequisicao;
    
    private List<TramiteRequisicaoVO> listAtendimentos;

    private TramiteRequisicaoVO tramiteRequisicao;

    private AvaliacaoRequisicaoVO avaliacaoSelecionada;

    private Boolean apenasMinhasRequisicoes;

    private EmpresaVO empresaUsuario;

    private Boolean formAtendimentoVisivel;

    private Boolean uploadAtendimentoVisivel;

    @PostConstruct
    protected void init() {
        arquivoDownload = null;

        setOcorrenciasAtendimento(OcorrenciaAtendimentoEnum.values());

        setTipoSuportes(TipoSuporteEnum.values());

        requisicaoDocumentoVO = new RequisicaoDocumentoVO();
        
        if (!ObjectUtils.isNullOrEmpty(JSFUtil.getUsuario())) {
        	empresaUsuario = empresaService.obterEmpresaCNPJ(((UsuarioLdap) JSFUtil.getUsuario()).getNuCnpj());
        }

        limparCamposFiltros();
    }

    public void limparCamposFiltros() {
        this.filtroRequisicao = new FiltroRequisicaoDTO();
        this.filtroRequisicao.setUnidadeSolicitante(new UnidadeVO());
        this.filtroRequisicao.setEmpresa(empresaUsuario);
        this.filtroRequisicao.setDataInicio(Util.getDataMesPassado());

        this.listRequisicaoModel = null;
    }

    public void pesquisarUnidadeSolicitante() {

        try {

            if (!ObjectUtils.isNullOrEmpty(filtroRequisicao.getUnidadeSolicitante().getId())) {
                //filtroRequisicao.setUnidadeSolicitante(unidadeService.findById((Long) filtroRequisicao.getUnidadeSolicitante().getId()));
                UnidadeVO unidade = this.unidadeService.findById((Long)this.filtroRequisicao.getUnidadeSolicitante().getId());

                if (!ObjectUtils.isNullOrEmpty(unidade)) {
                    List<BaseVO> basesUnidade = this.baseService.consultaBasePorIdUnidade((Long) filtroRequisicao.getUnidadeSolicitante().getId());
                    List<BaseVO> basesEmpresaContrato = this.baseService.consultaBasesEmpresaContrato();

                    if (!ObjectUtils.isNullOrEmpty(basesUnidade) && !basesEmpresaContrato.contains(basesUnidade.get(0))) {
                        throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_UNIDADE_GERADORA_NAO_PERMITIDA));
                    }
                    
                    this.filtroRequisicao.setUnidadeSolicitante(unidade);
                } else {
                    throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_UNIDADE_NAO_CADASTRADA));
                }

            } else {
                this.filtroRequisicao.setUnidadeSolicitante(new UnidadeVO());
            }

            JavaScriptUtils.update("painelButtom");

        } catch (BusinessException e) {
            UnidadeVO unidade = unidadeService.findById((Long) filtroRequisicao.getUnidadeSolicitante().getId());
            if (!ObjectUtils.isNullOrEmpty(unidade)) {
                filtroRequisicao.setUnidadeSolicitante(unidade);
            }
            facesMessager.addMessageError(e.getMessage());

        } catch (Exception e) {
            filtroRequisicao.setUnidadeSolicitante(new UnidadeVO());
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_CONSULTA_REQUISICAO, EVENTO_PESQUISAR_UNIDADE_SOLICITANTE));
        }
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

    public void localizar() {
        arquivoDownload = null;

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
                filtroPorNumero.setEmpresa(empresaUsuario);
                filtroPorNumero.setNumeroRequisicoes(this.filtroRequisicao.getNumeroRequisicoes());
                filtro.put("filtroDTO", filtroPorNumero);
            }

            this.resetarDataListResultados(ID_DATATABLE_RESULTADOS);
            this.listRelatorioAtendimentoLote = null;

            PaginatorModel<RequisicaoVO> paginator = new PaginatorModel<RequisicaoVO>(this.requisicaoService, filtro);
            this.listRequisicaoModel = paginator.getListModel();
            this.listRequisicaoModel.load(0, 1, null, null, null);

            this.setApenasMinhasRequisicoes(false);
        }
    }

    public void listarRelatoriosAtendimentoLote() {

        Map<String, Object> filtro = new HashMap<String, Object>();

        FiltroArquivoLoteDTO filtroArquivo = new FiltroArquivoLoteDTO();
        filtroArquivo.setUsuario((UsuarioLdap) RequestUtils.getSessionValue("usuario"));
        filtroArquivo.setDataFim(new Date());

        try {

            ParametroSistemaVO parametro = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_PZ_MANUTENCAO_ARQUIVOS);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, Integer.valueOf(parametro.getVlParametroSistema()) * -1);
            filtroArquivo.setDataInicio(calendar.getTime());

        } catch (Exception e) {
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.exception.InvalidSystemParameter"));
        }

        filtro.put("filtroDTO", filtroArquivo);

        this.resetarDataListResultados(ID_DATATABLE_RELATORIOS_LOTE);
        this.listRequisicaoModel = null;

        PaginatorModel<ArquivoLoteVO> paginator = new PaginatorModel<ArquivoLoteVO>(this.arquivoLoteService, filtro);
        this.listRelatorioAtendimentoLote = paginator.getListModel();
        this.listRelatorioAtendimentoLote.load(0, 1, null, null, null);
    }

    private void resetarDataListResultados(String idDataTable) {
        DataTable dataList = (DataTable) JavaScriptUtils.findComponentById(idDataTable);
        if (!ObjectUtils.isNullOrEmpty(dataList)) {
            dataList.setFirst(0);
            dataList.setRows(REGISTROS_POR_PAGINA);
        }
    }

    private Boolean validarCampos() {
        // if (ObjectUtils.isNullOrEmpty(filtroRequisicao.getNumeroRequisicoes())
        // && ObjectUtils.isNullOrEmpty(filtroRequisicao.getMatriculaUsuario())
        // && ObjectUtils.isNullOrEmpty(filtroRequisicao.getDataInicio())
        // && ObjectUtils.isNullOrEmpty(filtroRequisicao.getDataFim())
        // && (ObjectUtils.isNullOrEmpty(filtroRequisicao.getUnidadeSolicitante()) || ObjectUtils.isNullOrEmpty(filtroRequisicao.getUnidadeSolicitante()
        // .getId())) && ObjectUtils.isNullOrEmpty(filtroRequisicao.getMotivo()) && ObjectUtils.isNullOrEmpty(filtroRequisicao.getSituacao())
        // && ObjectUtils.isNullOrEmpty(filtroRequisicao.getBase())) {
        // facesMessager.addMessageError(MensagemUtils.obterMensagem("MA054"));
        // return false;
        // }

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
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_DT_INICIO_MENOR_DT_FIM));
                return false;
            }
        }

        return true;
    }

    private Boolean isDatasFiltroPreenchidas() {
        if (ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataInicio())) {
            return false;
        }

        return true;
    }

    private void ajustarPeriodoFiltro() {
        if (!ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataInicio()) && ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataFim())) {
            this.filtroRequisicao.setDataFim(Calendar.getInstance().getTime());
        }

        this.filtroRequisicao.setDataInicio(DateUtils.fitAtStart(this.filtroRequisicao.getDataInicio()));
        this.filtroRequisicao.setDataFim(DateUtils.fitAtEnd(this.filtroRequisicao.getDataFim()));
    }

    public void informarDataInicioObrigatoria() {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.dataInicio"));
    }

    public void onVisualizarDados(SelectEvent event) {
        setRequisicaoSelecionada((RequisicaoVO) event.getObject());
        JavaScriptUtils.showModal(MODAL_DETALHE_DOCUMENTO);
    }

    public void onVisualizarHistorico(RequisicaoVO requisicao) {
        try {
            requisicaoSelecionada = requisicao;

            listaTramiteRequisicao = tramiteRequisicaoService.obterTramitesPorRequisicao((Long) requisicao.getId());

            JavaScriptUtils.showModal(MODAL_HISTORICO);
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }
    }

    public void onMotivoReabertura(RequisicaoVO requisicao) {
        try {
            requisicaoSelecionada = requisicao;

            avaliacaoSelecionada = avaliacaoService.obterPorRequisicao((Long) requisicao.getId());

            if (ObjectUtils.isNullOrEmpty(avaliacaoSelecionada)) {
                avaliacaoSelecionada = new AvaliacaoRequisicaoVO();
                avaliacaoSelecionada.setTramite(requisicaoSelecionada.getTramiteRequisicaoAtual());
                UsuarioLdap usuarioLogado = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
                if (!ObjectUtils.isNullOrEmpty(usuarioLogado)) {
                    avaliacaoSelecionada.setCodigoUsuario(usuarioLogado.getEmail());
                }
            }

            JavaScriptUtils.showModal(MODAL_REABERTURA);
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }
    }

    public void onAtenderRequisicao(RequisicaoVO requisicao) {
        try {

            requisicaoSelecionada = requisicao;
            requisicaoSelecionada.getTramiteRequisicaoAtual().setArquivoDisponibilizado(StringUtils.EMPTY);

            tramiteRequisicao = new TramiteRequisicaoVO();
            Util.copiarInformacoesTramite(requisicao.getTramiteRequisicaoAtual(), tramiteRequisicao);

            if (ObjectUtils.isNullOrEmpty(tramiteRequisicao.getSuporte())) {
                tramiteRequisicao.setSuporte(new SuporteVO());
            }
            tramiteRequisicao.setOcorrencia(new OcorrenciaAtendimentoVO());
            tramiteRequisicao.getOcorrencia().setId(OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor());

            this.inicializarCamposModalAtendimento(requisicao);

            JavaScriptUtils.showModal(MODAL_ATENDIMENTO);

        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }
    }

    private void inicializarCamposModalAtendimento(RequisicaoVO requisicao) {

        Long idSituacao = (Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId();
        if (!SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId().equals(idSituacao) && !SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId().equals(idSituacao)) {

            TramiteRequisicaoVO tramiteAtual = requisicao.getTramiteRequisicaoAtual();
            if (SituacaoRequisicaoEnum.ABERTA.getId().equals(tramiteAtual.getSituacaoRequisicao().getId())
                    || SituacaoRequisicaoEnum.REABERTA.getId().equals(tramiteAtual.getSituacaoRequisicao().getId())) {

                JavaScriptUtils.enableComponents(ID_OCORRENCIA_ATENDIMENTO, ID_QTD_DISPONIBILIZADA_ATENDIMENTO, ID_TIPO_SUPORTE_ATENDIMENTO);
            } else {
                JavaScriptUtils.disableComponents(ID_OCORRENCIA_ATENDIMENTO, ID_QTD_DISPONIBILIZADA_ATENDIMENTO, ID_TIPO_SUPORTE_ATENDIMENTO);
            }

            this.setFormAtendimentoVisivel(true);
            this.setUploadDocumentoVisivel(false);

        } else {

            this.setFormAtendimentoVisivel(false);
            this.setUploadDocumentoVisivel(true);
        }
    }

    public void visualizarRequisicao(RequisicaoVO requisicao) {
        try {
            this.requisicaoSelecionada = this.requisicaoService.findByIdEager((Long) requisicao.getId());
            this.listAtendimentos = this.tramiteService.findAtendimentosRequisicao(this.requisicaoSelecionada);
            GrupoCamposHelper.getValorCamposDinamicos(this.requisicaoSelecionada, this.requisicaoSelecionada.getDocumento().getGrupo().getGrupoCampos());
            JavaScriptUtils.showModal(MODAL_RESUMO);

        } catch (AppException e) {
            facesMessager.addMessageError(e.getMessage());
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
    
    public StreamedContent fazerDownload(RequisicaoVO requisicao) {
        try {
            String fileName = System.getProperty(Constantes.PATH_ARQUIVOS_SERVIDOR_EXTRA).concat(
                    requisicao.getTramiteRequisicaoAtual().getArquivoDisponibilizado());
            if (!new File(fileName).exists()) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.exception.fileNotFound"));
            } else {
            	this.auditoriaService.gravar(requisicao, this.getUsuarioAutenticado());
                return RequestUtils.download(new File(fileName), requisicao.getTramiteRequisicaoAtual().getArquivoDisponibilizado());
            }
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }
        return null;
    }

    public StreamedContent fazerDownloadRelatorio(ArquivoLoteVO arquivoLote) {

        try {

            File relatorio = new File(System.getProperty(Constantes.EXTRANET_DIRETORIO_RELATORIOS) + FileUtils.SYSTEM_FILE_SEPARATOR
                    + arquivoLote.getRelatorioLote());

            if (!relatorio.exists()) {
                throw new FileNotFoundException();
            }

            return RequestUtils.download(relatorio, arquivoLote.getRelatorioLote());

        } catch (FileNotFoundException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.exception.fileNotFound"));
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }

        return null;
    }

    public StreamedContent exportarFormatoExcel() {
        return this.exportar(new ExportarRequisicaoXLS(), MensagemUtils.obterMensagem("requisicao.consulta.label.arquivoExportacaoXLS"));
    }

    public StreamedContent exportarFormatoCSV() {
        return this.exportar(new ExportarRequisicaoCSV(), MensagemUtils.obterMensagem("requisicao.consulta.label.arquivoExportacaoCSV"));
    }

    private StreamedContent exportar(DataExportable<RequisicaoVO> exportador, String nomeArquivo) {
        try {
            Map<String, Object> filtro = new HashMap<String, Object>();

            if (!ObjectUtils.isNullOrEmpty(this.filtroRequisicao.getDataInicio())) {
                this.ajustarPeriodoFiltro();
            }

            filtro.put("filtroDTO", this.filtroRequisicao);

            List<RequisicaoVO> datasource = this.requisicaoService.pesquisar(filtro);
            exportador.setData(datasource);

            String caminho = System.getProperty(Constantes.PATH_ARQUIVOS_SERVIDOR_EXTRA);
            String filename = FileUtils.appendDateTimeToFileName(nomeArquivo, new Date());
            File relatorio = exportador.export(caminho + filename);

            return RequestUtils.download(relatorio, relatorio.getName());
        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
            logger.error(e.getStackTrace());
        } catch (FileNotFoundException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.exception.fileNotFound"));
        } catch (IOException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.exception.io"));
        } catch (AppException e) {
            logger.error(e.getStackTrace());
        } catch (Exception e) {
          facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.exception.qtd.reg.excedido"));
          LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_CONSULTA_REQUISICAO, "Exportação Excel");
        }

        return null;
    }

    public void validarTipoArquivo() {

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String filename = StringUtils.defaultString(params.get("filename"), StringUtils.EMPTY);

        validarTipoArquivo(filename);
    }

    public Boolean validarTipoArquivo(String filename) {
        if (!filename.toLowerCase().endsWith(".pdf") && !filename.toLowerCase().endsWith(".zip")) {

            file = null;
            requisicaoSelecionada.getTramiteRequisicaoAtual().setArquivoDisponibilizado(StringUtils.EMPTY);

            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_FORMATO_ARQUIVO_INVALIDO));
            return false;
        }

        return true;
    }

    public void atenderRequisicaoDocDigital() {
        try {

            if (ObjectUtils.isNullOrEmpty(this.file)) {
                throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.INFORMACAO_SELECIONE_ARQUIVO));
            }

            if (this.validarCamposAtendimento() && this.validarTipoArquivo(this.file.getFileName())) {

                requisicaoSelecionada.getTramiteRequisicaoAtual().setArquivoDisponibilizado("Arquivo: " + this.file.getFileName());
                tramiteRequisicaoService.salvarTramiteRequisicao(requisicaoSelecionada, tramiteRequisicao, file,
                        TerceirizadaRequisicaoHelper.gerarNomeArquivo(this.file.getFileName(), requisicaoSelecionada.getCodigoRequisicao().toString()));

                this.file = null;
                Long idSituacao = (Long) requisicaoSelecionada.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId();

                if (idSituacao.equals(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId())
                        || idSituacao.equals(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId())) {
                    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS040"));
                } else {
                    // TODO: #SIRED-RC Criar mensagem no caso de uso
                    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS027"));
                }

                JavaScriptUtils.update("formConsulta");
                this.localizar();
            }

        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }
    }

    public void atenderRequisicao() {
        try {

            if (this.validarCamposAtendimento()) {
                tramiteRequisicaoService.salvarTramiteRequisicao(requisicaoSelecionada, tramiteRequisicao, null, null);
                JavaScriptUtils.execute(MODAL_ATENDIMENTO_HIDE);

                facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS027"));

                JavaScriptUtils.update("formConsulta");
                this.localizar();
            }
        } catch (BusinessException ex) {
            facesMessager.addMessageError(ex.getMessage());
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }
    }

    private Boolean validarCamposAtendimento() {
        try {

            if (tramiteRequisicao.getOcorrencia() == null || tramiteRequisicao.getOcorrencia().getId() == null) {
                throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_CAMPO_OCORRENCIA_ATENDIMENTO_VAZIO));
            } else if ((tramiteRequisicao.getOcorrencia().getId().equals(OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor()))
                    || (tramiteRequisicao.getOcorrencia().getId().equals(OcorrenciaAtendimentoEnum.ORIGINAL_UNIDADE.getValor()))
                    || (tramiteRequisicao.getOcorrencia().getId().equals(OcorrenciaAtendimentoEnum.COPIA_AUTENTICADA.getValor()))) {
                if (tramiteRequisicao.getQtdDisponibilizadaDocumento() == null) {
                    throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_QTD_DISPONIBILIZADA_INVALIDA));
                } else if (tramiteRequisicao.getSuporte() == null || tramiteRequisicao.getSuporte().getId() == null) {
                    throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_TIPO_SUPORTE_VAZIO));
                }
            }

            return true;
        } catch (BusinessException ex) {
            facesMessager.addMessageError(ex.getMessage());
            return false;
        }
    }

    public boolean habilitaMotivoReabertura(RequisicaoVO requisicao) {
        Long situacao = (Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId();

        return (situacao.equals(SituacaoRequisicaoEnum.REABERTA.getId()) || situacao.equals(SituacaoRequisicaoEnum.REABERTA_EM_TRATAMENTO.getId())
                || situacao.equals(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId())
                || situacao.equals(SituacaoRequisicaoEnum.REABERTA_PEND_DADOS_FAT.getId()) || situacao.equals(SituacaoRequisicaoEnum.REATENDIDA.getId()) || (situacao
                .equals(SituacaoRequisicaoEnum.FECHADA.getId()) && (avaliacaoService.obterPorRequisicao((Long) requisicao.getId()) != null)));
    }

    public boolean habilitaAtenderRequisicao(RequisicaoVO requisicao) {
        Long situacao = (Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId();

        return (situacao.equals(SituacaoRequisicaoEnum.ABERTA.getId()))
                || (situacao.equals(SituacaoRequisicaoEnum.REABERTA.getId()) || (situacao.equals(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId())) || (situacao
                        .equals(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId())));
    }

    public void onChangeOcorrenciaAtendimento() {

        OcorrenciaAtendimentoVO ocorrencia = this.tramiteRequisicao.getOcorrencia();
        if (OcorrenciaAtendimentoEnum.INCONSISTENCIA.getValor().equals(ocorrencia.getId())
                || OcorrenciaAtendimentoEnum.NAO_LOCALIZADO.getValor().equals(ocorrencia.getId())
                || OcorrenciaAtendimentoEnum.NAO_RECEPCIONADO.getValor().equals(ocorrencia.getId())
                || OcorrenciaAtendimentoEnum.PRAZO_EXPIRADO.getValor().equals(ocorrencia.getId())
                || OcorrenciaAtendimentoEnum.SEM_MICROFORMAS.getValor().equals(ocorrencia.getId())
                || OcorrenciaAtendimentoEnum.SEM_MOVIMENTAÇAO.getValor().equals(ocorrencia.getId())) {

            this.tramiteRequisicao.getSuporte().setId(null);
            this.tramiteRequisicao.setQtdDisponibilizadaDocumento(null);
            JavaScriptUtils.disableComponents(ID_QTD_DISPONIBILIZADA_ATENDIMENTO, ID_TIPO_SUPORTE_ATENDIMENTO);

        } else {
            JavaScriptUtils.enableComponents(ID_QTD_DISPONIBILIZADA_ATENDIMENTO, ID_TIPO_SUPORTE_ATENDIMENTO);
        }
    }

    public void onClickBotaoProximo() {
        if (this.validarCamposAtendimento()) {
            this.setFormAtendimentoVisivel(false);
            this.setUploadDocumentoVisivel(true);
        }
    }

    public Boolean isBotaoAtenderVisivel() {
        return !OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor().equals(this.tramiteRequisicao.getOcorrencia().getId());
    }

    public Boolean isBotaoProximoVisivel() {
        return OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor().equals(this.tramiteRequisicao.getOcorrencia().getId());
    }

    public Boolean isFormAtendimentoVisivel() {
        return this.formAtendimentoVisivel;
    }

    public void setFormAtendimentoVisivel(Boolean visivel) {
        this.formAtendimentoVisivel = visivel;
    }

    public Boolean isUploadDocumentoVisivel() {
        return this.uploadAtendimentoVisivel;
    }

    public void setUploadDocumentoVisivel(Boolean visivel) {
        this.uploadAtendimentoVisivel = visivel;
    }

    public String obterMensagemQuantidade() {
        Integer qtdDisponibilizada = tramiteRequisicao.getQtdDisponibilizadaDocumento();
        Integer qtdSolicitada = requisicaoSelecionada.getQtSolicitadaDocumento();

        String mensagem = StringUtils.EMPTY;

        if ((qtdDisponibilizada != null) && (qtdSolicitada != null)) {
            if (qtdDisponibilizada.intValue() > qtdSolicitada.intValue()) {
                mensagem = MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_QTD_DISPONIBILIZADA_MAIOR_QUE_SOLICITADA);
            } else if (qtdDisponibilizada.intValue() < qtdSolicitada.intValue()) {
                mensagem = MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_QTD_DISPONIBILIZADA_MENOR_QUE_SOLICITADA);
            }
        }

        return mensagem;
    }

    public void uploadLoteAtendimento() {
        try {
            if (!Util.isNullOuVazio(file)) {
                if (validarTipoArquivo(getFile().getContentType(), getFile().getFileName())) {
                    processarArquivoCSV(file);
                } else {
                    file = null;
                }
            }
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.message.validation.arquivo.upload.falha"));
        }
    }

    private void processarArquivoCSV(UploadedFile arquivo) {
        limparCamposFiltros();

        ProcessaArquivoLote processaArquivoLote = new ProcessaArquivoLote(arquivoLoteService, requisicaoService);

        try {
            List<TramiteRequisicaoVO> tramitesRequisicao = processaArquivoLote.processarArquivoCSV(arquivo);

            if (ObjectUtils.isNullOrEmpty(processaArquivoLote.getLinhas())) {
                validarArquivoProcessado(tramitesRequisicao);
            }

            processaArquivoLote.downloadArquivoLog(arquivo);

            arquivoDownload = processaArquivoLote.getArquivoDownload();

            nomeArquivoLogLote = processaArquivoLote.getNomeArquivoLogLote();
        } catch (RequiredException e) {
            facesMessager.addMessageError(e.getMessage());
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_CONSULTA_REQUISICAO, EVENTO_PROCESSAMENTO_EM_LOTE));
        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_CONSULTA_REQUISICAO, EVENTO_PROCESSAMENTO_EM_LOTE));
        } catch (IllegalArgumentException e) {
            arquivoDownload = null;
            facesMessager.addMessageError(e.getMessage());
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_CONSULTA_REQUISICAO, EVENTO_PROCESSAMENTO_EM_LOTE));
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.message.validation.arquivo.upload.falha"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_CONSULTA_REQUISICAO, EVENTO_PROCESSAMENTO_EM_LOTE));
        }
    }

    private void validarArquivoProcessado(List<TramiteRequisicaoVO> tramitesRequisicao) throws BusinessException {
        if ((tramitesRequisicao != null) && (!tramitesRequisicao.isEmpty())) {
            SituacaoRequisicaoEnum situacao = tramiteRequisicaoService.salvarTramiteRequisicao(tramitesRequisicao);

            facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS040"));

            if (!situacao.getId().equals(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId())) {
                facesMessager.addMessageInfo(MensagemUtils.obterMensagem("requisicao.atendimento.lote.sem.pendencia"));
            }
            arquivoDownload = null;

            limparCamposFiltros();

            StringBuilder numeroRequisicoes = new StringBuilder();
            boolean add = false;
            for (TramiteRequisicaoVO tramiteRequisicao : tramitesRequisicao) {
                if (add) {
                    numeroRequisicoes.append(",");
                }
                add = true;
                numeroRequisicoes.append(tramiteRequisicao.getRequisicao().getCodigoRequisicao());
            }

            filtroRequisicao.setNumeroRequisicoes(numeroRequisicoes.toString());

            localizar();
        }
    }

    private boolean validarTipoArquivo(String contentType, String fileName) {
        if (!contentType.equals(Constantes.CONTENT_TYPE_TXT)
                && !(contentType.equals(Constantes.CONTENT_TYPE_CSV) && fileName.length() > 3 && fileName.substring(fileName.length() - 3).equals("csv"))) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("geral.message.validation.formato.requisicao.lote.invalido"));
            arquivoDownload = null;
            return false;
        }

        return true;
    }

    public void downloadArquivoLog() {
        try {
            TerceirizadaRequisicaoHelper.downloadArquivoLog(getNomeArquivoLogLote(), arquivoDownload);
        } catch (IOException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "ConsultarRequisicao", "exportarDatagrid"));
        } finally {
            RequestContext.getCurrentInstance().execute("hideStatus();");
            FacesContext faces = FacesContext.getCurrentInstance();
            faces.responseComplete();
        }
    }

    public boolean getPesquisaRealizada() {
        return pesquisaRealizada;
    }

    public void setPesquisaRealizada(boolean pesquisaRealizada) {
        this.pesquisaRealizada = pesquisaRealizada;
    }

    public List<RequisicaoVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<RequisicaoVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public String getCoUsuarioFiltro() {
        return coUsuarioFiltro;
    }

    public void setCoUsuarioFiltro(String coUsuarioFiltro) {
        this.coUsuarioFiltro = coUsuarioFiltro;
    }

    public TramiteRequisicaoVO getTramiteRequisicao() {
        return tramiteRequisicao;
    }

    public void setTramiteRequisicao(TramiteRequisicaoVO tramiteRequisicao) {
        this.tramiteRequisicao = tramiteRequisicao;
    }

    public RequisicaoDocumentoVO getRequisicaoDocumentoVO() {
        return requisicaoDocumentoVO;
    }

    public void setRequisicaoDocumentoVO(RequisicaoDocumentoVO requisicaoDocumentoVO) {
        this.requisicaoDocumentoVO = requisicaoDocumentoVO;
    }

    public String getFiltroHistorico() {
        return filtroHistorico;
    }

    public void setFiltroHistorico(String filtroHistorico) {
        this.filtroHistorico = filtroHistorico;
    }

    public SituacaoRequisicaoEnum[] getListaSituacoesTerceirizada() {
        return SituacaoRequisicaoEnum.getListaSituacoesTerceirizadaOrdemAlfabetica();
    }

    public List<GrupoCampoVO> getListGrupoCampos() {
        if (!ObjectUtils.isNullOrEmpty(this.requisicaoSelecionada)
                && !ObjectUtils.isNullOrEmpty(this.requisicaoSelecionada.getDocumento().getGrupo().getGrupoCampos())) {
            return CollectionUtils.asSortedList(this.requisicaoSelecionada.getDocumento().getGrupo().getGrupoCampos());
        }

        return new ArrayList<GrupoCampoVO>();
    }

    public void showMessageFromRemoteCommand() {

        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        Gson gson = new Gson();
        RemoteCommandMessage[] messages = gson.fromJson(params.get("messages"), RemoteCommandMessage[].class);

        for (RemoteCommandMessage message : messages) {
            if (RemoteCommandMessageTypeEnum.ERROR.equals(message.getType())) {
                facesMessager.addMessageError(message.getMessage());
            } else if (RemoteCommandMessageTypeEnum.WARNING.equals(message.getType())) {
                facesMessager.addMessageWarn(message.getMessage());
            } else {
                facesMessager.addMessageInfo(message.getMessage());
            }
        }

        JavaScriptUtils.execute("bindOnChangeMessage();");
    }

    public String getDataFormatadaTramite(TramiteRequisicaoVO tramite) {
        if (SituacaoRequisicaoEnum.ATENDIDA.getId().equals(tramite.getSituacaoRequisicao().getId())
                || SituacaoRequisicaoEnum.REATENDIDA.getId().equals(tramite.getSituacaoRequisicao().getId())) {
            return DateUtils.format(tramite.getDataHoraAtendimento(), DateUtils.DATETIME_FORMAT);
        }

        return DateUtils.format(tramite.getDataHora(), DateUtils.DATETIME_FORMAT);
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public byte[] getArquivoDownload() {
        return arquivoDownload;
    }

    public void setArquivoDownload(byte[] arquivoDownload) {
        this.arquivoDownload = arquivoDownload;
    }

    public String getIdRequisicao() {
        return idRequisicao;
    }

    public void setIdRequisicao(String idRequisicao) {
        this.idRequisicao = idRequisicao;
    }

    public String getNumItem() {
        return numItem;
    }

    public void setNumItem(String numItem) {
        this.numItem = numItem;
    }

    public Boolean getPossuiItens() {
        return possuiItens;
    }

    public void setPossuiItens(Boolean possuiItens) {
        this.possuiItens = possuiItens;
    }

    public RequisicaoVO getRequisicaoSelecionada() {
        return requisicaoSelecionada;
    }

    public void setRequisicaoSelecionada(RequisicaoVO requisicaoSelecionada) {
        this.requisicaoSelecionada = requisicaoSelecionada;
    }

    public List<TramiteRequisicaoVO> getListaTramiteRequisicao() {
        return listaTramiteRequisicao;
    }

    public void setListaTramiteRequisicao(List<TramiteRequisicaoVO> listaTramiteRequisicao) {
        this.listaTramiteRequisicao = listaTramiteRequisicao;
    }

    public AvaliacaoRequisicaoVO getAvaliacaoSelecionada() {
        return avaliacaoSelecionada;
    }

    public void setAvaliacaoSelecionada(AvaliacaoRequisicaoVO avaliacaoSelecionada) {
        this.avaliacaoSelecionada = avaliacaoSelecionada;
    }

    public LazyDataModel<RequisicaoVO> getListRequisicaoModel() {
        return listRequisicaoModel;
    }

    public void setListRequisicaoModel(LazyDataModel<RequisicaoVO> listRequisicaoModel) {
        this.listRequisicaoModel = listRequisicaoModel;
    }

    public LazyDataModel<ArquivoLoteVO> getListRelatorioAtendimentoLote() {
        return listRelatorioAtendimentoLote;
    }

    public void setListRelatorioAtendimentoLote(LazyDataModel<ArquivoLoteVO> listRelatorioAtendimentoLote) {
        this.listRelatorioAtendimentoLote = listRelatorioAtendimentoLote;
    }

    public Boolean getApenasMinhasRequisicoes() {
        return apenasMinhasRequisicoes;
    }

    public void setApenasMinhasRequisicoes(Boolean apenasMinhasRequisicoes) {
        this.apenasMinhasRequisicoes = apenasMinhasRequisicoes;
    }

    public FiltroRequisicaoDTO getFiltroRequisicao() {
        return filtroRequisicao;
    }

    public void setFiltroRequisicao(FiltroRequisicaoDTO filtroRequisicao) {
        this.filtroRequisicao = filtroRequisicao;
    }

    public OcorrenciaAtendimentoEnum[] getOcorrenciasAtendimento() {
        return ocorrenciasAtendimento;
    }

    public void setOcorrenciasAtendimento(OcorrenciaAtendimentoEnum[] ocorrenciasAtendimento) {
        this.ocorrenciasAtendimento = ocorrenciasAtendimento;
    }

    public TipoSuporteEnum[] getTipoSuportes() {
        return tipoSuportes;
    }

    public void setTipoSuportes(TipoSuporteEnum[] tipoSuportes) {
        this.tipoSuportes = tipoSuportes;
    }

    public String getNomeArquivoLogLote() {
        return nomeArquivoLogLote;
    }

    public void setNomeArquivoLogLote(String nomeArquivoLogLote) {
        this.nomeArquivoLogLote = nomeArquivoLogLote;
    }

    /**
     * Verifica se o campo é do tipo InputMask tags: #requisicao #form #campos #dinamicos
     * 
     * @param campo
     * @return <b>True</b> se o campo é do tipo InputMask e <b>False</b> caso contrário
     */
    public Boolean isCampoInputMask(CampoVO campo) {
        return SiredUtils.isCampoInputMask(campo);
    }
    
    public List<TramiteRequisicaoVO> getListAtendimentos() {
        return listAtendimentos;
    }
}
