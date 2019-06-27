package br.gov.caixa.gitecsa.sired.controller;

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
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.web.PaginatorModel;
import br.gov.caixa.gitecsa.sired.dto.FiltroDocumentoOriginalDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoDocumentoOriginalEnum;
import br.gov.caixa.gitecsa.sired.exporter.ExportarDocumentoOriginalXLS;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.BaseService;
import br.gov.caixa.gitecsa.sired.service.DocumentoOriginalService;
import br.gov.caixa.gitecsa.sired.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.service.TramiteDocumentoOriginalService;
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
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
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
    
    private static final String ID_MODAL_DEVOLVER = "mdlDevolver";
    
    private static final String ID_DATALIST_RESULTADOS = "formConsulta:tabela";

    private static final String ID_MODAL_RECEPCIONAR = "mdlRecepcionar";

    private static final String ID_MODAL_EXTRAVIO = "mdlExtravio";

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

    @PostConstruct
    public void init() {
        try {
            this.limparCamposFiltros();
            this.listBase = this.baseService.findAll();
            Collections.sort(this.listBase);
            this.filtroDocOriginal.setDataInicio(Util.getDataMesPassado());
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
                UnidadeVO unidade = this.unidadeService.findUnidadeAutorizada(this.filtroDocOriginal.getUnidadeSolicitante(), usuario);
                if (ObjectUtils.isNullOrEmpty(unidade)) {
                    unidade = new UnidadeVO();
                }
                this.filtroDocOriginal.setUnidadeSolicitante(unidade);
                JavaScriptUtils.update("formConsulta:pnlBotoes");
            }
        } catch (BusinessException e) {
            UnidadeVO unidade = this.unidadeService.findById((Long) this.filtroDocOriginal.getUnidadeSolicitante().getId());
            if (!ObjectUtils.isNullOrEmpty(unidade)) {
                this.filtroDocOriginal.setUnidadeSolicitante(unidade);
            }
            this.facesMessager.addMessageError(e.getMessage());
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
     * @throws DataBaseException 
     */
    public void limparCamposFiltros() throws DataBaseException {

        this.filtroDocOriginal = new FiltroDocumentoOriginalDTO();
        UsuarioLdap usuario = getUsuarioAutenticado();
        if (!ObjectUtils.isNullOrEmpty(usuario)) {
            this.filtroDocOriginal.setMatriculaUsuario(usuario.getNuMatricula());
            this.filtroDocOriginal.setUnidadeSolicitante(unidadeService.findUnidadeLotacaoUsuarioLogado());
        }

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
	 * Verifica se deve exibir o botão para informar o extravio do documento
	 * original selecionado
	 * 
	 * @param docOriginal
	 *            Documento original selecionado
	 * @return <b>True</b> caso o DocumentoOriginalVO possa ser extraviado,
	 *         <b>false</b> caso contrário
	 */
	public Boolean hasBotaoExtraviado(DocumentoOriginalVO docOriginal) {
		if (JSFUtil.isPerfil(Constantes.PERFIL_GESTOR)
				&& !docOriginal.getTramiteDocOriginalAtual().getSituacaoDocOriginal()
						.equals(SituacaoDocumentoOriginalEnum.EXTRAVIADO)
				&& !docOriginal.getTramiteDocOriginalAtual().getSituacaoDocOriginal()
						.equals(SituacaoDocumentoOriginalEnum.RECEPCIONADO_TERCEIRIZADA)
				&& !docOriginal.getTramiteDocOriginalAtual().getSituacaoDocOriginal()
						.equals(SituacaoDocumentoOriginalEnum.RECEPCIONADO_UNIDADE)) {
			return true;
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
	 * Verifica se o estado do DocumentoOriginalVO é passível de devolução por
	 * parte da Unidade.
	 * 
	 * @return <b>True</b> caso o DocumentoOriginalVO possa ser devolvido pela
	 *         Unidade, <b>false</b> caso contrário
	 */
	public Boolean hasBotaoDevolver(DocumentoOriginalVO docOriginal) {
		return docOriginal.getTramiteDocOriginalAtual().getSituacaoDocOriginal()
				.equals(SituacaoDocumentoOriginalEnum.RECEPCIONADO_UNIDADE);
	}
	
    public void confirmarDevolucao(DocumentoOriginalVO docOriginal) {
        this.docOriginalSelecionado = docOriginal;
        JavaScriptUtils.showModal(ID_MODAL_DEVOLVER);
    }
    
	/**
	 * Verifica se o estado do DocumentoOriginalVO é passível de recebimento por parte da Unidade.
	 * 
	 * @return <b>True</b> caso o DocumentoOriginalVO possa ser recebido pela Unidade, <b>false</b> caso contrário
	 */
	public Boolean hasBotaoReceber(DocumentoOriginalVO docOriginal) {
		return docOriginal.getTramiteDocOriginalAtual().getSituacaoDocOriginal().equals(SituacaoDocumentoOriginalEnum.ENVIADO);
	}
	
    public void confirmarRecebimento(DocumentoOriginalVO docOriginal) {
        this.docOriginalSelecionado = docOriginal;
        JavaScriptUtils.showModal(ID_MODAL_RECEPCIONAR);
    }
    
    public void confirmarExtravio(DocumentoOriginalVO docOriginal) {
        this.docOriginalSelecionado = docOriginal;
        JavaScriptUtils.showModal(ID_MODAL_EXTRAVIO);
    }
    
    public void registrarTramite(SituacaoDocumentoOriginalEnum situacao) {
        try {
            
            if (!ObjectUtils.isNullOrEmpty(this.observacaoTramite) && this.observacaoTramite.length() > 100) {
                throw new BusinessException(MensagemUtils.obterMensagem("MA055", "observação", "100"));
            }
            
            String message = StringUtils.EMPTY;
            String idModal = StringUtils.EMPTY;
            
            TramiteDocumentoOriginalVO tramite = this.tramiteDocOriginalService.getBySituacao(situacao);
            tramite.setDocumentoOriginal(this.docOriginalSelecionado);
            tramite.setObservacao(this.observacaoTramite);
            
            this.tramiteDocOriginalService.atualizarTramite(tramite);
            
            Long codigo = this.docOriginalSelecionado.getRequisicao().getCodigoRequisicao();
            
            this.docOriginalSelecionado = null;
            this.observacaoTramite = null;
            
            switch (situacao) {
                case RECEPCIONADO_UNIDADE:
                    idModal = ID_MODAL_RECEPCIONAR;
                    message = "Requisição Código %s recebida com sucesso.";
                    break;
                case EXTRAVIADO:
                    idModal = ID_MODAL_EXTRAVIO;
                    message = "Requisição Código %s extraviada com sucesso.";
                    break;
                case DEVOLVIDO:
                    idModal = ID_MODAL_DEVOLVER;
                    message = "Requisição Código %s devolvida com sucesso.";
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
