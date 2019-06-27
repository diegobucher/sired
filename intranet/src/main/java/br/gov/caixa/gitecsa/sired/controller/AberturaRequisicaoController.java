package br.gov.caixa.gitecsa.sired.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import org.primefaces.component.datalist.DataList;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.web.PaginatorModel;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.FormatoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDemandaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoUnidadeEnum;
import br.gov.caixa.gitecsa.sired.exporter.ExportarModeloGrupoCampo;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.DocumentoService;
import br.gov.caixa.gitecsa.sired.service.OperacaoService;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.service.TipoDemandaService;
import br.gov.caixa.gitecsa.sired.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.JavaScriptUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.OperacaoVO;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Named("aberturaRequisicaoController")
@ViewScoped
public class AberturaRequisicaoController implements Serializable {
    private static final long serialVersionUID = 6278630082576412180L;

    public static final String VIEW_ID_CONSULTA_DOCUMENTO = "abertura.xhtml";
    public static final String VIEW_ID_RASCUNHO = "editaRequisicao.xhtml";
    public static final String VIEW_ID_CONSULTA_REQUISICAO = "consulta.xhtml";
    public static final String VIEW_ID_REQUISICAO_LOTE = "aberturaLote.xhtml";

    private static final String ID_MODAL_DOCUMENTO_DESABILITADO = "mdlDocNaoHabilitado";
    private static final String ID_MODAL_PERMISSAO_NEGADA_UNIDADE = "mdlPermissaoNegadaUnidade";
    private static final String ID_DATALIST_RESULTADOS = "formConsulta:tabela";

    private static final int REGISTROS_POR_PAGINA = 10;

    @Inject
    protected FacesMensager facesMessager;

    @Inject
    private transient Logger logger;

    @Inject
    private DocumentoService documentoService;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private OperacaoService operacaoService;

    @Inject
    private TipoDemandaService demandaService;

    @Inject
    private RequisicaoService requisicaoService;
    
    @Inject
    private ParametroSistemaService parametroSistemaService;

    private DocumentoVO documento;

    private LazyDataModel<DocumentoVO> listDocumentoModel;

    private RequisicaoVO requisicao;

    private List<OperacaoVO> listOperacao;

    private List<TipoDemandaVO> listTipoDemanda;

    private Set<GrupoCampoVO> listGrupoCampos;
    
    private UploadedFile file;

    @PostConstruct
    public void init() {

        try {

            if (RequestUtils.getViewId().contains(VIEW_ID_CONSULTA_DOCUMENTO)) {
                GrupoVO grupo = new GrupoVO();
                grupo.setTipoSolicitacao(TipoSolicitacaoEnum.REQUISICAO);

                this.documento = new DocumentoVO();
                this.documento.setGrupo(grupo);

            } else if (RequestUtils.getViewId().contains(VIEW_ID_RASCUNHO)) {
                if (!ObjectUtils.isNullOrEmpty(RequestUtils.getParameter("id"))) {
                    this.requisicao = this.requisicaoService.findByIdEager(Long.valueOf(RequestUtils.getParameter("id")));
                    this.documento = this.requisicao.getDocumento();
                } else {
                    this.inicializarRequisicao();
                }

                // Inicializa as listas
                this.listTipoDemanda = this.demandaService.findByDocumento(this.documento);
                this.listGrupoCampos = this.requisicao.getDocumento().getGrupo().getGrupoCampos();

                this.getValorCamposDinamicos();

                // RN014 - Área-Meio
                // O sistema deve possibilitar que as Áreas Meio (todas as Unidades cujo tipo seja diferente de PV) sejam relacionadas com
                // as UFs de abrangência e com grupos de documentos. Deve possibilitar também que para determinada área meio e grupo
                // de documentos uma determinada operação seja selecionada, garantindo que usuários desta área meio só poderão
                // solicitar documentos deste grupo para a operação restringida.
                UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();
                if (unidadeLotacao.getTipoUnidade().getIndicadorUnidade().equals(TipoUnidadeEnum.PV)) {
                    this.listOperacao = operacaoService.findAll();
                } else {
                    this.listOperacao = this.operacaoService.findAllByAreaMeioEDocumento(unidadeLotacao, this.documento);
                    if (ObjectUtils.isNullOrEmpty(this.listOperacao)) {
                        // caso não exista relação entre Unidade x Grupo x Operação assume que todas as operações podem ser consultadas.
                        this.listOperacao = operacaoService.findAll();
                    }
                }

                // Ordena as listas
                Collections.sort(this.listTipoDemanda);
                Collections.sort(this.listOperacao);
            }
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }
    }

    /**
     * Consulta as unidades solicitantes autorizadas através do código da unidade tags: #requisicao #form
     */
    public void pesquisarUnidadeGeradora() {

        try {

            UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
            if (usuario != null) {

                UnidadeVO unidade = null;
                UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();

                // RN016 - Subordinação ... Dessa forma, as Unidades Geradoras
                // disponíveis para o usuário de PV, nos Formulários de
                // Requisição,
                // são a própria Unidade de lotação e as respectivas Unidades
                // subordinadas (se houver).
                if (unidadeLotacao.getTipoUnidade().getIndicadorUnidade().equals(TipoUnidadeEnum.PV)) {
                    unidade = this.unidadeService.findUnidadePV(unidadeLotacao, usuario);
                } else {
                    // RN016 - Subordinação ... Para os Usuários de Áreas-Meio
                    // cadastradas na base do sistema (RN014), as Unidades
                    // Geradoras disponíveis são a
                    // própria Unidade de lotação e Unidades subordinadas (se
                    // houver), além de todas as Unidades do tipo PV, cujas
                    // respectivas UF estejam contidas na abrangência da
                    // Área-Meio cadastrada no sistema.
                    unidade = this.unidadeService.findUnidadeAutorizada(this.requisicao.getRequisicaoDocumento().getUnidadeGeradora(), usuario);
                }

                if (ObjectUtils.isNullOrEmpty(unidade)) {
                    unidade = new UnidadeVO();
                }

                this.requisicao.getRequisicaoDocumento().setUnidadeGeradora(unidade);
                JavaScriptUtils.update("pnlBotoes");
            }
        } catch (BusinessException e) {
            UnidadeVO unidade = this.unidadeService.findById((Long) this.requisicao.getRequisicaoDocumento().getUnidadeGeradora().getId());
            if (!ObjectUtils.isNullOrEmpty(unidade)) {
                this.requisicao.getRequisicaoDocumento().setUnidadeGeradora(unidade);
            }
            this.facesMessager.addMessageError(e.getMessage());
        } catch (DataBaseException e) {
        	this.logger.error(e.getMessage(), e);
			this.facesMessager.addMessageError("MA048");
		}
    }

    /**
     * Redefine os campos do formulário
     */
    public void limparCampos() {
        try {
			this.inicializarRequisicao();
			this.getValorCamposDinamicos();
		} catch (DataBaseException e) {
        	this.logger.error(e.getMessage(), e);
			this.facesMessager.addMessageError("MA048");
		}
    }

    /**
     * Realiza a consulta de documentos tags: #abertura #consulta #documento
     */
    public void localizar() {

        Map<String, Object> filtro = new HashMap<String, Object>();

        if (this.validarFiltros()) {

            filtro.put("filtro", this.documento);

            this.resetarDataListResultados();

            PaginatorModel<DocumentoVO> paginator = new PaginatorModel<DocumentoVO>(this.documentoService, filtro);
            this.listDocumentoModel = paginator.getListModel();

            try {
                this.listDocumentoModel.load(0, 1, null, null, null);
                if (ObjectUtils.isNullOrEmpty(paginator.getList())) {
                    facesMessager.addMessageError("MA008");
                }
            } catch (UnsupportedOperationException e) {
                facesMessager.addMessageError(e.getMessage());
            }
        }
    }

    /**
     * Redireciona o usuário para o formulário de abertura de requisição para o documento selecionado tags: #abertura #consulta #documento
     * 
     * @param documentoSelecionado
     * @return Url da página de abertura de requisição ou exibe mensagem de validação
     */
    public String navegarParaAberturaRequisicao(DocumentoVO documentoSelecionado) {
        try {
	    	if (this.documentoIsValid(documentoSelecionado)) {
	            return VIEW_ID_RASCUNHO + "?faces-redirect=true&id-doc=" + documentoSelecionado.getId();
	        }
        } catch (DataBaseException e) {
        	this.logger.error(e.getMessage(), e);
			this.facesMessager.addMessageError("MA048");
		}
        
        return StringUtils.EMPTY;
    }

    /**
     * Verifica se o campo é do tipo InputText tags: #requisicao #form #campos #dinamicos
     * 
     * @param campo
     * @return <b>True</b> se o campo é do tipo InputText e <b>False</b> caso contrário
     */
    public Boolean isCampoInputText(CampoVO campo) {
        if ((campo.getTipo().equals(TipoCampoEnum.NUMERICO) || campo.getTipo().equals(TipoCampoEnum.TEXTO)) && StringUtils.isBlank(campo.getMascara())) {
            return true;
        }

        return false;
    }

    /**
     * Verifica se o campo é do tipo InputText (Money) tags: #requisicao #form #campos #dinamicos
     * 
     * @param campo
     * @return <b>True</b> se o campo é do tipo InputText usando máscara para moedas e <b>False</b> caso contrário
     */
    public Boolean isCampoInputMoney(CampoVO campo) {
        if ((campo.getTipo().equals(TipoCampoEnum.NUMERICO) || campo.getTipo().equals(TipoCampoEnum.TEXTO)) && StringUtils.isNotBlank(campo.getMascara())
                && campo.getMascara().contains("0,00")) {
            return true;
        }

        return false;
    }

    /**
     * Verifica se o campo é do tipo Calendar tags: #requisicao #form #campos #dinamicos
     * 
     * @param campo
     * @return <b>True</b> se o campo é do tipo Calendar e <b>False</b> caso contrário
     */
    public Boolean isCampoData(CampoVO campo) {
        return ObjectUtils.isCampoData(campo);
    }

    /**
     * Persist a requisição tags: #requisicao #form
     */
    public String gravar() {
        if (this.validarCampos()) {
            try {
                this.setValorCamposDinamicos();
                this.requisicaoService.salvar(this.requisicao, this.file);
                
                MensagemUtils.setKeepMessages(true);
                facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS023"));
            } catch (BusinessException e) {
            	e.printStackTrace();
            	facesMessager.addMessageError(e.getMessage());
            } catch (Exception e) {
            	e.printStackTrace();
                facesMessager.addMessageError(e.getMessage());
            }
        }
        
        return null;
    }

	/**
     * Concluir rascunho de requisições tags: #requisição #modal #confirmar #concluir
     */
    public String concluir() {
        try {
            if (this.validarCampos()) {
                this.setValorCamposDinamicos();
                this.requisicaoService.concluir(this.requisicao, this.file);

                MensagemUtils.setKeepMessages(true);
                facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS020", this.requisicao.getCodigoRequisicao().toString()));
                return VIEW_ID_CONSULTA_REQUISICAO + "?faces-redirect=true&situacao=rascunho";
            }
        } catch (BusinessException e) {
        	e.printStackTrace();
            facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
        	e.printStackTrace();
            facesMessager.addMessageError(e.getMessage());
        }

        return null;
    }

    /**
     * Seta o valor dos campos dinâmicos do formulário tags: #requisicao #form #campos #dinamicos
     * 
     * @throws AppException
     */
    public void setValorCamposDinamicos() throws AppException {

        try {
            this.requisicao = GrupoCamposHelper.setValorCamposDinamicos(this.requisicao, this.listGrupoCampos);
        } catch (AppException e) {
            throw e;
        }
    }

    /**
     * Obtém os valores dos campos dinâmicos do formulário tags: #requisicao #form #campos #dinamicos
     */
    public void getValorCamposDinamicos() {

        try {
            this.listGrupoCampos = GrupoCamposHelper.getValorCamposDinamicos(this.requisicao, this.listGrupoCampos);
        } catch (AppException e) {
            facesMessager.addMessageError(e.getMessage());
        }
    }
    
    /**
     * Obtém o modelo de planilha do excel para abertura em lote tags: #modelo #campos #excel 
     */
    public StreamedContent downloadModeloExcel(final DocumentoVO documento) {
        
        try {
            
            Set<GrupoCampoVO> grupoCampos = this.documentoService.getUltimaVersaoGrupoCampoDocumento(documento);
            
            ExportarModeloGrupoCampo modelo = new ExportarModeloGrupoCampo();
            modelo.setDocumento(documento);
            modelo.setData(new ArrayList<GrupoCampoVO>(grupoCampos));
            
            int qtdLinhas = Constantes.PADRAO_QDT_LINHAS_MODELO_DOCUMENTO;
            
            try {
                ParametroSistemaVO parametro = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_QTD_LINHAS_MODELO_DOCUMENTO);
                qtdLinhas = Integer.valueOf(parametro.getVlParametroSistema());
            } catch (Exception e) {
				e.printStackTrace();
                this.logger.error(e.getMessage(), e);
            }
            
            modelo.setQtdLinhas(qtdLinhas);
            
            String caminho = System.getProperty(Constantes.CAMINHO_UPLOAD);
            String filename = "ModeloDocumento_" + documento.getNome().toUpperCase().replaceAll("[\\/\\.]", StringUtils.EMPTY);
            
            File relatorio = modelo.export(caminho + filename + ".xls");
    
            return RequestUtils.download(relatorio, relatorio.getName());
        } catch (BusinessException e) {
			e.printStackTrace();
            facesMessager.addMessageError(e.getMessage());
        } catch (FileNotFoundException e) {
			e.printStackTrace();
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI028"));
        } catch (IOException e) {
			e.printStackTrace();
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI028"));
        } catch (AppException e) {
			e.printStackTrace();
            facesMessager.addMessageError(e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Redireciona o usuário para o formulário de abertura de requisição em lote para o documento selecionado tags: #abertura #lote #consulta #documento
     * 
     * @return Url da página de abertura de requisição em lote ou exibe mensagem de validação
     */
    public String navegarParaAberturaRequisicaoEmLote() {
        return VIEW_ID_REQUISICAO_LOTE + "?faces-redirect=true";
    }

    private Boolean documentoIsValid(DocumentoVO documentoSelecionado) throws DataBaseException {
        // FE3 - O documento e/ou o grupo não está habilitado
        if (!documentoSelecionado.getIcAtivo().equals(AtivoInativoEnum.ATIVO) || !documentoSelecionado.getGrupo().getSituacao().equals(AtivoInativoEnum.ATIVO)) {
            JavaScriptUtils.showModal(ID_MODAL_DOCUMENTO_DESABILITADO);
            return false;
        }

        // FE4 - A Área-Meio não possui permissão para solicitar documento
        UnidadeVO unidadeUsuario = this.unidadeService.findUnidadeLotacaoUsuarioLogado();
        if (!this.unidadeService.hasPermissaoDocumentoUnidade(documentoSelecionado, unidadeUsuario)) {
            JavaScriptUtils.showModal(ID_MODAL_PERMISSAO_NEGADA_UNIDADE);
            return false;
        }
        
        return true;
    }

    /**
     * Inicializa todos os elementos tags: #requisicao #form
     * @throws DataBaseException 
     */
    private void inicializarRequisicao() throws DataBaseException {
        // Inicializa a requisição
        this.requisicao = new RequisicaoVO();

        // Inicializa o usuário e a unidade
        UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();
        this.requisicao.setCodigoUsuarioAbertura(usuario.getNuMatricula());
        this.requisicao.setUnidadeSolicitante(unidadeLotacao);
        this.requisicao.setDataHoraAbertura(new Date());

        // Inicializa o documento
        if (!ObjectUtils.isNullOrEmpty(RequestUtils.getParameter("id-doc"))) {
            this.documento = this.documentoService.findByIdEager(Long.valueOf(RequestUtils.getParameter("id-doc")));
        }
        this.requisicao.setDocumento(this.documento);

        // Inicializa a requisição documento
        RequisicaoDocumentoVO requisicaoDocumento = new RequisicaoDocumentoVO();
        requisicaoDocumento.setUnidadeGeradora((UnidadeVO) ObjectUtils.clone(unidadeLotacao));
        this.requisicao.setRequisicaoDocumento(requisicaoDocumento);
    }

    /**
     * Reseta o datalist que contém os resultados da consulta tags: #abertura #consulta #documento
     */
    private void resetarDataListResultados() {
        DataList dataList = (DataList) JavaScriptUtils.findComponentById(ID_DATALIST_RESULTADOS);
        if (!ObjectUtils.isNullOrEmpty(dataList)) {
            dataList.setFirst(0);
            dataList.setRows(REGISTROS_POR_PAGINA);
        }
    }

    /**
     * Realiza a validação dos filtros da consulta tags: #abertura #consulta #documento
     * 
     * @return <b>True</b> se todos os campos estão preenchidos corretamente e <b>false</b> caso contrário
     */
    private Boolean validarFiltros() {
        if (StringUtils.isBlank(this.documento.getNome())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "MA059"));
            return false;
        }

        return true;
    }

    /**
     * Realiza a validação dos campos do formulário de abertura de requisição tags: #requisicao #form #campos
     * 
     * @return <b>True</b> se todos os campos estão preenchidos corretamente e <b>false</b> caso contrário
     */
    private Boolean validarCampos() {
        Boolean valido = true;

        if (ObjectUtils.isNullOrEmpty(this.requisicao.getRequisicaoDocumento().getUnidadeGeradora())
                || ObjectUtils.isNullOrEmpty(this.requisicao.getRequisicaoDocumento().getUnidadeGeradora().getId())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.unidadeGeradora"));
            valido = false;
        }
        
        if (FormatoDocumentoEnum.ORIGINAL.equals(this.requisicao.getFormato())) {
			if (ObjectUtils.isNullOrEmpty(this.file) && ObjectUtils.isNullOrEmpty(this.requisicao.getArquivoJustificativa())) {
	        	valido = false;
	            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.arquivo"));
	        } else if (!ObjectUtils.isNullOrEmpty(this.file) && !this.file.getFileName().toLowerCase().endsWith(".zip") 
	        		&& !this.file.getFileName().toLowerCase().endsWith(".pdf")) {
	        	valido = false;
	            this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA044"));
	        }
        }

        // Campos dinâmicos
        for (GrupoCampoVO grupoCampo : this.requisicao.getDocumento().getGrupo().getGrupoCampos()) {
            if (grupoCampo.getCampoObrigatorio().equals(SimNaoEnum.SIM)) {
                if (grupoCampo.getCampo().getNome().equals("NU_OPERACAO_A11")
                        && ObjectUtils.isNullOrEmpty(this.requisicao.getRequisicaoDocumento().getOperacao())) {
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "requisicao.label.operacao"));
                    valido = false;
                } else if (!grupoCampo.getCampo().getNome().equals("NU_OPERACAO_A11") && ObjectUtils.isNullOrEmpty(grupoCampo.getValor())
                        && ObjectUtils.isNullOrEmpty(grupoCampo.getValorData())) {
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001",
                            StringUtils.defaultIfEmpty(grupoCampo.getLegenda(), grupoCampo.getCampo().getDescricao())));
                    valido = false;
                }
            }
        }

        if (ObjectUtils.isNullOrEmpty(this.requisicao.getFormato())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "requisicao.label.formato"));
            valido = false;
        }

        if (ObjectUtils.isNullOrEmpty(this.requisicao.getRequisicaoDocumento().getTipoDemanda())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "requisicao.label.demanda"));
            valido = false;
        } else {
            if (!this.requisicao.getRequisicaoDocumento().getTipoDemanda().getNome().equalsIgnoreCase(TipoDemandaEnum.NORMAL.toString())
                    && ObjectUtils.isNullOrEmpty(this.requisicao.getRequisicaoDocumento().getNuDocumentoExigido())) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "requisicao.label.numeroProcesso"));
                valido = false;
            }
        }

        return valido;
    }

    public DocumentoVO getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoVO documentoFiltro) {
        this.documento = documentoFiltro;
    }

    public LazyDataModel<DocumentoVO> getListDocumentoModel() {
        return listDocumentoModel;
    }

    public void setListDocumentoModel(LazyDataModel<DocumentoVO> listDocumentoModel) {
        this.listDocumentoModel = listDocumentoModel;
    }

    public RequisicaoVO getRequisicao() {
        return requisicao;
    }

    public void setRequisicao(RequisicaoVO requisicao) {
        this.requisicao = requisicao;
    }

    public List<OperacaoVO> getListOperacao() {
        return listOperacao;
    }

    public FormatoDocumentoEnum[] getListFormato() {
        return FormatoDocumentoEnum.values();
    }

    public List<TipoDemandaVO> getListTipoDemanda() {
        return this.listTipoDemanda;
    }

    public List<GrupoCampoVO> getListGrupoCampos() {
        if (!ObjectUtils.isNullOrEmpty(listGrupoCampos)) {
            return CollectionUtils.asSortedList(listGrupoCampos);
        }

        return null;
    }

    public void setListGrupoCampos(Set<GrupoCampoVO> listGrupoCampos) {
        this.listGrupoCampos = listGrupoCampos;
    }

    public boolean exibirNumeroProcesso() {
        TipoDemandaVO tipoDemandaVO = this.requisicao.getRequisicaoDocumento().getTipoDemanda();
        if ((!ObjectUtils.isNullOrEmpty(tipoDemandaVO)) && !tipoDemandaVO.getNome().equalsIgnoreCase(TipoDemandaEnum.NORMAL.toString())) {
            return true;
        }
        // limpa o numero do processo.
        this.requisicao.getRequisicaoDocumento().setNuDocumentoExigido(StringUtils.EMPTY);
        return false;
    }
    
    public Boolean exibirArquivoJustificativa() {
    	if (FormatoDocumentoEnum.ORIGINAL.equals(this.requisicao.getFormato())) {
    		return true;
    	}
    	
    	this.file = null;
    	return false;
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

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}
}
