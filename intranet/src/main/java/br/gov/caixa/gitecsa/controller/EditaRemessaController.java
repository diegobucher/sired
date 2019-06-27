package br.gov.caixa.gitecsa.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.AutenticadorLdap;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.DocumentoService;
import br.gov.caixa.gitecsa.service.EmpresaContratoService;
import br.gov.caixa.gitecsa.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.service.RemessaService;
import br.gov.caixa.gitecsa.service.RequisicaoService;
import br.gov.caixa.gitecsa.service.TramiteRemessaService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.service.FeriadoService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.util.JSFUtil;
import br.gov.caixa.gitecsa.util.ReportUtils;

@Named
@ViewScoped
public class EditaRemessaController extends BaseConsultaCRUD<DocumentoVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private DocumentoService documentoService;

    @Inject
    private RemessaService remessaService;

    @Inject
    private RemessaDocumentoService remessaDocumentoService;

    @Inject
    private TramiteRemessaService tramiteRemessaService;

    @Inject
    private EmpresaContratoService empresaContratoService;

    @Inject
    private RequisicaoService requisicaoService;

    private DocumentoVO documento;
    private RemessaVO remessa;
    private RemessaDocumentoVO remessaDocumento;

    private String nomeFiltro;
    private List<DocumentoVO> lista;
    private List<DocumentoVO> listaFiltro;
    private Boolean pesquisaRealizada;
    private Boolean abrirModal = false;
    private String tituloPagina;
    private String breadcrumbPagina;

    private Long anoFragmentacao;

    private StreamedContent termoResponsabilidade;

    private StreamedContent capaLote;
    
    @Inject
    private FeriadoService feriadoService;

    @Override
    protected DocumentoVO newInstance() {
        return new DocumentoVO();
    }

    @Override
    protected AbstractService<DocumentoVO> getService() {
        return documentoService;
    }

    @PostConstruct
    protected void init() throws AppException {
        
        try {
            
            if (JSFUtil.getSessionMapValue("remessa") != null) {
                remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
                documento = remessa.getDocumento();
                remessa.setTramiteRemessas(tramiteRemessaService.findByRemessa(remessa));
                listarRemessaDocumento();
            } else {
                this.remessa = new RemessaVO();
                this.documento = (DocumentoVO) JSFUtil.getSessionMapValue("documentoRemessa");
                this.abrirModal = true;
            }
    
            if (JSFUtil.getSessionMapValue("operacao") != null && JSFUtil.getSessionMapValue("operacao").equals("correcao")) {
                tituloPagina = MensagemUtils.obterMensagem("remessa.correcao.tituloPagina.descricao");
                breadcrumbPagina = MensagemUtils.obterMensagem("remessa.manutencao.breadcrumb");
            } else {
                tituloPagina = MensagemUtils.obterMensagem("remessa.rascunho.tituloPagina.descricao");
                breadcrumbPagina = MensagemUtils.obterMensagem("remessa.rascunho.breadcrumb");
            }
    
            Date date = new Date();
            Calendar cal = new GregorianCalendar();
            cal.setTime(date);
            if (!Util.isNullOuVazio(this.documento.getPrazoFragmentacao())) {
                this.anoFragmentacao = cal.get(Calendar.YEAR) + this.documento.getPrazoFragmentacao();
                JSFUtil.setSessionMapValue("anoFragmentacao", this.anoFragmentacao);
    
            }
            
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }

    }

    public void editarRemessaDocumento(RemessaDocumentoVO remessaDocumento) {
        try {
            JSFUtil.setSessionMapValue("remessaDocumento", remessaDocumento);
            FacesContext.getCurrentInstance().getExternalContext().redirect("abertura2");
        } catch (Exception e) {
            /**
             * Não é necessário escrever exceção no log pois o interceptor já interceptou a exceção e escreveu no log.
             */
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
        }
    }

    public void incluirRemessaDocumento(RemessaVO remessaVO) {
        try {
            JSFUtil.setSessionMapValue("remessa", remessaVO);
            FacesContext.getCurrentInstance().getExternalContext().redirect("abertura2");
        } catch (Exception e) {
            /**
             * Não é necessário escrever exceção no log pois o interceptor já interceptou a exceção e escreveu no log.
             */
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
        }
    }

    public boolean exibirBotaoConcluirRemessa() {

        SituacaoRemessaVO situacao = this.remessa.getTramiteRemessaAtual().getSituacao();
        if (situacao.getId().equals(SituacaoRemessaEnum.RASCUNHO.getId())) {
            return true;
        }
        return false;
    }

    /**
     * Metodo para conclusão da remessa. Salva o tramite da remessa com situação ABERTA
     */
    public void concluirRemessa() {
        try {
            remessa.setTramiteRemessaAtual(tramiteRemessaService.salvarTramiteRemessaAberta(this.remessa));
            remessa.setCodigoUsuarioAbertura(JSFUtil.getUsuario().getNuMatricula());
            remessa.setDataHoraAbertura(new Date());
            this.remessaService.saveOrUpdate(this.remessa);
            JSFUtil.addSuccessMessage(MensagemUtils.obterMensagem("MS028", remessa.getId().toString()));
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            if (remessa.getDocumento().getNome().trim().equalsIgnoreCase(Constantes.DOCUMENTO_REMESSA_MOVIMENTO_DIARIO)) {
                remessa.setTramiteRemessaAtual(tramiteRemessaService.salvarTramiteRemessaAgendada(this.remessa));
                
                if (!ObjectUtils.isNullOrEmpty(remessa.getTramiteRemessaAtual()) 
                		&& !ObjectUtils.isNullOrEmpty(remessa.getTramiteRemessaAtual().getDataAgendamento()) 
                		&& !this.feriadoService.isDataUtil(remessa.getTramiteRemessaAtual().getDataAgendamento(), remessa.getUnidadeSolicitante())) {
                    throw new BusinessException("Data de Agendamento inválida. A data deve ser um dia útil.");
                }
                
                this.remessa.setDataAgendamento(remessa.getTramiteRemessaAtual().getDataAgendamento());
                this.remessaService.saveOrUpdate(this.remessa);
            }
            this.voltar();
            if (JSFUtil.getSessionMapValue("origem").toString().equalsIgnoreCase("abertura")) {
                updateComponentes("formAbertura:messagesAbertura");
            } else {
                updateComponentes("formConsulta:messages");
            }
        } catch (BusinessException e) {
            if (!Util.isNull(e.getErroList())) {
                for (String message : e.getErroList()) {
                    facesMessager.addMessageError(message);
                }
            } else {
                facesMessager.addMessageError(e.getMessage());
            }
        } catch (AppException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "EditaRemessa", "concluirRemessa"));
        }
    }


    public void listarRemessaDocumento() throws AppException {
        remessa.setRemessaDocumentos(remessaDocumentoService.findByRemessa(getRemessa()));
    }

    /**
     * Metodo para exclusão de remessa documento da remessa. Se houver apenas um item na remessa, exclui também a remessa.
     */
    public void excluir() {
        try {
            if (this.remessa.getRemessaDocumentos().size() > 1) {
                remessaDocumentoService.delete(this.remessaDocumento);
                listarRemessaDocumento();
                JSFUtil.addSuccessMessage(MensagemUtils.obterMensagem("MS016"));
                updateComponentes("formRemessa:dataTableCadastro");
            } else {
                remessaDocumentoService.delete(this.remessaDocumento);
                getRemessa().setRemessaDocumentos(new ArrayList<RemessaDocumentoVO>());
                remessaService.delete(getRemessa());
                JSFUtil.addSuccessMessage(MensagemUtils.obterMensagem("MS029"));
                this.voltar();
                // Preciso ver para onde irei voltar para atualizar o form
                // correto.
                if (JSFUtil.getSessionMapValue("origem").toString().equalsIgnoreCase("abertura")) {
                    updateComponentes("formAbertura:messagesAbertura");
                } else {
                    updateComponentes("formConsulta:messages");
                }
            }

        } catch (AppException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "EditaRemessa", "excluir"));
        }
    }

    /**
     * Metodo chamado pelo botão Voltar
     */
    public void voltar() throws AppException {
        try {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

            if (JSFUtil.getSessionMapValue("origem").toString().equalsIgnoreCase("abertura")) {
                FacesContext.getCurrentInstance().getExternalContext().redirect("abertura");
            } else {
                FacesContext.getCurrentInstance().getExternalContext().redirect("mantem-remessa");
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void limparFiltro() {
        super.limparFiltro();
        contextoController.limpar();
        contextoController.setObjectFilter(null);
        nomeFiltro = null;
    }

    /**
     * Localiza o documento pesquisado
     */
    public void localizar() {
        pesquisaRealizada = true;
        DocumentoVO filtro = new DocumentoVO();
        filtro.setNome(nomeFiltro.toUpperCase());
        try {
            lista = documentoService.findByParameters(filtro);
            listaFiltro = lista;
            limparFiltro();
        } catch (AppException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "EditaRemessa", "localizar"));
        }
    }

    public void gerarTermoResponsabilidade() {

        byte[] bytesArray = imprimirTermo();
        InputStream stream = new ByteArrayInputStream(bytesArray);
        setTermoResponsabilidade(new DefaultStreamedContent(stream, "application/pdf", "Remessa" + this.remessa.getId().toString() + ".pdf"));
    }

    public byte[] imprimirTermo() {

        try {

            String matricula = this.remessa.getCodigoUsuarioAbertura().trim();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            UsuarioLdap usuario = consultaUsuarioLDAP(matricula);

            HashMap<String, Object> parametros = new HashMap<String, Object>();
            parametros.put("IMAGE_DIR", Constantes.CAMINHO_IMG + "logocaixa2.png");
            parametros.put("CIDADE_LOTACAO", usuario.getCidade().trim());
            parametros.put("DATA_ATUAL_EXTENSO", Util.dataPorExtenso(calendar).trim());
            parametros.put("NOME_EMPREGADO_LOGADO", usuario.getNomeUsuario().trim());
            parametros.put("FUNCAO_LOGADO", usuario.getNoFuncao().trim());
            parametros.put("MATRICULA_LOGADO", usuario.getNuMatricula().toUpperCase().trim());
            parametros.put("UNIDADE_LOGADO", usuario.getCoUnidade().toString().trim());
            parametros.put("NOME_UNIDADE_LOGADO", usuario.getNoUnidade().trim());

            return new ReportUtils().obterRelatorio(
                    getClass().getClassLoader().getResourceAsStream(Constantes.CAMINHO_JASPER.concat("Termo_responsabilidade.jasper")), parametros, null,
                    ReportUtils.REL_TIPO_PDF);

        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "EditaRemessa", "imprimirTermo"));
        }

        return null;
    }

    /**
     * Metodo chamado pela página para gerar a capa do lote da remessa documento.
     * 
     * @param remessaDocumento
     */
    public void gerarCapaDeLote(RemessaDocumentoVO remessaDocumento) {
        byte[] bytesArray = imprimirCapaDeLote(remessaDocumento);
        InputStream stream = new ByteArrayInputStream(bytesArray);
//        setCapaLote(new DefaultStreamedContent(stream, "application/pdf", "CapaLoteItem" + remessaDocumento.getNuItem().toString() + ".pdf"));
    }

    public byte[] imprimirCapaDeLote(RemessaDocumentoVO remessaDocumento) {

        try {

            String campoEspecieDocumento = "";

            HashMap<String, Object> parametros = new HashMap<String, Object>();
//            parametros.put("NUMERO_ITEM", remessaDocumento.getNuItem().toString().trim());
            parametros.put("NUMERO_REMESSA", remessaDocumento.getRemessa().getId().toString().trim());
            parametros.put("NOME_UF", remessaDocumento.getUnidadeGeradora().getUf().getNome().trim());
            parametros.put("NOME_UF_UNIDADE", remessaDocumento.getUnidadeGeradora().getNome().trim());
            parametros.put("SG_UF_UNIDADE", remessaDocumento.getUnidadeGeradora().getUf().getId().toString().trim());
            parametros.put("NU_UNIDADE", remessaDocumento.getUnidadeGeradora().getId().toString());

            /*
             * if (!Util.isNullOuVazio(remessaDocumento.getNumeroContaInicio()) && !Util.isNullOuVazio(remessaDocumento.getNumeroContaFim())
             * ) { campoEspecieDocumento = "Contas: " + remessaDocumento.getNumeroContaInicio().trim() + " - " +
             * remessaDocumento.getNumeroContaFim().trim(); } else if (!Util.isNullOuVazio(remessaDocumento.getNumeroContaInicio())) {
             * campoEspecieDocumento = "Contas: " + remessaDocumento.getNumeroContaInicio().trim(); } else if
             * (!Util.isNullOuVazio(remessaDocumento.getNumeroContaFim())) { campoEspecieDocumento = "Contas: " +
             * remessaDocumento.getNumeroContaFim().trim(); }
             */

            /*
             * if (!Util.isNullOuVazio(remessaDocumento.getQuantidadeTfs())) { if (!Util.isNullOuVazio(campoEspecieDocumento)) {
             * campoEspecieDocumento += "\nQuantidade de terminais financeiros:  " + remessaDocumento.getQuantidadeTfs().toString().trim();
             * } else { campoEspecieDocumento += "Quantidade de terminais financeiros:  " +
             * remessaDocumento.getQuantidadeTfs().toString().trim(); } }
             */
            parametros.put("CAMPOS_CONTA", campoEspecieDocumento);

            if (!Util.isNullOuVazio(remessaDocumento.getDataInicio()) && !Util.isNullOuVazio(remessaDocumento.getDataFim())) {
                parametros.put(
                        "CAMPOS_DATA",
                        Util.formatData(remessaDocumento.getDataInicio(), Constantes.FORMATO_DATA).trim() + " - "
                                + Util.formatData(remessaDocumento.getDataFim(), Constantes.FORMATO_DATA).trim());
            } else if (!Util.isNullOuVazio(remessaDocumento.getDataInicio())) {
                parametros.put("CAMPOS_DATA", Util.formatData(remessaDocumento.getDataInicio(), Constantes.FORMATO_DATA).trim());
            } else if (!Util.isNullOuVazio(remessaDocumento.getDataFim())) {
                parametros.put("CAMPOS_DATA", Util.formatData(remessaDocumento.getDataFim(), Constantes.FORMATO_DATA).trim());
            } else if (!Util.isNullOuVazio(remessaDocumento.getDataGeracao())) {
                parametros.put("CAMPOS_DATA", Util.formatData(remessaDocumento.getDataGeracao(), Constantes.FORMATO_DATA).trim());
            }

            parametros.put("DT_ATUAL", Util.formatData(new Date(), Constantes.FORMATO_DATA));

            return new ReportUtils().obterRelatorio(getClass().getClassLoader().getResourceAsStream(Constantes.CAMINHO_JASPER.concat("Capa_lote.jasper")),
                    parametros, null, ReportUtils.REL_TIPO_PDF);

        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "EditaRemessa", "imprimirCapaDeLote"));
        }

        return null;
    }

    public UsuarioLdap consultaUsuarioLDAP(String matricula) {
        UsuarioLdap usuarioAvaliacao = new UsuarioLdap();
        try {
            AutenticadorLdap autenticadorLdap = new AutenticadorLdap();
            usuarioAvaliacao = autenticadorLdap.pesquisarForaDoGrupoDoLdap(matricula.trim(), System.getProperty(Constantes.INTRANET_URL_LDAP));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return usuarioAvaliacao;
    }

    /**
     * Retorna <code>true</code> caso a situação da Remessa seja passível de Geração do Termo de Responsabilidade, <code>false</code> caso contrário.
     * 
     * @param remessa
     * @return <code>true</code> caso a situação da Remessa seja passível de Geração do Termo de Responsabilidade, <code>false</code> caso contrário.
     */
    public boolean isExibirTermoResponsabilidade(RemessaVO remessa) {
        return Util.exibirTermoResponsabilidadeNaRemessa(remessa);
    }

    /**
     * Retorna <code>true</code> caso a situação da Remessa seja passível de Geração da Caoa de Lote, <code>false</code> caso contrário.
     * 
     * @param remessa
     * @return <code>true</code> caso a situação da Remessa seja passível de Geração da Caoa de Lote, <code>false</code> caso contrário.
     */
    public boolean isExibirCapaLote(RemessaVO remessa) {
        return Util.exibirCapaLoteNaRemessa(remessa);
    }

    public void setService(DocumentoService service) {
        this.documentoService = service;
    }

    public String getNomeFiltro() {
        return nomeFiltro;
    }

    public void setNomeFiltro(String nomeFiltro) {
        this.nomeFiltro = nomeFiltro;
    }

    public List<DocumentoVO> getLista() {
        return lista;
    }

    public void setLista(List<DocumentoVO> lista) {
        this.lista = lista;
    }

    public List<DocumentoVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<DocumentoVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public Boolean getPesquisaRealizada() {
        return pesquisaRealizada;
    }

    public void setPesquisaRealizada(Boolean pesquisaRealizada) {
        this.pesquisaRealizada = pesquisaRealizada;
    }

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

    public Boolean getAbrirModal() {
        return abrirModal;
    }

    public void setAbrirModal(Boolean abrirModal) {
        this.abrirModal = abrirModal;
    }

    public RemessaDocumentoVO getRemessaDocumento() {

        return remessaDocumento;
    }

    public void setRemessaDocumento(RemessaDocumentoVO remessaDocumento) {
        if (remessaDocumento == null)
            remessaDocumento = new RemessaDocumentoVO();
        this.remessaDocumento = remessaDocumento;
    }

    public Long getAnoFragmentacao() {
        return anoFragmentacao;
    }

    public void setAnoFragmentacao(Long anoFragmentacao) {
        this.anoFragmentacao = anoFragmentacao;
    }

    public String getTituloPagina() {
        return tituloPagina;
    }

    public void setTituloPagina(String tituloPagina) {
        this.tituloPagina = tituloPagina;
    }

    public String getBreadcrumbPagina() {
        return breadcrumbPagina;
    }

    public void setBreadcrumbPagina(String breadcrumbPagina) {
        this.breadcrumbPagina = breadcrumbPagina;
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

}
