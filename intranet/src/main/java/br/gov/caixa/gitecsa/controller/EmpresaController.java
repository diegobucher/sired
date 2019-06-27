package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.context.RequestContext;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.dto.EmpresaContratoDTO;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.EmpresaContratoService;
import br.gov.caixa.gitecsa.service.EmpresaService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.util.MascaraUtil;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;

@Named
@ViewScoped
public class EmpresaController extends BaseConsultaCRUD<EmpresaVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private EmpresaService empresaService;

    @Inject
    private EmpresaContratoService empresaContratoService;

    @Inject
    private BaseService baseService;

    private static String NOME_RELATORIO = "CADASTRO DE EMPRESA";

    private boolean faseEditar;

    private List<EmpresaContratoDTO> listaFiltro;

    private List<EmpresaContratoDTO> listaEmpresaContrato;

    private EmpresaContratoVO empresaContrato;

    private List<BaseVO> listaBase;
    private List<EmpresaVO> listaEmpresa;

    private EmpresaContratoVO empresaContratoExcluir;

    @Override
    protected EmpresaVO newInstance() {
        return new EmpresaVO();
    }

    @PostConstruct
    protected void init() throws AppException {
        try {
            obterLista();
            verificaFaseFormulario();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }
    }

    private void verificaFaseFormulario() {
        if (!Util.isNullOuVazio(getInstance().getId()))
            faseEditar = true;
        else
            faseEditar = false;
    }

    private void obterLista() {
        setLista(empresaService.findAllEager());

        listaEmpresaContrato = new ArrayList<EmpresaContratoDTO>();
        Map<Long, EmpresaVO> mapa = new HashMap<Long, EmpresaVO>();

        for (EmpresaVO empresa : getLista()) {

            if (!mapa.containsKey((Long) empresa.getId())) {

                mapa.put((Long) empresa.getId(), empresa);

                EmpresaContratoDTO empresaContratoDTO = new EmpresaContratoDTO();
                empresaContratoDTO.setEmpresa(empresa);

                for (EmpresaContratoVO empresaContrato : empresa.getEmpresaContratos()) {
                    empresaContratoDTO = new EmpresaContratoDTO();
                    empresaContratoDTO.setEmpresa(empresa);
                    empresaContratoDTO.setEmpresaContrato(empresaContrato);
                    listaEmpresaContrato.add(empresaContratoDTO);
                }

                if (empresa.getEmpresaContratos() == null || empresa.getEmpresaContratos().size() == 0) {
                    listaEmpresaContrato.add(empresaContratoDTO);
                }
            }
        }
        listaFiltro = getListaEmpresaContrato();
    }

    @Override
    /**
     * Limpa o filtro de pesquisa
     */
    public void limparFiltro() {
        super.limparFiltro();
        contextoController.limpar();
        contextoController.setObjectFilter(null);

    }

    /**
     * Salva
     * 
     * @throws AppException
     */
    public void salvar() throws AppException {

        if (!isCNPJvalido(getInstance())) {
            String focoCNPJ = "cadastrarForm:idCnpj";
            giveFocus(focoCNPJ);
        } else {
            super.save();
            listar();
        }
    }

    public Boolean isCNPJvalido(EmpresaVO entity) {

        if (!MascaraUtil.isCnpjValido(entity.getCnpj())) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI014", "CNPJ"));
            return false;
        }
        return true;
    }

    private void listar() {
        setInstanceFilter(null);
        reConsultar();

        // //REFAZ A CONSULTA
        obterLista();
    }

    public void pesquisarContrato() throws AppException {

        EmpresaContratoVO filter = new EmpresaContratoVO();
        filter.setBase(empresaContrato.getBase());
        filter.setEmpresa(empresaContrato.getEmpresa());
        filter.setNumeroContrato(empresaContrato.getNumeroContrato());

        List<EmpresaContratoVO> listaEmpresaContrato = empresaContratoService.find(filter);

        if (listaEmpresaContrato != null && listaEmpresaContrato.size() > 0) {
            empresaContrato = empresaContratoService.findById((Long) listaEmpresaContrato.get(0).getId());
        } else {
            empresaContrato.setDataFimVigencia(null);
            empresaContrato.setDataInicioVigencia(null);
        }

        desabilitarPesquisarAssociacao = true;
    }

    public void salvarContrato() throws AppException {
        try {
            if (!this.empresaContratoService.existeOutroContratoVigente(empresaContrato)) {
                empresaContratoService.saveOrUpdate(empresaContrato);
                listar();

                facesMessager.info(MensagemUtils.obterMensagem("MS024"));
                RequestContext.getCurrentInstance().update("formConsulta:messages");

                hideDialog("modalAssociar");
            } else {
                facesMessager.error(MensagemUtils.obterMensagem("MI035", empresaContrato.getBase().getNome()));
            }
        } catch (BusinessException e) {
            for (String message : e.getErroList())
                facesMessager.addMessageError(message);
        }

    }

    public void edita(EmpresaContratoDTO dto) throws AppException {
        EmpresaVO empresa = dto.getEmpresa();
        faseEditar = true;
        super.edita(empresa);

    }

    public void preExportar(Object document) {
        RelatorioSiredUtil relatorio = new RelatorioSiredUtil(NOME_RELATORIO, document);
        relatorio.preExportar();
    }

    public void limparPesquisa() {
        desabilitarPesquisarAssociacao = false;
        empresaContrato.setDataFimVigencia(null);
        empresaContrato.setDataInicioVigencia(null);

    }

    public void excluir() throws AppException {
        getInstanceExcluir().setEmpresaContratoExcluir(getEmpresaContratoExcluir());
        if (super.delete()) {
            // REFAZ A CONSULTA
            obterLista();
            // ATUALIZA A DATATABLE PARA EXIBIR O QUE FOI CADASTRADO
            updateComponentes(DATA_TABLE_CRUD);
        }
    }

    private void limparForm() {
        setInstance(new EmpresaVO());
    }

    public void novaAssociacao() {
        limparFormAssocicao();
    }

    public void editarContrato(EmpresaContratoDTO empresaContrato) {

        this.limparFormAssocicao();

        if (!Util.isNullOuVazio(empresaContrato.getEmpresa())) {
            this.empresaContrato.setEmpresa(empresaContrato.getEmpresa());
        }

        if (!Util.isNullOuVazio(empresaContrato.getEmpresaContrato())) {
            this.empresaContrato = this.empresaContratoService.findById((Long) empresaContrato.getEmpresaContrato().getId());
            this.desabilitarPesquisarAssociacao = true;
        }
    }

    private void limparFormAssocicao() {
        empresaContrato = new EmpresaContratoVO();
        listaBase = baseService.findAll();
        setListaEmpresa(empresaService.findAll());
        limparPesquisa();
    }

    public void novo() {
        limparForm();
        faseEditar = false;
    }

    @Override
    protected AbstractService<EmpresaVO> getService() {
        return empresaService;
    }

    public boolean isFaseEditar() {
        return faseEditar;
    }

    public void setFaseEditar(boolean faseEditar) {
        this.faseEditar = faseEditar;
    }

    public List<EmpresaContratoDTO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<EmpresaContratoDTO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public EmpresaService getEmpresaService() {
        return empresaService;
    }

    public void setEmpresaService(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    public EmpresaContratoService getEmpresaContratoService() {
        return empresaContratoService;
    }

    public void setEmpresaContratoService(EmpresaContratoService empresaContratoService) {
        this.empresaContratoService = empresaContratoService;
    }

    public List<EmpresaContratoDTO> getListaEmpresaContrato() {
        return listaEmpresaContrato;
    }

    public void setListaEmpresaContrato(List<EmpresaContratoDTO> listaEmpresaContrato) {
        this.listaEmpresaContrato = listaEmpresaContrato;
    }

    public EmpresaContratoVO getEmpresaContrato() {
        return empresaContrato;
    }

    public void setEmpresaContrato(EmpresaContratoVO empresaContrato) {
        this.empresaContrato = empresaContrato;
    }

    public List<BaseVO> getListaBase() {
        return listaBase;
    }

    public void setListaBase(List<BaseVO> listaBase) {
        this.listaBase = listaBase;
    }

    public BaseService getBaseService() {
        return baseService;
    }

    public void setBaseService(BaseService baseService) {
        this.baseService = baseService;
    }

    public List<EmpresaVO> getListaEmpresa() {
        return listaEmpresa;
    }

    public void setListaEmpresa(List<EmpresaVO> listaEmpresa) {
        this.listaEmpresa = listaEmpresa;
    }

    public EmpresaContratoVO getEmpresaContratoExcluir() {
        return empresaContratoExcluir;
    }

    public void setEmpresaContratoExcluir(EmpresaContratoVO empresaContratoExcluir) {
        this.empresaContratoExcluir = empresaContratoExcluir;
    }

}
