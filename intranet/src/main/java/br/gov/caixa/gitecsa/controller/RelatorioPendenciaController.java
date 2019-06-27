package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.converter.DataConverterSIRED;
import br.gov.caixa.gitecsa.dto.RelatorioPendenciaDTO;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.TipoDemandaService;
import br.gov.caixa.gitecsa.service.TramiteRequisicaoDocumentoService;
import br.gov.caixa.gitecsa.service.ViewPendenciaService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.ViewPendenciaVO;
import br.gov.caixa.gitecsa.util.JSFUtil;
import br.gov.caixa.gitecsa.visitor.RelatorioVisitor;

@Named
@ViewScoped
public class RelatorioPendenciaController extends BaseConsultaCRUD<TramiteRequisicaoVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    private static String NOME_RELATORIO = "RELATÓRIO DE PENDÊNCIA";
    private RelatorioPendenciaDTO relatorioPendencia;
    private List<BaseVO> listaBases;
    private List<ViewPendenciaVO> listaPendencia;
    private List<ViewPendenciaVO> listaFiltro;
    private Boolean pesquisaRealizada;
    private RelatorioVisitor relatorioVisitor;
    private String filtroPesquisar;
    private List<String> descricaoTiposDeDemanda;

    @Inject
    private BaseService baseService;

    @Inject
    private TipoDemandaService tipoDemandaService;

    @Inject
    private ViewPendenciaService viewPendenciaService;

    @Inject
    private TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;
    
    @Inject
    private RequisicaoService requisicaoService;

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
        pesquisaRealizada = false;
        filtroPesquisar = new String();
        try {
            if (getPerfilAcessoBase()) {
                listaBases = baseService.findAll();
            }

            relatorioVisitor = new RelatorioVisitor();
            relatorioVisitor.setTipoDemanda(new TipoDemandaVO());

            relatorioPendencia = new RelatorioPendenciaDTO();
            listaPendencia = new ArrayList<ViewPendenciaVO>();
            listaFiltro = listaPendencia;

            descricaoTiposDeDemanda = tipoDemandaService.consultaDescricaoTiposDemanda();

        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
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
     * Exporta o Relatorio De Acordo com o nome Informado
     * 
     * @param document
     */
    public void preExportar(Object document) {
        RelatorioSiredUtil relatorio = new RelatorioSiredUtil(NOME_RELATORIO, document);
        relatorio.preExportar();
    }

    public void localizar() throws AppException {
        filtroPesquisar = new String();
        preencheDataFimDefault();
        if (validaCamposObrigatorios() && isDadosValidos() && obtemBaseUnidadeUsuarioAdministrador()) {

            // criação do filtro
            ViewPendenciaVO relatorioPendencia = new ViewPendenciaVO();
            if (relatorioVisitor.getBase() != null && relatorioVisitor.getBase().getId() != null) {
                relatorioPendencia.setIdBase((Long) relatorioVisitor.getBase().getId());
            }

            if (relatorioVisitor.getTipoDemanda() != null && relatorioVisitor.getTipoDemanda().getNome() != null) {
                relatorioPendencia.setNomeDemanda(relatorioVisitor.getTipoDemanda().getNome());
            }

            if (!Util.isNullOuVazio(relatorioVisitor.getDataInicio())) {

                Calendar dtInicio = Calendar.getInstance();
                dtInicio.setTime(relatorioVisitor.getDataInicio());
                dtInicio.set(Calendar.MILLISECOND, 0);
                dtInicio.set(Calendar.SECOND, 0);
                dtInicio.set(Calendar.MINUTE, 0);
                dtInicio.set(Calendar.HOUR_OF_DAY, 0);

                relatorioVisitor.setDataInicio(dtInicio.getTime());
            }

            if (!Util.isNullOuVazio(relatorioVisitor.getDataFim())) {

                Calendar dtFim = Calendar.getInstance();
                dtFim.setTime(relatorioVisitor.getDataFim());
                dtFim.set(Calendar.SECOND, 59);
                dtFim.set(Calendar.MINUTE, 59);
                dtFim.set(Calendar.HOUR_OF_DAY, 23);

                relatorioVisitor.setDataFim(dtFim.getTime());
            }

            try {
                listaPendencia = viewPendenciaService.findByParameters(relatorioPendencia, relatorioVisitor);
                pesquisaRealizada = true;
                resetDatatable();
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
            pesquisaRealizada = false;
            listaPendencia = new ArrayList<ViewPendenciaVO>();
        }

        listaFiltro = listaPendencia;
    }

    @SuppressWarnings("unused")
    private TramiteRequisicaoVO createFiltro() {
        TramiteRequisicaoVO filter = new TramiteRequisicaoVO();
        if (relatorioPendencia.getBase() != null) {
            EmpresaContratoVO empresaContrato = new EmpresaContratoVO();
            empresaContrato.setBase(relatorioPendencia.getBase());

            RequisicaoVO requisicao = new RequisicaoVO();
            requisicao.setEmpresaContrato(empresaContrato);

            RequisicaoDocumentoVO requisicaoDoc = new RequisicaoDocumentoVO();
            // requisicaoDoc.setRequisicao(requisicao);

            // filter = new TramiteRequisicaoDocumentoVO();
            // filter.setRequisicaoDocumento(requisicaoDoc);
        }

        if (relatorioPendencia.getTipoDemanada() != null) {

            TipoDemandaVO tipoDemanda = new TipoDemandaVO();
            tipoDemanda.setNome(relatorioPendencia.getTipoDemanada().getId());

            /*
             * if (filter.getRequisicaoDocumento() != null){ filter.getRequisicaoDocumento().setTipoDemanda(tipoDemanda); }else{
             * RequisicaoDocumentoVO requisicaoDoc = new RequisicaoDocumentoVO(); filter.setRequisicaoDocumento(requisicaoDoc);
             * filter.getRequisicaoDocumento().setTipoDemanda(tipoDemanda); }
             */
        }

        return filter;
    }

    /**
     * Preenche a data fim com a data atual quando o usuário não preenche a mesma.
     */
    public void preencheDataFimDefault() {
        if ((!Util.isNullOuVazio(relatorioVisitor.getDataInicio())) && Util.isNullOuVazio(relatorioVisitor.getDataFim())
                && (DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim") == null || !DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim"))) {

            Calendar hoje = Calendar.getInstance(new Locale("pt", "BR"));
            hoje.set(Calendar.SECOND, 59);
            hoje.set(Calendar.MINUTE, 59);
            hoje.set(Calendar.HOUR_OF_DAY, 23);

            relatorioVisitor.setDataFim(hoje.getTime());
            updateComponentes("formConsulta:idDataFim");
        }
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

        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.SECOND, 59);
        hoje.set(Calendar.MINUTE, 59);
        hoje.set(Calendar.HOUR_OF_DAY, 23);

        if (dataValida && relatorioVisitor.getDataFim().after(hoje.getTime())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataFim"));
            retorno = false;
        }

        if (dataValida && retorno && relatorioVisitor.getDataInicio().after(relatorioVisitor.getDataFim())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA018"));
            return false;
        }
        return (retorno && dataValida);
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

    public List<BaseVO> getListaBases() {
        return listaBases;
    }

    public void setListaBases(List<BaseVO> litaBases) {
        this.listaBases = litaBases;
    }

    public RelatorioPendenciaDTO getRelatorioPendencia() {
        return relatorioPendencia;
    }

    public void setRelatorioPendencia(RelatorioPendenciaDTO relatorioPendencia) {
        this.relatorioPendencia = relatorioPendencia;
    }

    public Boolean getPesquisaRealizada() {
        return pesquisaRealizada;
    }

    public void setPesquisaRealizada(Boolean pesquisaRealizada) {
        this.pesquisaRealizada = pesquisaRealizada;
    }

    public List<ViewPendenciaVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<ViewPendenciaVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public List<ViewPendenciaVO> getListaPendencia() {
        return listaPendencia;
    }

    public void setListaPendencia(List<ViewPendenciaVO> listaPendencia) {
        this.listaPendencia = listaPendencia;
    }

    public RelatorioVisitor getRelatorioVisitor() {
        return relatorioVisitor;
    }

    public void setRelatorioVisitor(RelatorioVisitor relatorioVisitor) {
        this.relatorioVisitor = relatorioVisitor;
    }

    public String getFiltroPesquisar() {
        return filtroPesquisar;
    }

    public void setFiltroPesquisar(String filtroPesquisar) {
        this.filtroPesquisar = filtroPesquisar;
    }

    public boolean isDesabilitarBotaoExportar() {
        if (listaFiltro != null && listaFiltro.size() > 0) {
            return false;
        }

        return true;
    }

    public int getQtdDocsAtrasados() {
        int qtd = 0;
        if (this.listaFiltro == null || (this.listaFiltro != null && this.listaFiltro.size() == 0)) {
            return qtd;
        }

        for (ViewPendenciaVO pendencia : this.listaFiltro) {
            if (pendencia.getAtraso() > 0) {
                qtd++;
            }
        }
        return qtd;
    }

    private boolean obtemBaseUnidadeUsuarioAdministrador() {
        UsuarioLdap usuario = JSFUtil.getUsuario();

        // Se não existe usuário na sessão ou usuário não possui perfil, não
        // permite a consulta.
        if (usuario == null || (usuario != null && usuario.getListaGruposLdap() == null)) {
            String usuarioLogado = (String) JSFUtil.getSessionMapValue("loggedMatricula");
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI026", usuarioLogado));
            logger.error(LogUtils.getMensagemPadraoLog(MensagemUtils.obterMensagem("MI026", usuarioLogado), "RelatorioPendencia", "localizar"));
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
                    logger.error(LogUtils.getMensagemPadraoLog(MensagemUtils.obterMensagem("MI026", usuario.getNuMatricula()), "RelatorioPendencia",
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
                logger.error(LogUtils.getMensagemPadraoLog(Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n"), "RelatorioPendencia", "localizar"));
                return false;
            }
        }
        return true;
    }
    
    public RequisicaoVO getRequisicaoPendencia(Long codigoRequisicao) {
        return this.requisicaoService.findByCodigo(codigoRequisicao);
    }

    public List<String> getDescricaoTiposDeDemanda() {
        return descricaoTiposDeDemanda;
    }

    public void setDescricaoTiposDeDemanda(List<String> descricaoTiposDeDemanda) {
        this.descricaoTiposDeDemanda = descricaoTiposDeDemanda;
    }
}
