package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.converter.DataConverterSIRED;
import br.gov.caixa.gitecsa.dto.RelatorioSuporteDTO;
import br.gov.caixa.gitecsa.dto.TramiteRequisicaoSuporteDTO;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.TramiteRequisicaoDocumentoService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteAtendimentoRequisicaoVisitor;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class RelatorioSuporteController extends BaseConsultaCRUD<TramiteRequisicaoVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    private static String NOME_RELATORIO = "RELATÓRIO DE ATENDIMENTO";
    private RelatorioSuporteDTO relatorio;
    private List<BaseVO> litaBases;
    private List<TramiteRequisicaoSuporteDTO> listaConsulta;
    private List<TramiteRequisicaoSuporteDTO> listaFiltro;
    private Boolean pesquisaRealizada;
    private String filtroPesquisar;

    @Inject
    private BaseService baseService;
    @Inject
    private TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;

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
                litaBases = baseService.findAll();
            }
            relatorio = new RelatorioSuporteDTO();
            listaConsulta = new ArrayList<TramiteRequisicaoSuporteDTO>();
            listaFiltro = listaConsulta;
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

            // visitor
            TramiteAtendimentoRequisicaoVisitor visitor = new TramiteAtendimentoRequisicaoVisitor();
            visitor.setDataInicio(relatorio.getDataInicio());
            visitor.setDataFim(relatorio.getDataFim());

            // criação do filtro
            try {
                listaConsulta = tramiteRequisicaoDocumentoService.consultaRelatorioSuporte(relatorio);
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
            listaConsulta = new ArrayList<TramiteRequisicaoSuporteDTO>();
        }

        listaFiltro = listaConsulta;
    }

    /**
     * Preenche a data fim com a data atual quando o usuário não preenche a mesma.
     */
    public void preencheDataFimDefault() {

        if ((!Util.isNullOuVazio(relatorio.getDataInicio()))
                && Util.isNullOuVazio(relatorio.getDataFim())
                && (DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim") == null || (!DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim")))) {
            relatorio.setDataFim(new Date());
            updateComponentes("formConsulta:idDataFim");
        }
    }

    /**
     * Validação de campos obrigatórios no server-side.
     * 
     * @return
     */
    private Boolean validaCamposObrigatorios() {
        if (Util.isNullOuVazio(relatorio.getDataInicio())
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

        if (dataValida && relatorio.getDataInicio().after(new Date())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataInicio"));
            retorno = false;
        }

        if (dataValida && relatorio.getDataFim().after(new Date())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataFim"));
            retorno = false;
        }

        if (dataValida && retorno && relatorio.getDataInicio().after(relatorio.getDataFim())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA018"));
            retorno = false;
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

    public Integer getTotalQuantidadePapel() {
        Integer qtdPapel = 0;
        if (!ObjectUtils.isNullOrEmpty(listaFiltro)) {
            for (TramiteRequisicaoSuporteDTO tramite : listaFiltro) {
                qtdPapel = qtdPapel + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadePapel(), 0);
                tramite.setQuantidadeTotalPapel(qtdPapel);
            }
        }
        return qtdPapel;
    }

    public Integer getTotalQuantidadeMicroficha() {
        Integer qtd = 0;
        if (!ObjectUtils.isNullOrEmpty(listaFiltro)) {
            for (TramiteRequisicaoSuporteDTO tramite : listaFiltro) {
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMicroficha(), 0);
                tramite.setQuantidadeTotalMicroficha(qtd);
            }
        }
        return qtd;
    }

    public Integer getTotalQuantidadeMicrofilme() {
        Integer qtd = 0;
        if (!ObjectUtils.isNullOrEmpty(listaFiltro)) {
            for (TramiteRequisicaoSuporteDTO tramite : listaFiltro) {
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMicrofilme(), 0);
                tramite.setQuantidadeTotalMicrofilme(qtd);
            }
        }
        return qtd;
    }

    public Integer getTotalQuantidadeMidiaOptica() {
        Integer qtd = 0;
        if (!ObjectUtils.isNullOrEmpty(listaFiltro)) {
            for (TramiteRequisicaoSuporteDTO tramite : listaFiltro) {
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMidiaOptica(), 0);
                tramite.setQuantidadeTotalMidiaOptica(qtd);
            }
        }
        return qtd;
    }

    public Integer getTotalQuantidadeRepositorio() {
        Integer qtd = 0;
        if (!ObjectUtils.isNullOrEmpty(listaFiltro)) {
            for (TramiteRequisicaoSuporteDTO tramite : listaFiltro) {
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeRepositorio(), 0);
                tramite.setQuantidadeTotalRepositorio(qtd);
            }
        }
        return qtd;
    }

    public Integer getTotal() {
        Integer qtd = 0;
        if (!ObjectUtils.isNullOrEmpty(listaFiltro)) {
            for (TramiteRequisicaoSuporteDTO tramite : listaFiltro) {
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadePapel(), 0);
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMidiaOptica(), 0);
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMicrofilme(), 0);
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMicroficha(), 0);
                qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeRepositorio(), 0);
            }
        }
        return qtd;
    }

    public Integer getQuantidadeLinha(BaseVO base, Date dataHoraRegsitro) {
        Integer qtd = 0;
        if (!ObjectUtils.isNullOrEmpty(listaFiltro)) {
            for (TramiteRequisicaoSuporteDTO tramite : listaFiltro) {
                if (base != null && dataHoraRegsitro != null) {
                    if (tramite.getBase().getId().equals(base.getId()) && tramite.getDataHora().equals(dataHoraRegsitro)) {
                        qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadePapel(), 0);
                        qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMidiaOptica(), 0);
                        qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMicrofilme(), 0);
                        qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeMicroficha(), 0);
                        qtd = qtd + (Integer) ObjectUtils.defaultIfNull(tramite.getQuantidadeRepositorio(), 0);
                    }
                }
            }
        }
        return qtd;
    }

    public List<BaseVO> getLitaBases() {
        return litaBases;
    }

    public void setLitaBases(List<BaseVO> litaBases) {
        this.litaBases = litaBases;
    }

    public List<TramiteRequisicaoSuporteDTO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<TramiteRequisicaoSuporteDTO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public Boolean getPesquisaRealizada() {
        return pesquisaRealizada;
    }

    public void setPesquisaRealizada(Boolean pesquisaRealizada) {
        this.pesquisaRealizada = pesquisaRealizada;
    }

    public RelatorioSuporteDTO getRelatorio() {
        return relatorio;
    }

    public void setRelatorio(RelatorioSuporteDTO relatorio) {
        this.relatorio = relatorio;
    }

    public List<TramiteRequisicaoSuporteDTO> getListaConsulta() {
        return listaConsulta;
    }

    public void setListaConsulta(List<TramiteRequisicaoSuporteDTO> listaConsulta) {
        this.listaConsulta = listaConsulta;
    }

    public BaseService getBaseService() {
        return baseService;
    }

    public void setBaseService(BaseService baseService) {
        this.baseService = baseService;
    }

    public TramiteRequisicaoDocumentoService getTramiteRequisicaoDocumentoService() {
        return tramiteRequisicaoDocumentoService;
    }

    public void setTramiteRequisicaoDocumentoService(TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService) {
        this.tramiteRequisicaoDocumentoService = tramiteRequisicaoDocumentoService;
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
            logger.error(LogUtils.getMensagemPadraoLog(MensagemUtils.obterMensagem("MI026", usuarioLogado), "RelatorioSuporte", "localizar"));
            return false;
        }

        // Se usuário não é GESTOR nem AUDITOR, então pesquisa a Base da Unidade
        // de Lotação do Usuário
        if (!getPerfilAcessoBase()) {
            try {
                List<BaseVO> bases = tramiteRequisicaoDocumentoService.consultaBasePorIdUnidade(usuario.getCoUnidade().longValue());
                if (!Util.isNullOuVazio(bases)) {
                    relatorio.setBase(bases.get(0));
                } else {
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MI026", usuario.getNuMatricula()));
                    logger.error(LogUtils.getMensagemPadraoLog(MensagemUtils.obterMensagem("MI026", usuario.getNuMatricula()), "RelatorioSuporte", "localizar"));
                    return false;
                }
            } catch (BusinessException be) {
                for (String message : be.getErroList()) {
                    facesMessager.addMessageError(message);
                }
                return false;
            } catch (AppException e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                logger.error(LogUtils.getMensagemPadraoLog(Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n"), "RelatorioSuporte",
                        "obtemBaseUnidadeUsuarioAdministrador"));
                return false;
            }
        }
        return true;
    }

}
