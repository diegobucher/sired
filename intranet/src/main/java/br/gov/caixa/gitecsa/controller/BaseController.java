package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.DualListModel;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.LoteSequenciaService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.LoteSequenciaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Named
@ViewScoped
public class BaseController extends BaseConsultaCRUD<BaseVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private BaseService baseService;

    @Inject
    private UnidadeService serviceUnidade;

    @Inject
    private LoteSequenciaService loteSequenciaService;

    private static String NOME_RELATORIO = "CADASTRO DE BASE";

    private boolean faseEditar;

    private List<BaseVO> listaFiltro;

    private Long codigoUnidadeFiltro;

    private DualListModel<LoteSequenciaVO> dualList;

    @Override
    protected BaseVO newInstance() {
        return new BaseVO();
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

    /**
     * Ao modificar o codigo o sistema deve limprar o filtro
     * 
     */
    public void modificarCodigoFiltro() {
        Long filtro = codigoUnidadeFiltro;
        limparForm();
        codigoUnidadeFiltro = filtro;
    }

    private void verificaFaseFormulario() {
        if (!Util.isNullOuVazio(getInstance().getId()))
            faseEditar = true;
        else
            faseEditar = false;
    }

    private void obterLista() {
        setLista(baseService.findAll());
        listaFiltro = getLista();
    }

    @Override
    /**
     * Limpa o filtro de pesquisa
     */
    public void limparFiltro() {
        super.limparFiltro();
        contextoController.limpar();
        contextoController.setObjectFilter(null);
        iniciarDualList();
    }

    /**
     * Salva o documento
     * 
     * @throws AppException
     */
    public void salvar() throws AppException {

        getInstance().setIcBaseCentralizadora(0);
        getInstance().setLoteSequencia(dualList.getTarget());

        super.save();
        setInstanceFilter(null);
        reConsultar();
        // //REFAZ A CONSULTA
        obterLista();

    }

    @Override
    public void edita(BaseVO vo) throws AppException {
        faseEditar = true;
        LoteSequenciaVO loteFilter = new LoteSequenciaVO();
        loteFilter.setBases(new ArrayList<BaseVO>());
        loteFilter.getBases().add(vo);
        List<LoteSequenciaVO> itensCadastrados = loteSequenciaService.findByParameters(loteFilter);
        List<LoteSequenciaVO> itensAssociados = loteSequenciaService.findAssociadoBase();
        List<LoteSequenciaVO> itens = getItensNaoAssociados(loteSequenciaService.findAll(), itensAssociados);

        dualList = new DualListModel<LoteSequenciaVO>(itens, itensCadastrados);

        setInstance(baseService.getById(vo.getId()));

        codigoUnidadeFiltro = (Long) vo.getUnidade().getId();
        desabilitarPesquisarAssociacao = true;

    }

    public void preExportar(Object document) {
        RelatorioSiredUtil relatorio = new RelatorioSiredUtil(NOME_RELATORIO, document);
        relatorio.preExportar();
    }

    public void excluir() throws AppException {
        if (super.delete()) {
            // REFAZ A CONSULTA
            obterLista();
            // ATUALIZA A DATATABLE PARA EXIBIR O QUE FOI CADASTRADO
            updateComponentes(DATA_TABLE_CRUD);
        }
    }

    private void limparForm() {
        setInstance(new BaseVO());
        desabilitarPesquisarAssociacao = false;
        codigoUnidadeFiltro = null;
        iniciarDualList();
    }

    private void iniciarDualList() {

        List<LoteSequenciaVO> target = new ArrayList<LoteSequenciaVO>();
        List<LoteSequenciaVO> itensAssociados = loteSequenciaService.findAssociadoBase();

        List<LoteSequenciaVO> loteSequencias = loteSequenciaService.findAll();
        List<LoteSequenciaVO> source = getItensNaoAssociados(loteSequencias, itensAssociados);

        dualList = new DualListModel<LoteSequenciaVO>(source, target);
    }

    private List<LoteSequenciaVO> getItensNaoAssociados(List<LoteSequenciaVO> itens, List<LoteSequenciaVO> itensAssociados) {
        List<LoteSequenciaVO> source = new ArrayList<LoteSequenciaVO>();
        boolean encontrado = false;
        for (LoteSequenciaVO obj : itens) {
            encontrado = false;
            for (LoteSequenciaVO item : itensAssociados) {
                if (obj.getId().equals(item.getId())) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                source.add(obj);
            }
        }
        return source;
    }

    public void novo() {
        limparForm();
        faseEditar = false;
    }

    /**
     * Carrega o dual list para associação
     * 
     * @throws AppException
     */
    public void pesquisaUnidade() throws AppException {
        Long codigo = codigoUnidadeFiltro;

        if (codigo != null) {
            UnidadeVO unidadeFiltro = new UnidadeVO();
            unidadeFiltro.setId(codigo);

            getUnidadeFiltro(codigo, unidadeFiltro);

        }

    }

    public void ordenarDualList() {

        Collections.sort(dualList.getTarget(), new Comparator<LoteSequenciaVO>() {

            @Override
            public int compare(LoteSequenciaVO o1, LoteSequenciaVO o2) {

                return ((String) o1.getId()).compareToIgnoreCase((String) o2.getId());
            }
        });

        Collections.sort(dualList.getSource(), new Comparator<LoteSequenciaVO>() {

            @Override
            public int compare(LoteSequenciaVO o1, LoteSequenciaVO o2) {

                return ((String) o1.getId()).compareToIgnoreCase((String) o2.getId());
            }
        });
    }

    private void getUnidadeFiltro(Long codigo, UnidadeVO unidadeFilto) {
        UnidadeVO unidade = serviceUnidade.getById(codigo);

        if (unidade == null) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
            getInstance().setUnidade(null);
            desabilitarPesquisarAssociacao = false;

        } else {

            if (unidade.getTipoUnidade().getIndicadorUnidade().equalsIgnoreCase(SiredUtils.ABREV_PONTO_VENDA)) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MI025"));
                setInstance(null);
                desabilitarPesquisarAssociacao = false;
                disableAssociacao = true;
                return;
            }

            getInstance().setUnidade(unidade);
            desabilitarPesquisarAssociacao = true;
            disableAssociacao = false;
        }
    }

    @Override
    protected AbstractService<BaseVO> getService() {
        return baseService;
    }

    public boolean isFaseEditar() {
        return faseEditar;
    }

    public void setFaseEditar(boolean faseEditar) {
        this.faseEditar = faseEditar;
    }

    public List<BaseVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<BaseVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public Long getCodigoUnidadeFiltro() {
        return codigoUnidadeFiltro;
    }

    public void setCodigoUnidadeFiltro(Long codigoUnidadeFiltro) {
        this.codigoUnidadeFiltro = codigoUnidadeFiltro;
    }

    public DualListModel<LoteSequenciaVO> getDualList() {
        return dualList;
    }

    public void setDualList(DualListModel<LoteSequenciaVO> dualList) {
        this.dualList = dualList;
    }

}
