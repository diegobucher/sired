package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.converter.DataConverterSIRED;
import br.gov.caixa.gitecsa.dto.RelatorioAtendimentoDTO;
import br.gov.caixa.gitecsa.dto.ResultadoAtendimentoDTO;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.TramiteRequisicaoDocumentoService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class RelatorioAtendimentoController extends BaseConsultaCRUD<TramiteRequisicaoVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    private static String NOME_RELATORIO = "RELATÓRIO DE ATENDIMENTO";
    private RelatorioAtendimentoDTO relatorioAtendimento;
    private List<BaseVO> listaBases;
    private List<ResultadoAtendimentoDTO> listaAtendimento;
    private List<ResultadoAtendimentoDTO> listaFiltro;
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
        filtroPesquisar = new String();
        try {
            if (getPerfilAcessoBase()) {
                listaBases = baseService.findAll();
            }
            relatorioAtendimento = new RelatorioAtendimentoDTO();
            listaAtendimento = new ArrayList<ResultadoAtendimentoDTO>();
            listaFiltro = listaAtendimento;
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

            try {
                listaAtendimento = tramiteRequisicaoDocumentoService.consultaRelatorioAtendimento(relatorioAtendimento);
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
            listaAtendimento = new ArrayList<ResultadoAtendimentoDTO>();
        }

        listaFiltro = listaAtendimento;
    }

    /**
     * Preenche a data fim com a data atual quando o usuário não preenche a mesma.
     */
    public void preencheDataFimDefault() {
        if ((!Util.isNullOuVazio(relatorioAtendimento.getDataInicio())) && Util.isNullOuVazio(relatorioAtendimento.getDataFim())
                && (DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim") == null || !DataConverterSIRED.getMapDataInvalidaSessao().get("idDataFim"))) {
            relatorioAtendimento.setDataFim(new Date());
            updateComponentes("formConsulta:idDataFim");
        }
    }

    /**
     * Validação de campos obrigatórios no server-side.
     * 
     * @return
     */
    private Boolean validaCamposObrigatorios() {
        if (Util.isNullOuVazio(relatorioAtendimento.getDataInicio())
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

        if (dataValida && relatorioAtendimento.getDataInicio().after(new Date())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataInicio"));
            retorno = false;
        }

        if (dataValida && relatorioAtendimento.getDataFim().after(new Date())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA051", "geral.label.dataFim"));
            retorno = false;
        }

        if (dataValida && retorno && relatorioAtendimento.getDataInicio().after(relatorioAtendimento.getDataFim())) {
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

        // Se usuario é GESTOR ou AUDITOR
        if (JSFUtil.isPerfil("GEST") || JSFUtil.isPerfil("ADT")) {
            return true;
        }
        return false;
    }

    public List<BaseVO> getListaBases() {
        return listaBases;
    }

    public void setListaBases(List<BaseVO> listaBases) {
        this.listaBases = listaBases;
    }

    public RelatorioAtendimentoDTO getRelatorioAtendimento() {
        return relatorioAtendimento;
    }

    public void setRelatorioAtendimento(RelatorioAtendimentoDTO relatorioAtendimento) {
        this.relatorioAtendimento = relatorioAtendimento;
    }

    public List<ResultadoAtendimentoDTO> getListaAtendimento() {
        return listaAtendimento;
    }

    public void setListaAtendimento(List<ResultadoAtendimentoDTO> listaAtendimento) {
        this.listaAtendimento = listaAtendimento;
    }

    public List<ResultadoAtendimentoDTO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<ResultadoAtendimentoDTO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public Boolean getPesquisaRealizada() {
        return pesquisaRealizada;
    }

    public void setPesquisaRealizada(Boolean pesquisaRealizada) {
        this.pesquisaRealizada = pesquisaRealizada;
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
            logger.error(LogUtils.getMensagemPadraoLog(MensagemUtils.obterMensagem("MI026", usuarioLogado), "RelatorioAtendimento", "localizar"));
            return false;
        }

        // Se usuário não é GESTOR nem AUDITOR, então pesquisa a Base da Unidade
        // de Lotação do Usuário
        if (!getPerfilAcessoBase()) {
            try {
                List<BaseVO> bases = tramiteRequisicaoDocumentoService.consultaBasePorIdUnidade(usuario.getCoUnidade().longValue());
                if (!Util.isNullOuVazio(bases)) {
                    relatorioAtendimento.setBase(bases.get(0));
                } else {
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MI026", usuario.getNuMatricula()));
                    logger.error(LogUtils.getMensagemPadraoLog(MensagemUtils.obterMensagem("MI026", usuario.getNuMatricula()), "RelatorioAtendimento",
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
                return false;
            }
        }
        return true;
    }

}
