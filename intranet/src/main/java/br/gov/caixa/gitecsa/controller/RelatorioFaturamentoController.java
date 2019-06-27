package br.gov.caixa.gitecsa.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.converter.DataConverterSIRED;
import br.gov.caixa.gitecsa.dto.RelatorioFaturamentoDTO;
import br.gov.caixa.gitecsa.dto.RelatorioSuporteDTO;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.TramiteRequisicaoDocumentoService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.dto.FiltroFaturamentoRemessaDTO;
import br.gov.caixa.gitecsa.sired.enumerator.TipoRelatorioFaturamentoEnum;
import br.gov.caixa.gitecsa.sired.exporter.ExportarFaturamentoXLS;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.service.RemessaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.JavaScriptUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.ViewRelatorioFaturamentoABVO;
import br.gov.caixa.gitecsa.util.JSFUtil;
import br.gov.caixa.gitecsa.visitor.RelatorioVisitor;

@Named
@ViewScoped
public class RelatorioFaturamentoController extends BaseConsultaCRUD<TramiteRequisicaoVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    private static String NOME_RELATORIO_REQUISICAO = "RELATÓRIO DE FATURAMENTO DE REQUISIÇÃO";
    private static String NOME_RELATORIO_REMESSA_TIPO_AB = "RELATÓRIO DE FATURAMENTO DE REMESSA TIPO A/B";
    private static String NOME_RELATORIO_REMESSA_TIPO_C = "RELATÓRIO DE FATURAMENTO DE REMESSA TIPO C";

    private RelatorioVisitor relatorioVisitor;

    private List<BaseVO> litaBases;

    private Boolean pesquisaRealizada;

    private TipoRelatorioFaturamentoEnum tipoRelatorio = TipoRelatorioFaturamentoEnum.REQUISICAO;

    private List<RelatorioFaturamentoDTO> listaFaturamento;
    
    private Map<Long, Map<Long, RelatorioFaturamentoDTO>> mapSubtotalFaturamento;

    private List<RelatorioFaturamentoDTO> listaFiltro;

    private List<RemessaVO> listRemessaRelFaturamentoTipoC;

    private List<ViewRelatorioFaturamentoABVO> listRemessaRelFaturamentoAB;

    private int qtdTotalSolicitada;
    private int qtdTotalDisponibilizada;
    private int qtdTotalDisponibilizadaNoPrazo;
    private int qtdTotalNaoLocalizada;
    private Double idlpTotal;
    private Double idnlTotal;
    private String filtroPesquisar;

    @Inject
    private BaseService baseService;

    @Inject
    private TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;

    @Inject
    private RemessaService remessaService;

    @Inject
    private ParametroSistemaService parametroSistemaService;
    
    private Integer totalUnidSolicitantes = 0;

    private Integer totalNovosMovimentos = 0;

    private Integer totalMovimentosUltimos90Dias = 0;

    private Integer totalMovimentosApos90Dias = 0;

    private Integer pzColeta;

    private Integer totalAgendamentos;

    private Integer totalRecebimentos;

    private Integer totalConferencias;

    private Integer totalFechamentos;

    private Long totalQtdCaixa;

    private Integer totalRemessaNoPrazo;

    private Integer totalRemessaForaPrazo;

    @Override
    protected TramiteRequisicaoVO newInstance() {
        return (new TramiteRequisicaoVO());
    }

    @Override
    protected AbstractService<TramiteRequisicaoVO> getService() {
        return null;
    }

    @PostConstruct
    protected void init() throws AppException {
        filtroPesquisar = new String();
        pesquisaRealizada = false;
        try {
            if (getPerfilAcessoBase()) {
                litaBases = baseService.findAll();
            }

            relatorioVisitor = new RelatorioVisitor();

            listaFaturamento = new ArrayList<RelatorioFaturamentoDTO>();
            listaFiltro = listaFaturamento;
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                BusinessException be = (BusinessException) e;
                for (String message : be.getErroList()) {
                    facesMessager.addMessageError(message);
                }
            } else {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            }
        }
    }
    
    /**
     * Exporta Relatorio de Requisição.
     * 
     * @param document
     */
    public void preExportarRequisicao(Object document) {
        preExportar(NOME_RELATORIO_REQUISICAO, document);
    }

    /**
     * Exporta Relatorio de Remessa Tipo A/B.
     * 
     * @param document
     */
    public void preExportarRemessaTipoAB(Object document) {
        preExportar(NOME_RELATORIO_REMESSA_TIPO_AB, document);
    }

    /**
     * Exporta Relatorio de Remessa Tipo A/B.
     * 
     * @param document
     */
    public void preExportarRemessaTipoC(Object document) {
        preExportar(NOME_RELATORIO_REMESSA_TIPO_C, document);
    }

    /**
     * Exporta o Relatorio De Acordo com o nome Informado
     * 
     * @param document
     */
    public void preExportar(String titulo, Object document) {
        RelatorioSiredUtil relatorio = new RelatorioSiredUtil(titulo, document);
        relatorio.preExportar();
    }

    /**
     * Verificar se o perfil de acesso pode ou não visualizar uma base
     * 
     * @return Boolean
     */
    public Boolean getPerfilAcessoBase() {

        if (JSFUtil.isPerfil("GEST") || JSFUtil.isPerfil("ADT")) {
            return true;
        }
        return false;
    }
    
    private RelatorioFaturamentoDTO getSubtotalRelatorioFaturamento(RelatorioFaturamentoDTO obj) {
        Map<Long, RelatorioFaturamentoDTO> mapSubtotal = mapSubtotalFaturamento.get(obj.getBase().getId());
        if (ObjectUtils.isNullOrEmpty(mapSubtotal)) {
            return null;
        }
        
        Long id = (!ObjectUtils.isNullOrEmpty(obj.getSuporte())) ? (Long)obj.getSuporte().getId() : -1;
        return mapSubtotal.get(id);
    }
    
    public Integer calcSubtotalQtdSolicitada(RelatorioFaturamentoDTO obj) {
        RelatorioFaturamentoDTO item = getSubtotalRelatorioFaturamento(obj);
        return (ObjectUtils.isNullOrEmpty(item)) ? BigDecimal.ZERO.intValue() : item.getQtdSolicitada();
    }
    
    public Integer calcSubtotalQtdDisponibilizada(RelatorioFaturamentoDTO obj) {
      RelatorioFaturamentoDTO item = getSubtotalRelatorioFaturamento(obj);
      return (ObjectUtils.isNullOrEmpty(item)) ? BigDecimal.ZERO.intValue() : item.getQtdDisponibilizada();
    }
    
    public Integer calcSubtotalQtdDisponibilizadaNoPrazo(RelatorioFaturamentoDTO obj) {
      RelatorioFaturamentoDTO item = getSubtotalRelatorioFaturamento(obj);
      return (ObjectUtils.isNullOrEmpty(item)) ? BigDecimal.ZERO.intValue() : item.getQtdDispNoPrazo();
    }
    
    public Integer calcSubtotalQtdNaoLocalizada(RelatorioFaturamentoDTO obj) {
      RelatorioFaturamentoDTO item = getSubtotalRelatorioFaturamento(obj);
      return (ObjectUtils.isNullOrEmpty(item)) ? BigDecimal.ZERO.intValue() : item.getQtdNaoLocalizada();
    }
    
    public Double calcSubtotalIDLP(RelatorioFaturamentoDTO obj) {
      RelatorioFaturamentoDTO item = getSubtotalRelatorioFaturamento(obj);
      Double idlp = (ObjectUtils.isNullOrEmpty(item)) ? BigDecimal.ZERO.doubleValue() : ((new Double(item.getQtdDispNoPrazo()) / new Double(item.getQtdDisponibilizada())) * 100); 
      return (Double.isNaN(idlp)) ? BigDecimal.ZERO.doubleValue() : idlp;
    }
    
    public Double calcSubtotalIDLN(RelatorioFaturamentoDTO obj) {
      RelatorioFaturamentoDTO item = getSubtotalRelatorioFaturamento(obj);
      Double idln = (ObjectUtils.isNullOrEmpty(item)) ? BigDecimal.ZERO.doubleValue() : ((new Double(item.getQtdNaoLocalizada()) / new Double(item.getQtdSolicitada())) * 100);
      return (Double.isNaN(idln)) ? BigDecimal.ZERO.doubleValue() : idln;
    }

    public void localizar() throws AppException {

        this.listaFaturamento = null;
        this.listRemessaRelFaturamentoTipoC = null;
        this.listRemessaRelFaturamentoAB = null;

        filtroPesquisar = new String();
        preencheDataFimDefault();
        if (validaCamposObrigatorios() && isDadosValidos() && obtemBaseUnidadeUsuarioAdministrador()) {

            if (!TipoRelatorioFaturamentoEnum.REQUISICAO.equals(this.tipoRelatorio)) {

                FiltroFaturamentoRemessaDTO filtroRelatorio = new FiltroFaturamentoRemessaDTO();
                filtroRelatorio.setDataInicio(this.relatorioVisitor.getDataInicio());
                filtroRelatorio.setDataFim(this.relatorioVisitor.getDataFim());
                filtroRelatorio.setBase(this.relatorioVisitor.getBase());

                if (TipoRelatorioFaturamentoEnum.REMESSA_AB.equals(this.tipoRelatorio)) {

                    try {
                        this.pzColeta = Integer.valueOf(this.parametroSistemaService.findByNome("PZ_COLETA_REMESSA").getVlParametroSistema());
                    } catch (Exception e) {
                        this.pzColeta = 0;
                    }

                    this.listRemessaRelFaturamentoAB = this.remessaService.relatorioFaturamentoTipoAB(filtroRelatorio);
                    this.buildSumarioRelatorioTipoAB();

                    JavaScriptUtils.update("frmRelatorioTipoC", "frmRelatorioTipoAB");

                    return;
                }

                if (TipoRelatorioFaturamentoEnum.REMESSA_C.equals(this.tipoRelatorio)) {

                    this.listRemessaRelFaturamentoTipoC = this.remessaService.relatorioFaturamentoTipoC(filtroRelatorio);
                    this.buildSumarioRelatorioTipoC();

                    JavaScriptUtils.update("frmRelatorioTipoC", "frmRelatorioTipoAB");

                    return;
                }
            }
            
            qtdTotalSolicitada = 0;
            qtdTotalDisponibilizada = 0;
            qtdTotalNaoLocalizada = 0;
            qtdTotalDisponibilizadaNoPrazo = 0;
            idnlTotal = 0d;
            idlpTotal = 0d;

            // Filtro
            RelatorioSuporteDTO relatorioSuporte = new RelatorioSuporteDTO();
            relatorioSuporte.setDataInicio(relatorioVisitor.getDataInicio());
            relatorioSuporte.setDataFim(relatorioVisitor.getDataFim());
            if (relatorioVisitor.getBase() != null) {
                relatorioSuporte.setBase(relatorioVisitor.getBase());
            }

            try {
                listaFaturamento = new ArrayList<RelatorioFaturamentoDTO>();
                mapSubtotalFaturamento = new HashMap<Long, Map<Long, RelatorioFaturamentoDTO>>();
                listaFaturamento = tramiteRequisicaoDocumentoService.consultaRelatorioFaturamento(relatorioSuporte);
                pesquisaRealizada = true;
                resetDatatable();
                for (RelatorioFaturamentoDTO lista : listaFaturamento) {
                    qtdTotalSolicitada = qtdTotalSolicitada + lista.getQtdSolicitada();
                    qtdTotalDisponibilizada = qtdTotalDisponibilizada + lista.getQtdDisponibilizada();
                    qtdTotalDisponibilizadaNoPrazo = qtdTotalDisponibilizadaNoPrazo + lista.getQtdDispNoPrazo();
                    
                    qtdTotalNaoLocalizada = qtdTotalNaoLocalizada + lista.getQtdNaoLocalizada();
                    
                    // Cria o map com o sumário agrupado por base e suporte
                    Map<Long, RelatorioFaturamentoDTO> mapSubtotalBase = mapSubtotalFaturamento.get((Long)lista.getBase().getId());
                    if (ObjectUtils.isNullOrEmpty(mapSubtotalBase)) {
                        mapSubtotalBase = new HashMap<Long, RelatorioFaturamentoDTO>(); 
                    }
                    
                    Long idSuporte = (!ObjectUtils.isNullOrEmpty(lista.getSuporte())) ? (Long)lista.getSuporte().getId() : -1;
                    RelatorioFaturamentoDTO subtotal = mapSubtotalBase.get(idSuporte);
                    if (ObjectUtils.isNullOrEmpty(subtotal)) {
                        subtotal = new RelatorioFaturamentoDTO();
                        subtotal.setQtdSolicitada(0);
                        subtotal.setQtdDisponibilizada(0);
                        subtotal.setQtdDispNoPrazo(0);
                        subtotal.setQtdNaoLocalizada(0);
                        subtotal.setIdlp(BigDecimal.ZERO.doubleValue());
                        subtotal.setIdnl(BigDecimal.ZERO.doubleValue());
                    }
                    
                    subtotal.setQtdSolicitada(subtotal.getQtdSolicitada() + lista.getQtdSolicitada());
                    subtotal.setQtdDisponibilizada(subtotal.getQtdDisponibilizada() + lista.getQtdDisponibilizada());
                    subtotal.setQtdDispNoPrazo(subtotal.getQtdDispNoPrazo() + lista.getQtdDispNoPrazo());
                    subtotal.setQtdNaoLocalizada(subtotal.getQtdNaoLocalizada() + lista.getQtdNaoLocalizada());
                    
                    mapSubtotalBase.put(idSuporte, subtotal);
                    mapSubtotalFaturamento.put((Long)lista.getBase().getId(), mapSubtotalBase);
                }

                idlpTotal = (new Double(qtdTotalDisponibilizadaNoPrazo) / new Double(qtdTotalDisponibilizada)) * 100;
                idlpTotal = (Double.isNaN(idlpTotal)) ? BigDecimal.ZERO.doubleValue() : idlpTotal;
                    
                idnlTotal = (new Double(qtdTotalNaoLocalizada) / new Double(qtdTotalSolicitada)) * 100;
                idnlTotal = (Double.isNaN(idnlTotal)) ? BigDecimal.ZERO.doubleValue() : idnlTotal;

            } catch (BusinessException be) {
                for (String message : be.getErroList()) {
                    facesMessager.addMessageError(message);
                }
                return;
            } catch (Exception e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                return;
            }
        } else {
            listaFaturamento = new ArrayList<RelatorioFaturamentoDTO>();
            pesquisaRealizada = false;
        }

        listaFiltro = listaFaturamento;
        JavaScriptUtils.update("frmRelatorioTipoC", "frmRelatorioTipoAB");
        
        DataTable component = (DataTable) JavaScriptUtils.findComponentById("formConsulta:dataTableCrud");
        if (component != null) {
          String valueExprs = "suporte.nome";
          FacesContext facesContext = FacesContext.getCurrentInstance();
          ELContext elContext = facesContext.getELContext();
          ExpressionFactory elFactory = facesContext.getApplication().getExpressionFactory();
          ValueExpression valueExpression = elFactory.createValueExpression(elContext, valueExprs, String.class);
          component.setValueExpression("sortBy", valueExpression);
        }
    }

    public void buildSumarioRelatorioTipoC() {

        if (!ObjectUtils.isNullOrEmpty(this.listRemessaRelFaturamentoTipoC)) {

            Set<Long> unidades = new HashSet<Long>();
            int totalNovosMovimentos = 0;
            int totalMovimentosUltimos90Dias = 0;
            int totalMovimentosApos90Dias = 0;

            for (RemessaVO remessa : this.listRemessaRelFaturamentoTipoC) {
                unidades.add((Long) remessa.getUnidadeSolicitante().getId());

                String novos = remessa.getNovosMovimentos();
                if (!novos.isEmpty()) {
                    // String novos representa uma String contendo as datas, com separador ";". O length do split representa, então, a quantidade de datas
                    // (itens) encontrados.
                    totalNovosMovimentos += novos.split(";").length;
                }

                String ate90Dias = remessa.getMovimentosUltimos90Dias();
                if (!ate90Dias.isEmpty()) {
                    // String ate90Dias representa uma String contendo as datas, com separador ";". O length do split representa, então, a quantidade de datas
                    // (itens) encontrados.
                    totalMovimentosUltimos90Dias += ate90Dias.split(";").length;
                }

                String apos90Dias = remessa.getMovimentosApos90Dias();
                if (!apos90Dias.isEmpty()) {
                    // String apos90Dias representa uma String contendo as datas, com separador ";". O length do split representa, então, a quantidade de datas
                    // (itens) encontrados.
                    totalMovimentosApos90Dias += apos90Dias.split(";").length;
                }
            }

            this.setTotalUnidSolicitantes(unidades.size());
            this.setTotalNovosMovimentos(totalNovosMovimentos);
            this.setTotalMovimentosUltimos90Dias(totalMovimentosUltimos90Dias);
            this.setTotalMovimentosApos90Dias(totalMovimentosApos90Dias);
        }
    }

    public void buildSumarioRelatorioTipoAB() {

        this.totalAgendamentos = 0;
        this.totalRecebimentos = 0;
        this.totalConferencias = 0;
        this.totalFechamentos = 0;
        this.totalQtdCaixa = 0L;
        this.totalRemessaNoPrazo = 0;
        this.totalRemessaForaPrazo = 0;

        if (!ObjectUtils.isNullOrEmpty(this.listRemessaRelFaturamentoAB)) {

            for (ViewRelatorioFaturamentoABVO remessa : this.listRemessaRelFaturamentoAB) {
                if (!ObjectUtils.isNullOrEmpty(remessa.getDataHoraAgendamento())) {
                    this.totalAgendamentos++;
                }

                if (!ObjectUtils.isNullOrEmpty(remessa.getDataHoraRecebimento())) {
                    this.totalRecebimentos++;
                }

                if (!ObjectUtils.isNullOrEmpty(remessa.getDataHoraConferencia())) {
                    this.totalConferencias++;
                }

                if (!ObjectUtils.isNullOrEmpty(remessa.getDataHoraFechamento())) {
                    this.totalFechamentos++;
                }

                if (!ObjectUtils.isNullOrEmpty(remessa.getQtdCaixa())) {
                    this.totalQtdCaixa += remessa.getQtdCaixa();
                }

                if (remessa.isDentroPrazo(this.pzColeta)) {
                    this.totalRemessaNoPrazo++;
                } else {
                    this.totalRemessaForaPrazo++;
                }
            }
        }
    }

    /**
     * Preenche a data fim com a data atual quando o usuário não preenche a mesma.
     */
    public void preencheDataFimDefault() {
        if ((!Util.isNullOuVazio(relatorioVisitor.getDataInicio())) && Util.isNullOuVazio(relatorioVisitor.getDataFim())
                && (DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim") == null || !DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim"))) {
            relatorioVisitor.setDataFim(new Date());
            updateComponentes("formConsulta:idDataFim");
        }
    }

    @SuppressWarnings("unused")
    private TramiteRequisicaoVO createFiltro() {
        TramiteRequisicaoVO filter = new TramiteRequisicaoVO();
        if (relatorioVisitor.getBase() != null) {
            EmpresaContratoVO empresaContrato = new EmpresaContratoVO();
            empresaContrato.setBase(relatorioVisitor.getBase());

            RequisicaoVO requisicao = new RequisicaoVO();
            requisicao.setEmpresaContrato(empresaContrato);

            RequisicaoDocumentoVO requisicaoDoc = new RequisicaoDocumentoVO();
            // requisicaoDoc.setRequisicao(requisicao);

            filter = new TramiteRequisicaoVO();
            // filter.setRequisicaoDocumento(requisicaoDoc);
        }

        /*
         * if (relatorioVisitor.getSuporte() != null){ filter.setSuporte(relatorioVisitor.getSuporte()); }
         */

        return filter;
    }

    /**
     * Validação de campos obrigatórios no server-side.
     * 
     * @return
     */
    private Boolean validaCamposObrigatorios() {
        if (Util.isNullOuVazio(relatorioVisitor.getDataInicio())
                && (DataConverterSIRED.getMapDataInvalidaSessao().get("idDataInicio") == null || !DataConverterSIRED.getMapDataInvalidaSessao().get(
                        "idDataInicio"))) {
            facesMessager.addMessageError(getRequiredMessage("geral.label.dataInicio"));
            giveFocus("formConsulta:idDataInicio_input");
            return false;
        }
        
        if (Util.isNullOuVazio(this.relatorioVisitor.getBase())) {
          facesMessager.addMessageError(getRequiredMessage("base.label.base"));
          return false;
        }

        return true;
    }

    private Boolean isDadosValidos() {

        boolean retorno = true;
        boolean dataValida = true;

        if (DataConverterSIRED.getMapDataInvalidaSessao().get("idDataInicio") != null && DataConverterSIRED.getMapDataInvalidaSessao().get("idDataInicio")) {

            DataConverterSIRED.getMapDataInvalidaSessao().put("idDataInicio", null);
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI014", "geral.label.dataInicio"));
            dataValida = false;
        }

        if (DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim") != null && DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim")) {
            DataConverterSIRED.getMapDataInvalidaSessao().put("idDataFim", null);
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI014", "geral.label.dataFim"));
            dataValida = false;
        }

        if (dataValida && relatorioVisitor.getDataInicio().after(new Date())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataInicio"));
            retorno = false;
        }

        if (dataValida && relatorioVisitor.getDataFim().after(new Date())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataFim"));
            retorno = false;
        }

        if (dataValida && retorno && relatorioVisitor.getDataInicio().after(relatorioVisitor.getDataFim())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA018"));
            retorno = false;
        }
        return (retorno && dataValida);
    }

    // MÉT0D0S DE ACESSO

    public RelatorioVisitor getRelatorioVisitor() {
        return relatorioVisitor;
    }

    public void setRelatorioVisitor(RelatorioVisitor relatorioFaturamento) {
        this.relatorioVisitor = relatorioFaturamento;
    }

    public List<BaseVO> getLitaBases() {
        return litaBases;
    }

    public void setLitaBases(List<BaseVO> litaBases) {
        this.litaBases = litaBases;
    }

    public Boolean getPesquisaRealizada() {
        return pesquisaRealizada;
    }

    public void setPesquisaRealizada(Boolean pesquisaRealizada) {
        this.pesquisaRealizada = pesquisaRealizada;
    }

    public List<RelatorioFaturamentoDTO> getListaFaturamento() {
        return listaFaturamento;
    }

    public void setListaFaturamento(List<RelatorioFaturamentoDTO> listaFaturamento) {
        this.listaFaturamento = listaFaturamento;
    }

    public List<RelatorioFaturamentoDTO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<RelatorioFaturamentoDTO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public int getQtdTotalSolicitada() {
        return qtdTotalSolicitada;
    }

    public void setQtdTotalSolicitada(int qtdTotalSolicitada) {
        this.qtdTotalSolicitada = qtdTotalSolicitada;
    }

    public int getQtdTotalDisponibilizada() {
        return qtdTotalDisponibilizada;
    }

    public void setQtdTotalDisponibilizada(int qtdTotalDisponibilizada) {
        this.qtdTotalDisponibilizada = qtdTotalDisponibilizada;
    }

    public int getQtdTotalDisponibilizadaNoPrazo() {
        return qtdTotalDisponibilizadaNoPrazo;
    }

    public void setQtdTotalDisponibilizadaNoPrazo(int qtdtotalDisponibilizadaNoPrazo) {
        this.qtdTotalDisponibilizadaNoPrazo = qtdtotalDisponibilizadaNoPrazo;
    }

    public int getQtdTotalNaoLocalizada() {
        return qtdTotalNaoLocalizada;
    }

    public void setQtdTotalNaoLocalizada(int qtdTotalNaoLocalizada) {
        this.qtdTotalNaoLocalizada = qtdTotalNaoLocalizada;
    }

    public Double getIdlpTotal() {
        return idlpTotal;
    }

    public String getIdlpTotalFormatado() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(getIdlpTotal());
    }

    public void setIdlpTotal(Double idlpTotal) {
        this.idlpTotal = idlpTotal;
    }

    public Double getIdnlTotal() {
        return idnlTotal;
    }

    public String getIdnlTotalFormatado() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(getIdnlTotal());
    }

    public void setIdnlTotal(Double idnlTotal) {
        this.idnlTotal = idnlTotal;
    }

    public String getFiltroPesquisar() {
        return filtroPesquisar;
    }

    public void setFiltroPesquisar(String filtroPesquisar) {
        this.filtroPesquisar = filtroPesquisar;
    }

    private boolean obtemBaseUnidadeUsuarioAdministrador() {
        UsuarioLdap usuario = JSFUtil.getUsuario();

        // Se não existe usuário na sessão ou usuário não possui perfil, não
        // permite a consulta.
        if (usuario == null || (usuario != null && usuario.getListaGruposLdap() == null)) {
            String usuarioLogado = (String) JSFUtil.getSessionMapValue("loggedMatricula");
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI026", usuarioLogado));
            logger.error(LogUtils.getMensagemPadraoLog(MensagemUtils.obterMensagem("MI026", usuarioLogado), "RelatorioFaturamento", "localizar"));
            return false;
        }

        // Se usuário não é GESTOR nem AUDITOR, então pesquisa a Base da Unidade
        // de Lotação do Usuário
        if (!getPerfilAcessoBase()) {
            try {
                List<BaseVO> bases = tramiteRequisicaoDocumentoService.consultaBasePorIdUnidade(usuario.getCoUnidade().longValue());
                if (!Util.isNullOuVazio(bases)) {
                    relatorioVisitor.setBase(bases.get(0));
                } else {
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MI026", usuario.getNuMatricula()));
                    logger.error(LogUtils.getMensagemPadraoLog(MensagemUtils.obterMensagem("MI026", usuario.getNuMatricula()), "RelatorioFaturamento",
                            "localizar"));
                    return false;
                }
            } catch (BusinessException be) {
                for (String message : be.getErroList()) {
                    facesMessager.addMessageError(message);
                }
                return false;
            } catch (AppException e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                logger.error(LogUtils.getMensagemPadraoLog(Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n"), "RelatorioFaturamento",
                        "obtemBaseUnidadeUsuarioAdministrador"));
                return false;
            }
        }
        return true;
    }
    
    public StreamedContent exportar() throws FileNotFoundException, IOException, AppException {

      ExportarFaturamentoXLS exportador = new ExportarFaturamentoXLS();
      exportador.setData(this.listaFaturamento);
      exportador.setMapSubtotalFaturamento(this.mapSubtotalFaturamento);
      exportador.setQtdTotalSolicitada(this.qtdTotalSolicitada);
      exportador.setQtdTotalDisponibilizada(this.qtdTotalDisponibilizada);
      exportador.setQtdTotalDisponibilizadaNoPrazo(this.qtdTotalDisponibilizadaNoPrazo);
      exportador.setQtdTotalNaoLocalizada(this.qtdTotalNaoLocalizada);
      exportador.setIdlpTotal(this.idlpTotal);
      exportador.setIdnlTotal(this.idnlTotal);
      exportador.setRelatorioVisitor(this.relatorioVisitor);

      String caminho = System.getProperty(Constantes.CAMINHO_UPLOAD);
      String filename = FileUtils.appendDateTimeToFileName("Relatorio_Faturamento.xls", new Date());
      File relatorio = exportador.export(caminho + filename);

      return RequestUtils.download(relatorio, relatorio.getName());
  }

    public TipoRelatorioFaturamentoEnum[] getListTipoRelatorio() {
        return TipoRelatorioFaturamentoEnum.values();
    }

    public TipoRelatorioFaturamentoEnum getTipoRelatorio() {
        return this.tipoRelatorio;
    }

    public void setTipoRelatorio(TipoRelatorioFaturamentoEnum tipoRelatorio) {
        this.tipoRelatorio = tipoRelatorio;
    }

    public int getTotalListRemessaRelFaturamentoTipoC() {
        return listRemessaRelFaturamentoTipoC.size();
    }

    public List<RemessaVO> getListRemessaRelFaturamentoTipoC() {
        return listRemessaRelFaturamentoTipoC;
    }

    public void setListRemessaRelFaturamentoTipoC(List<RemessaVO> listRemessaRelFaturamentoTipoC) {
        this.listRemessaRelFaturamentoTipoC = listRemessaRelFaturamentoTipoC;
    }

    public List<ViewRelatorioFaturamentoABVO> getListRemessaRelFaturamentoAB() {
        return listRemessaRelFaturamentoAB;
    }

    public void setListRemessaRelFaturamentoAB(List<ViewRelatorioFaturamentoABVO> listRemessaRelFaturamentoAB) {
        this.listRemessaRelFaturamentoAB = listRemessaRelFaturamentoAB;
    }

    public Integer getTotalUnidSolicitantes() {
        return totalUnidSolicitantes;
    }

    public void setTotalUnidSolicitantes(Integer totalUnidSolicitantes) {
        this.totalUnidSolicitantes = totalUnidSolicitantes;
    }

    public Integer getTotalNovosMovimentos() {
        return totalNovosMovimentos;
    }

    public void setTotalNovosMovimentos(Integer totalNovosMovimentos) {
        this.totalNovosMovimentos = totalNovosMovimentos;
    }

    public Integer getTotalMovimentosUltimos90Dias() {
        return totalMovimentosUltimos90Dias;
    }

    public void setTotalMovimentosUltimos90Dias(Integer totalMovimentosUltimos90Dias) {
        this.totalMovimentosUltimos90Dias = totalMovimentosUltimos90Dias;
    }

    public Integer getTotalMovimentosApos90Dias() {
        return totalMovimentosApos90Dias;
    }

    public void setTotalMovimentosApos90Dias(Integer totalMovimentosApos90Dias) {
        this.totalMovimentosApos90Dias = totalMovimentosApos90Dias;
    }

    public Integer getPzColeta() {
        return pzColeta;
    }

    public void setPzColeta(Integer pzColeta) {
        this.pzColeta = pzColeta;
    }

    public Integer getTotalAgendamentos() {
        return totalAgendamentos;
    }

    public void setTotalAgendamentos(Integer totalAgendamentos) {
        this.totalAgendamentos = totalAgendamentos;
    }

    public Integer getTotalRecebimentos() {
        return totalRecebimentos;
    }

    public void setTotalRecebimentos(Integer totalRecebimentos) {
        this.totalRecebimentos = totalRecebimentos;
    }

    public Integer getTotalConferencias() {
        return totalConferencias;
    }

    public void setTotalConferencias(Integer totalConferencias) {
        this.totalConferencias = totalConferencias;
    }

    public Integer getTotalFechamentos() {
        return totalFechamentos;
    }

    public void setTotalFechamentos(Integer totalFechamentos) {
        this.totalFechamentos = totalFechamentos;
    }

    public Long getTotalQtdCaixa() {
        return totalQtdCaixa;
    }

    public void setTotalQtdCaixa(Long totalQtdCaixa) {
        this.totalQtdCaixa = totalQtdCaixa;
    }

    public Integer getTotalRemessaNoPrazo() {
        return totalRemessaNoPrazo;
    }

    public void setTotalRemessaNoPrazo(Integer totalRemessaNoPrazo) {
        this.totalRemessaNoPrazo = totalRemessaNoPrazo;
    }

    public Integer getTotalRemessaForaPrazo() {
        return totalRemessaForaPrazo;
    }

    public void setTotalRemessaForaPrazo(Integer totalRemessaForaPrazo) {
        this.totalRemessaForaPrazo = totalRemessaForaPrazo;
    }

    public Boolean isRelatorioRequisicao() {
        return TipoRelatorioFaturamentoEnum.REQUISICAO.equals(this.tipoRelatorio);
    }

    public Boolean isRelatorioRemessaAB() {
        return TipoRelatorioFaturamentoEnum.REMESSA_AB.equals(this.tipoRelatorio);
    }

    public Boolean isRelatorioRemessaC() {
        return TipoRelatorioFaturamentoEnum.REMESSA_C.equals(this.tipoRelatorio);
    }
}
