package br.gov.caixa.gitecsa.sired.extra.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
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
import br.gov.caixa.gitecsa.sired.arquitetura.web.PaginatorModel;
import br.gov.caixa.gitecsa.sired.dto.FiltroDocumentoOriginalDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoDocumentoOriginalEnum;
import br.gov.caixa.gitecsa.sired.exporter.ExportarDocumentoOriginalXLS;
import br.gov.caixa.gitecsa.sired.extra.service.BaseService;
import br.gov.caixa.gitecsa.sired.extra.service.DocumentoOriginalService;
import br.gov.caixa.gitecsa.sired.extra.service.EmpresaService;
import br.gov.caixa.gitecsa.sired.extra.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.extra.service.TramiteDocumentoOriginalService;
import br.gov.caixa.gitecsa.sired.extra.service.TramiteRequisicaoService;
import br.gov.caixa.gitecsa.sired.extra.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
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
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteDocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class DocumentoOriginalController implements Serializable {

    private static final long serialVersionUID = 8688523472148339040L;

    private static final int INTERVALO_DIAS_FILTRO = -60;

    private static final int REGISTROS_POR_PAGINA = 10;

    private static final String ID_MODAL_DATA = "mdlDtNaoInformada";

    private static final String ID_MODAL_HISTORICO = "mdlHistorico";

    private static final String ID_MODAL_INFORMACOES = "mdlInfo";
    
    private static final String ID_DATALIST_RESULTADOS = "formConsulta:tabela";

    private static final String ID_MODAL_RECEPCIONAR = "mdlRecepcionar";

    private static final String EVENTO_PESQUISAR_UNIDADE_SOLICITANTE = "pesquisarUnidadeSolicitante";

    private static final String FUNCIONALIDADE_CONSULTA_REQUISICAO = "ConsultaRequisicaoController";

    @Inject
    protected FacesMensager facesMessager;

    @Inject
    private transient Logger logger;

    @Inject
    private RequisicaoService requisicaoService;
    
    @Inject
    private DocumentoOriginalService docOriginalService;

    @Inject
    private BaseService baseService;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private TramiteDocumentoOriginalService tramiteDocOriginalService;
    
    @Inject
    private TramiteRequisicaoService tramiteRequisicaoService;
    
    @Inject
    private EmpresaService empresaService;

    private FiltroDocumentoOriginalDTO filtroDocOriginal;

    private LazyDataModel<DocumentoOriginalVO> listDocOriginalModel;

    private List<BaseVO> listBase;

    private Set<GrupoCampoVO> listGrupoCampos;

    private List<TramiteRequisicaoVO> listAtendimentos;

    private List<TramiteDocumentoOriginalVO> listTramiteDocOriginal;

    private DocumentoOriginalVO docOriginalSelecionado;
    
    private RequisicaoVO requisicaoSelecionada;

    private String notificaoUnidadeNaoPermitida;
    
    private String observacaoTramite;
    
    private EmpresaVO empresaUsuario;

    @PostConstruct
    public void init() {

        try {

            this.limparCamposFiltros();
            this.listBase = this.baseService.findAll();
            Collections.sort(this.listBase);
            this.filtroDocOriginal.setDataInicio(Util.getDataMesPassado());
            this.empresaUsuario = this.empresaService.obterEmpresaCNPJ(((UsuarioLdap)RequestUtils.getSessionValue("usuario")).getNuCnpj());

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

            if (!ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getUnidadeSolicitante().getId())) {
                UnidadeVO unidade = this.unidadeService.findById((Long) this.filtroDocOriginal.getUnidadeSolicitante().getId());

                if (!ObjectUtils.isNullOrEmpty(unidade)) {
                    List<BaseVO> basesUnidade = baseService.consultaBasePorIdUnidade((Long) this.filtroDocOriginal.getUnidadeSolicitante().getId());
                    List<BaseVO> basesEmpresaContrato = baseService.consultaBasesEmpresaContrato();

                    if (!ObjectUtils.isNullOrEmpty(basesUnidade) && !basesEmpresaContrato.contains(basesUnidade.get(0))) {
                        throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_UNIDADE_GERADORA_NAO_PERMITIDA));
                    }
                    
                    this.filtroDocOriginal.setUnidadeSolicitante(unidade);
                    
                } else {
                    throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_UNIDADE_NAO_CADASTRADA));
                }

            } else {
                this.filtroDocOriginal.setUnidadeSolicitante(new UnidadeVO());
            }

            JavaScriptUtils.update("painelButtom");

        } catch (BusinessException e) {
            UnidadeVO unidade = unidadeService.findById((Long) this.filtroDocOriginal.getUnidadeSolicitante().getId());
            if (!ObjectUtils.isNullOrEmpty(unidade)) {
                this.filtroDocOriginal.setUnidadeSolicitante(unidade);
            }
            facesMessager.addMessageError(e.getMessage());

        } catch (Exception e) {
            this.filtroDocOriginal.setUnidadeSolicitante(new UnidadeVO());
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            this.logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), FUNCIONALIDADE_CONSULTA_REQUISICAO, EVENTO_PESQUISAR_UNIDADE_SOLICITANTE));
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
        this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.dataInicio"));
    }

    /**
     * Realiza a consulta de requisições
     */
    public void localizar() {

        if (this.validarCampos()) {

            Map<String, Object> filtro = new HashMap<String, Object>();

            if (StringUtils.isBlank(this.filtroDocOriginal.getNumeroRequisicoes())) {
                if (this.isDatasFiltroPreenchidas()) {
                    this.ajustarPeriodoFiltro();
                    filtro.put("filtroDTO", this.filtroDocOriginal);
                } else {
                    JavaScriptUtils.showModal(ID_MODAL_DATA);
                    return;
                }
            } else {
                FiltroDocumentoOriginalDTO filtroPorNumero = new FiltroDocumentoOriginalDTO();
                filtroPorNumero.setNumeroRequisicoes(this.filtroDocOriginal.getNumeroRequisicoes());
                filtro.put("filtroDTO", filtroPorNumero);
            }

            this.resetarDataListResultados();

            UsuarioLdap usuario = getUsuarioAutenticado();
            if (!this.unidadeService.isUnidadeAutorizada(this.filtroDocOriginal.getUnidadeSolicitante(), usuario)) {
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA040"));
            } else {
                PaginatorModel<DocumentoOriginalVO> paginator = new PaginatorModel<DocumentoOriginalVO>(this.docOriginalService, filtro); 
                this.listDocOriginalModel = paginator.getListModel();
                this.listDocOriginalModel.load(0, 1, null, null, null);

                JavaScriptUtils.update("formConsulta");
            }
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
        this.filtroDocOriginal.setDataFim(calendar.getTime());

        calendar.add(Calendar.DAY_OF_YEAR, INTERVALO_DIAS_FILTRO);
        this.filtroDocOriginal.setDataInicio(calendar.getTime());
        this.localizar();
    }

    /**
     * Redefine os filtros da consulta para o estado inicial
     */
    public void limparCamposFiltros() {

        this.filtroDocOriginal = new FiltroDocumentoOriginalDTO();
        this.filtroDocOriginal.setUnidadeSolicitante(new UnidadeVO());
        this.filtroDocOriginal.setEmpresa(this.empresaUsuario);
        this.filtroDocOriginal.setDataInicio(Util.getDataMesPassado());

        this.listDocOriginalModel = null;
        this.limparNotificacaoUnidade();
    }

    /**
     * Exibe o modal que contém o histórico dos tramites do documento original selecionada
     * 
     * @param docOriginal
     *            Documento orginal selecionado
     */
    public void visualizarHistoricoTramite(DocumentoOriginalVO docOriginal) {

        this.docOriginalSelecionado = docOriginal;
        this.setListTramiteDocOriginal(this.tramiteDocOriginalService.pesquisarHistorico(docOriginal));

        JavaScriptUtils.showModal(ID_MODAL_HISTORICO);
    }

    /**
     * Obtém os valores dos campos dinâmicos do formulário
     */
    public void getValorCamposDinamicos() {
        try {
            this.listGrupoCampos = GrupoCamposHelper.getValorCamposDinamicos(this.docOriginalSelecionado.getRequisicao(), this.listGrupoCampos);
        } catch (AppException e) {
            facesMessager.addMessageError(e.getMessage());
        }
    }

    /**
     * Exibe o modal que exibe as informações da requisição selecionada
     * 
     * @param docOriginal
     *            Requisição selecionada
     */
    public void visualizarRequisicao(DocumentoOriginalVO docOriginal) {
        
        this.requisicaoSelecionada = this.requisicaoService.findByIdEager((Long) docOriginal.getRequisicao().getId());
        
        this.docOriginalSelecionado = docOriginal;
        this.docOriginalSelecionado.setRequisicao(this.requisicaoSelecionada);
       
        this.listAtendimentos = this.tramiteRequisicaoService.findAtendimentosRequisicao(this.requisicaoSelecionada);
        this.listGrupoCampos = this.requisicaoSelecionada.getDocumento().getGrupo().getGrupoCampos();
        
        this.getValorCamposDinamicos();

        JavaScriptUtils.showModal(ID_MODAL_INFORMACOES);
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
     * @throws Exception 
     */
    public StreamedContent exportar() throws Exception {

        try {

            List<DocumentoOriginalVO> datasource = null;
            
            Map<String, Object> filtro = new HashMap<String, Object>();

            if (!ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getDataInicio())) {
                this.ajustarPeriodoFiltro();
            }
            
            filtro.put("filtroDTO", this.filtroDocOriginal);
            datasource = this.docOriginalService.pesquisar(filtro);

            ExportarDocumentoOriginalXLS exportador = new ExportarDocumentoOriginalXLS();
            exportador.setData(datasource);

            String caminho = System.getProperty(Constantes.CAMINHO_UPLOAD);
            String filename = FileUtils.appendDateTimeToFileName(MensagemUtils.obterMensagem("documentoOriginal.consulta.label.arquivoExportacaoXLS"), new Date());
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

        if (ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getNumeroRequisicoes())
                && ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getMatriculaUsuario())
                && ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getDataInicio())
                && ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getDataFim())
                && (ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getUnidadeSolicitante()) || ObjectUtils.isNullOrEmpty(this.filtroDocOriginal
                        .getUnidadeSolicitante().getId())) && ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getMotivo())
                && ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getSituacaoDocOriginal()) && ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getBase())) {
            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA054"));
            return false;
        }

        if (!ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getDataInicio()) && !ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getDataFim())) {

            Calendar dtInicio = Calendar.getInstance();
            dtInicio.setLenient(false);
            dtInicio.setTime(this.filtroDocOriginal.getDataInicio());

            if (dtInicio.after(Calendar.getInstance())) {
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA038"), "Data Início");
                return false;
            }

            Calendar dtFim = Calendar.getInstance();
            dtFim.setLenient(false);
            dtFim.setTime(this.filtroDocOriginal.getDataFim());

            if (dtInicio.after(dtFim)) {
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA018"));
                return false;
            }
        }

        return true;
    }
    
    /**
     * Verifica se os campos do tipo data foram preenchidos
     * 
     * @return <b>True</b> caso tenham sido preenchidos e <b>false</b> caso contrário.
     */
    private Boolean isDatasFiltroPreenchidas() {
        if (ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getDataInicio())) {
            return false;
        }

        return true;
    }

    /**
     * Realiza o ajuste no horário das datas e seta a data padrão para o campo "data fim", caso não esteja preenchido
     */
    private void ajustarPeriodoFiltro() {

        if (!ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getDataInicio()) && ObjectUtils.isNullOrEmpty(this.filtroDocOriginal.getDataFim())) {
            this.filtroDocOriginal.setDataFim(Calendar.getInstance().getTime());
        }

        this.filtroDocOriginal.setDataInicio(DateUtils.fitAtStart(this.filtroDocOriginal.getDataInicio()));
        this.filtroDocOriginal.setDataFim(DateUtils.fitAtEnd(this.filtroDocOriginal.getDataFim()));
    }
    
    public FiltroDocumentoOriginalDTO getFiltroDocOriginal() {
        return filtroDocOriginal;
    }

    public void setFiltroDocOriginal(FiltroDocumentoOriginalDTO filtroDocOriginal) {
        this.filtroDocOriginal = filtroDocOriginal;
    }

    public LazyDataModel<DocumentoOriginalVO> getListDocOriginalModel() {
        return listDocOriginalModel;
    }

    public void setListDocOriginalModel(LazyDataModel<DocumentoOriginalVO> listDocOriginalModel) {
        this.listDocOriginalModel = listDocOriginalModel;
    }

    public List<SituacaoDocumentoOriginalEnum> getListSituacao() {
        List<SituacaoDocumentoOriginalEnum> situacaoList = Arrays.asList(SituacaoDocumentoOriginalEnum.values());
        Collections.sort(situacaoList, new Comparator<SituacaoDocumentoOriginalEnum>() {

            @Override
            public int compare(SituacaoDocumentoOriginalEnum o1, SituacaoDocumentoOriginalEnum o2) {
                return o1.getLabel().compareToIgnoreCase(o2.getLabel());
            }
            
        });
        
        return situacaoList;
    }
    
    public Date getDataEnvioDocumentoOriginal(DocumentoOriginalVO docOriginal) {
        return this.tramiteDocOriginalService.getDataEnvio(docOriginal);
    }
    
	/**
	 * Verifica se o estado do DocumentoOriginalVO é passível de recebimento por parte da Terceirizada.
	 */
	public Boolean hasBotaoReceberTerceirizada(DocumentoOriginalVO docOriginal) {
		return docOriginal.getTramiteDocOriginalAtual().getSituacaoDocOriginal().equals(SituacaoDocumentoOriginalEnum.DEVOLVIDO);
	}
    
    public void confirmarRecebimento(DocumentoOriginalVO docOriginal) {
        this.docOriginalSelecionado = docOriginal;
        JavaScriptUtils.showModal(ID_MODAL_RECEPCIONAR);
    }
    
    public void registrarTramite(SituacaoDocumentoOriginalEnum situacao) {
        try {
            
            if (!ObjectUtils.isNullOrEmpty(this.observacaoTramite) && this.observacaoTramite.length() > 100) {
                throw new BusinessException(MensagemUtils.obterMensagem("MA055", "observação", "100"));
            }
            
            String message = StringUtils.EMPTY;
            String idModal = StringUtils.EMPTY;
            
            UsuarioLdap usuarioLogado = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
            TramiteDocumentoOriginalVO tramite = this.tramiteDocOriginalService.getBySituacao(situacao, usuarioLogado.getEmail());
            tramite.setDocumentoOriginal(this.docOriginalSelecionado);
            tramite.setObservacao(this.observacaoTramite);
            
            this.tramiteDocOriginalService.atualizarTramite(tramite);
            
            Long codigo = this.docOriginalSelecionado.getRequisicao().getCodigoRequisicao();
            
            this.docOriginalSelecionado = null;
            this.observacaoTramite = null;
            
            switch (situacao) {
                case RECEPCIONADO_TERCEIRIZADA:
                    idModal = ID_MODAL_RECEPCIONAR;
                    message = "Requisição Código %s recebida com sucesso.";
                    break;
            default:
                throw new IllegalArgumentException();
            }
            
            JavaScriptUtils.hideModal(idModal);
            this.facesMessager.addMessageInfo(String.format(message, codigo));
            
            this.localizar();
            
        } catch (BusinessException e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }
        
    }
    
    public Boolean isCampoInputMask(CampoVO campo) {
        return SiredUtils.isCampoInputMask(campo);
    }

    public List<BaseVO> getListBase() {
        return listBase;
    }

    public List<TramiteDocumentoOriginalVO> getListTramiteDocOriginal() {
        return listTramiteDocOriginal;
    }

    public void setListTramiteDocOriginal(List<TramiteDocumentoOriginalVO> listTramiteDocOriginal) {
        this.listTramiteDocOriginal = listTramiteDocOriginal;
    }

    public DocumentoOriginalVO getDocOriginalSelecionado() {
        return docOriginalSelecionado;
    }

    public void setDocOriginalSelecionado(DocumentoOriginalVO docOriginalSelecionado) {
        this.docOriginalSelecionado = docOriginalSelecionado;
    }

    public RequisicaoVO getRequisicaoSelecionada() {
        return requisicaoSelecionada;
    }

    public void setRequisicaoSelecionada(RequisicaoVO requisicaoSelecionada) {
        this.requisicaoSelecionada = requisicaoSelecionada;
    }

    public List<GrupoCampoVO> getListGrupoCampos() {
        if (!ObjectUtils.isNullOrEmpty(this.listGrupoCampos)) {
            return CollectionUtils.asSortedList(this.listGrupoCampos);
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

    public String getObservacaoTramite() {
        return observacaoTramite;
    }

    public void setObservacaoTramite(String observacaoTramite) {
        this.observacaoTramite = observacaoTramite;
    }
}
