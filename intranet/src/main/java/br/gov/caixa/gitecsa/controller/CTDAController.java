package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.DualListModel;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.CTDAService;
import br.gov.caixa.gitecsa.service.CTDAUFRestricaoService;
import br.gov.caixa.gitecsa.service.CTDAUFVOService;
import br.gov.caixa.gitecsa.service.MunicipioService;
import br.gov.caixa.gitecsa.service.UFService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.TipoRestricaoEnum;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.CTDAUFRestricaoVO;
import br.gov.caixa.gitecsa.sired.vo.CTDAUFVO;
import br.gov.caixa.gitecsa.sired.vo.CTDAVO;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Named
@ViewScoped
public class CTDAController extends BaseConsultaCRUD<CTDAVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private CTDAService service;

    @Inject
    private CTDAUFVOService ctdaUFVOService;

    @Inject
    private BaseService baseService;

    @Inject
    private MunicipioService municipioService;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private UFService ufService;

    @Inject
    private CTDAUFRestricaoService ctdaUFRestricaoService;

    private DualListModel<UFVO> dualList;

    private List<CTDAVO> lista;

    private static String NOME_RELATORIO = "RELATÓRIO DE CTDA";

    private boolean faseEditar;

    private List<CTDAVO> listaFiltro;
    private List<BaseVO> listaBase;
    private List<MunicipioVO> listaMunicipio;
    private List<UFVO> listaUFRestricao;
    private Long idUFRestricao;
    private UFVO UFRestricao;
    private String codigoUnidadeFiltro;
    private String nomeUnidadeFiltro;
    private TipoRestricaoEnum tipoRestricao;
    private TipoRestricaoEnum[] listaTiposRestricaoEnum;
    private List<CTDAUFRestricaoVO> listaCtdaUFRestricao;
    private CTDAUFRestricaoVO ctdaUFRestricaoExcluir;
    private List<CTDAUFRestricaoVO> listaResticoesInclusao;
    private List<CTDAUFRestricaoVO> listaResticoesExclusao;

    private HtmlSelectOneMenu selectBase = new HtmlSelectOneMenu();

    @Override
    protected CTDAVO newInstance() {
        return new CTDAVO();

    }

    @PostConstruct
    protected void init() throws AppException {
        try {
            obterLista();
            verificaFaseFormulario();
            novo();
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
        try {
            setLista(service.findAll());
            listaFiltro = getLista();
        } catch (Exception e) {
            return;
        }

    }

    /**
     * Limpa o filtro de pesquisa
     */
    @Override
    public void limparFiltro() {
        super.limparFiltro();
        contextoController.limpar();
        contextoController.setObjectFilter(null);

    }

    /**
     * Salvar o vo
     * 
     * @throws AppException
     */
    public void salvar() throws AppException {

        if (this.getInstance().getId() != null) {
            CTDAVO obj = null;

            try {
                obj = service.getById(getInstance().getId());
            } catch (Exception e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                return;
            }

            obj.getCtdaUF().clear();

            List<CTDAUFVO> lista = new ArrayList<CTDAUFVO>();
            for (UFVO ufVO : dualList.getTarget()) {

                CTDAUFVO cdtdaVO = new CTDAUFVO();
                cdtdaVO.setUf(ufVO);
                cdtdaVO.setCtda(obj);
                try {
                    lista = ctdaUFVOService.findByParameters(cdtdaVO);
                } catch (AppException e) {
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                    return;
                }

                if (lista.size() > 0) {
                    cdtdaVO = lista.get(0);
                    cdtdaVO.getCtdaUFRestricoes().clear();
                }

                for (CTDAUFRestricaoVO cUFR : listaCtdaUFRestricao) {

                    if (cUFR.getCtdaUF().getUf().getId().equals(ufVO.getId())) {
                        if (cdtdaVO.getCtdaUFRestricoes() == null) {
                            cdtdaVO.setCtdaUFRestricoes(new ArrayList<CTDAUFRestricaoVO>());
                        }
                        cUFR.setCtdaUF(cdtdaVO);
                        cdtdaVO.getCtdaUFRestricoes().add(cUFR);
                    }
                }
                obj.getCtdaUF().add(cdtdaVO);

            }

            setInstance(obj);
            if (!faseEditar)
                this.setMensagemPersonalizada("MS024");
            try {
                super.save();
            } catch (AppException e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                return;
            }

            desabilitarPesquisarAssociacao = false;
            disableAssociacao = false;
            this.setInstance(obj);

            setMensagemPersonalizada(null);

            setInstanceFilter(null);
            reConsultar();

            obterLista();

        }
    }

    public void iniciarDualList() {
        List<UFVO> target = new ArrayList<UFVO>();
        List<UFVO> source = getItensNaoAssociados(ufService.findAll(), ufService.findAssociadoCTDA());
        source.addAll(getUFRestricaoExclusao());
        dualList = new DualListModel<UFVO>(source, target);
        ordenarDualList();
    }

    private List<UFVO> getUFRestricaoExclusao() {
        Map<String, UFVO> listaPossiveisUF = new HashMap<String, UFVO>();
        // OBTER TODAS AS RETRICOES
        List<CTDAUFRestricaoVO> listaResticoes = new ArrayList<CTDAUFRestricaoVO>();
        listaResticoes = ctdaUFRestricaoService.findAllEager();
        // SEPARAR AS RETRICOES DE INCLUSAO
        listaResticoesInclusao = getListaResticaoByTipo(listaResticoes, TipoRestricaoEnum.INCLUIR);
        // SEPARAR RESTRICOES DE EXLUSAO
        listaResticoesExclusao = getListaResticaoByTipo(listaResticoes, TipoRestricaoEnum.EXCLUIR);
        // ADICIONAR A LISTA DE UFS POSSIVEIS INDICANDO QUE TEM RESTRICAO
        // verificar se a UF que esta na lista de Exclusao ja esta contida na
        // lista de Inclusao
        for (CTDAUFRestricaoVO ctdaUFRestricaoVO : listaResticoesExclusao) {
            boolean contem = listaContemObjeto(listaResticoesInclusao, ctdaUFRestricaoVO.getUnidade());
            if (!contem && !listaPossiveisUF.containsKey(ctdaUFRestricaoVO.getCtdaUF().getUf().getId())) {
                listaPossiveisUF.put((String) ctdaUFRestricaoVO.getCtdaUF().getUf().getId(), ctdaUFRestricaoVO.getCtdaUF().getUf());
            }
        }
        return (new ArrayList<UFVO>(listaPossiveisUF.values()));
    }

    private boolean listaContemObjeto(List<CTDAUFRestricaoVO> listaResticoesInclusao, UnidadeVO unidade) {
        for (CTDAUFRestricaoVO ctdaUFRestricaoVO : listaResticoesInclusao) {
            if (ctdaUFRestricaoVO.getUnidade().getId().equals(unidade.getId()))
                return true;
        }
        return false;
    }

    private List<CTDAUFRestricaoVO> getListaResticaoByTipo(List<CTDAUFRestricaoVO> listaResticoes, TipoRestricaoEnum tipoRestricao) {
        List<CTDAUFRestricaoVO> lista = new ArrayList<CTDAUFRestricaoVO>();
        for (CTDAUFRestricaoVO ctdaUFRestricaoVO : listaResticoes) {
            if (ctdaUFRestricaoVO.getTipoResticao().equals(tipoRestricao)) {
                lista.add(ctdaUFRestricaoVO);
            }
        }
        return lista;
    }

    private List<UFVO> getItensNaoAssociados(List<UFVO> documentos, List<UFVO> itemAssociadoSomeGroup) {
        List<UFVO> source = new ArrayList<UFVO>();
        boolean encontrado = false;
        for (UFVO item : documentos) {
            encontrado = false;
            for (UFVO itemAssiciado : itemAssociadoSomeGroup) {
                if (item.getId().equals(itemAssiciado.getId())) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                source.add(item);
            }
        }
        return source;
    }

    @Override
    public void edita(CTDAVO vo) throws AppException {
        faseEditar = true;
        limparForm();

        preencheListaMunicpios(vo.getBase());
        // super.edita(vo);
        // setInstance(service.buscarPorCTDAVO(vo));
        setInstance(vo);

        // cria a duallist
        CTDAUFVO filterCTDAUF = new CTDAUFVO();
        filterCTDAUF.setCtda(vo);
        List<CTDAUFVO> listaCTDAUF = ctdaUFVOService.buscarPorCTDAUFVO(filterCTDAUF);
        List<UFVO> target = mountListaUF(listaCTDAUF);
        List<UFVO> source = getItensNaoAssociados(ufService.findAll(), ufService.findAssociadoCTDA());
        source.addAll(getUFRestricaoExclusao());
        dualList = new DualListModel<UFVO>(source, target);
        ordenarDualList();

        // Lista de Municipios para uf
        listaUFRestricao = target;

        // preenche as restricoes
        CTDAUFRestricaoVO filterRestricao = new CTDAUFRestricaoVO();
        filterRestricao.setCtdaUF(filterCTDAUF);
        listaCtdaUFRestricao = ctdaUFRestricaoService.buscarPorCTDAUFRestricao(filterRestricao);
    }

    private List<UFVO> mountListaUF(List<CTDAUFVO> listaCTDAUF) {
        List<UFVO> ufs = new ArrayList<UFVO>();
        for (CTDAUFVO ctdaUFVO : listaCTDAUF) {
            ufs.add(ctdaUFVO.getUf());
        }
        return ufs;
    }

    public void preExportar(Object document) {
        RelatorioSiredUtil relatorio = new RelatorioSiredUtil(NOME_RELATORIO, document);
        relatorio.preExportar();
    }

    public void excluir() throws AppException {
        service.delete(getInstanceExcluir());
        obterLista();
        updateComponentes(DATA_TABLE_CRUD);
    }

    public void excluirRestricao() {
        listaCtdaUFRestricao.remove(ctdaUFRestricaoExcluir);
        facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS016"));
        // ATUALIZA A DATATABLE PARA EXIBIR O QUE FOI CADASTRADO
        updateComponentes("dataTableCrudRestricao");
    }

    private void limparForm() {
        setInstance(new CTDAVO());
        iniciarDualList();
        listaMunicipio = new ArrayList<MunicipioVO>();
        listaUFRestricao = new ArrayList<UFVO>();
        listaTiposRestricaoEnum = TipoRestricaoEnum.values();
        listaCtdaUFRestricao = new ArrayList<CTDAUFRestricaoVO>();
        limparFormRestricao();
    }

    private void limparFormRestricao() {
        idUFRestricao = null;
        nomeUnidadeFiltro = null;
        codigoUnidadeFiltro = null;
        tipoRestricao = null;
        UFRestricao = null;
    }

    public void novo() {
        limparForm();
        faseEditar = false;
        setListaBase(baseService.findAll());

    }

    public void preencheListaMunicpios() {
        BaseVO baseSelecionada = (BaseVO) selectBase.getValue();
        getInstance().setMunicipio(null);
        listaMunicipio = new ArrayList<MunicipioVO>();

        if (baseSelecionada != null)
            preencheListaMunicpios(baseSelecionada);
    }

    public void preencheListaMunicpios(BaseVO baseSelecionada) {

        if (!Util.isNullOuVazio(baseSelecionada)) {
            MunicipioVO filter = new MunicipioVO();
            // carregando todos os relacionamentos/referências da Unidade.
            UnidadeVO unidade = unidadeService.getById(baseSelecionada.getUnidade().getId());
            filter.setUf(unidade.getUf());
            try {
                listaMunicipio = municipioService.findByParameters(filter);
                selectBase.resetValue();
            } catch (AppException e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "preencheListaMunicpios", "findByParameters"));
            }
        } else {
            listaMunicipio = new ArrayList<MunicipioVO>();
            getInstance().setMunicipio(null);
        }

    }

    public void tranferirItem() {

        ordenarDualList();
        listaUFRestricao = dualList.getTarget();
    }

    public void incluirRestricao() throws AppException {
        boolean isObrigatorioNaoInformado = false;
        if (isNullOuVazio(UFRestricao)) {
            facesMessager.addMessageError(getRequiredMessage("ctda.label.uf"));
            giveFocus("selectUFRestricao");
            isObrigatorioNaoInformado = true;
        }
        if (isNullOuVazio(codigoUnidadeFiltro)) {
            facesMessager.addMessageError(getRequiredMessage("geral.label.codigoUnidade"));
            giveFocus("codigoUnidadeFiltro");
            isObrigatorioNaoInformado = true;
        }
        if (isNullOuVazio(tipoRestricao)) {
            facesMessager.addMessageError(getRequiredMessage("ctda.label.acao"));
            giveFocus("selectTipoResticao");
            isObrigatorioNaoInformado = true;
        }
        if (!isObrigatorioNaoInformado) {
            // pesquisar unidade, se unidade existir
            UnidadeVO unidade = pesquisaUnidade();
            if (unidade != null) {
                if (!restricaoJaExistente()) {
                    if (isInclusaoRestricaoPermitida(unidade, UFRestricao, tipoRestricao)) {
                        addRestricao(unidade);
                        limparFormRestricao();
                        facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS013"));
                        return;
                    } else {
                        facesMessager.addMessageError(MensagemUtils.obterMensagem("ctda.mensagem.inclusaoNaoPermitida"));

                    }
                }
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA031"));
            }
        }
    }

    public UnidadeVO pesquisaUnidade() {
        UnidadeVO unidade = null;
        if (codigoUnidadeFiltro != null && UFRestricao != null) {
            try {
                codigoUnidadeFiltro = codigoUnidadeFiltro.trim();
                UnidadeVO filtro = new UnidadeVO();
                filtro.setId(Long.parseLong(codigoUnidadeFiltro));
                filtro.setUf(UFRestricao);
                List<UnidadeVO> unidades = unidadeService.findByParameters(filtro);
                unidade = unidades.size() > 0 ? unidades.get(0) : null;

            } catch (Exception e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "Unidade", "getById"));
            }
            if (unidade == null) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
                return null;
            } else {
                nomeUnidadeFiltro = unidade.getNome();
            }
        }
        return unidade;
    }

    /**
     * Verificar se undiade não possui nenhum restrição de inclusao criada para ela no banco atualmente
     * 
     * @param undiade
     * @param uf
     * @return Boolean
     * @throws AppException
     */
    private Boolean isInclusaoRestricaoPermitida(UnidadeVO unidade, UFVO uf, TipoRestricaoEnum tipoRestricao) {
        try {
            // VERIFICA SE A UF JA POSSUI ALGUMA CTDA
            CTDAUFVO filterCtdaUF = new CTDAUFVO();
            filterCtdaUF.setUf(uf);
            List<CTDAUFVO> listaCtdaUF = ctdaUFVOService.findByParameters(filterCtdaUF);

            // SE JA EXISTE ALGUMA UF, VERIFICO SE EXISTE ALGUMA RESTRICAO
            if (listaCtdaUF.size() > 0) {

                CTDAUFRestricaoVO filter = new CTDAUFRestricaoVO();
                filter.setUnidade(unidade);
                CTDAUFVO ctdaUF = new CTDAUFVO();
                ctdaUF.setUf(uf);
                filter.setCtdaUF(ctdaUF);

                List<CTDAUFRestricaoVO> lista = new ArrayList<CTDAUFRestricaoVO>();
                lista = ctdaUFRestricaoService.findByParameters(filter);

                boolean restricaoIgual = false;

                for (CTDAUFRestricaoVO ctdaUFRestricaoVO : lista) {
                    if (ctdaUFRestricaoVO.getTipoResticao().equals(tipoRestricao)) {
                        restricaoIgual = true;
                    } else {
                        restricaoIgual = false;
                    }
                }

                return (!restricaoIgual);

            } else { // SE NÃO EXISTIR NENHUMA RESTRICAO
                return true;
            }

        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "isRestricaoPermitida", "CtdaUFRestricao"));
        }
        return Boolean.FALSE;

    }

    private void addRestricao(UnidadeVO unidade) {
        if (listaCtdaUFRestricao == null)
            listaCtdaUFRestricao = new ArrayList<CTDAUFRestricaoVO>();

        CTDAUFRestricaoVO restricao = new CTDAUFRestricaoVO();
        CTDAUFVO ctdaUF = new CTDAUFVO();
        ctdaUF.setCtda(getInstance());
        ctdaUF.setUf(UFRestricao);
        restricao.setCtdaUF(ctdaUF);
        restricao.setTipoResticao(tipoRestricao);
        restricao.setUnidade(unidade);
        listaCtdaUFRestricao.add(restricao);
    }

    private boolean restricaoJaExistente() {

        for (CTDAUFRestricaoVO item : listaCtdaUFRestricao) {
            if (item.getUnidade().getId().toString().equalsIgnoreCase(codigoUnidadeFiltro) && item.getCtdaUF().getUf().getId().equals(UFRestricao.getId()))
                return true;
        }
        return false;
    }

    public void modificarCodigoPesquisaUnidade() {
        String filtro = codigoUnidadeFiltro;
        desabilitarPesquisarAssociacao = false;
        disableAssociacao = true;
        codigoUnidadeFiltro = filtro;
        nomeUnidadeFiltro = null;
    }

    public void ordenarDualList() {

        Collections.sort(dualList.getTarget(), new Comparator<UFVO>() {
            @Override
            public int compare(UFVO o1, UFVO o2) {
                return o1.getNome().compareToIgnoreCase(o2.getNome());
            }
        });

        Collections.sort(dualList.getSource(), new Comparator<UFVO>() {
            @Override
            public int compare(UFVO o1, UFVO o2) {
                return o1.getNome().compareToIgnoreCase(o2.getNome());
            }
        });
    }

    @Override
    protected AbstractService<CTDAVO> getService() {
        return service;
    }

    public boolean isFaseEditar() {
        return faseEditar;
    }

    public void setFaseEditar(boolean faseEditar) {
        this.faseEditar = faseEditar;
    }

    public DualListModel<UFVO> getDualList() {
        return dualList;
    }

    public void setDualList(DualListModel<UFVO> dualList) {
        this.dualList = dualList;
    }

    public List<CTDAVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<CTDAVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public List<CTDAVO> getLista() {
        return lista;
    }

    public void setLista(List<CTDAVO> lista) {
        this.lista = lista;
    }

    public List<BaseVO> getListaBase() {
        return listaBase;
    }

    public void setListaBase(List<BaseVO> listaBase) {
        this.listaBase = listaBase;
    }

    public List<MunicipioVO> getListaMunicipio() {
        return listaMunicipio;
    }

    public void setListaMunicipio(List<MunicipioVO> listaMunicipio) {
        this.listaMunicipio = listaMunicipio;
    }

    public HtmlSelectOneMenu getSelectBase() {
        return selectBase;
    }

    public void setSelectBase(HtmlSelectOneMenu selectBase) {
        this.selectBase = selectBase;
    }

    public List<UFVO> getListaUFRestricao() {
        return listaUFRestricao;
    }

    public void setListaUFRestricao(List<UFVO> listaUFRestricao) {
        this.listaUFRestricao = listaUFRestricao;
    }

    public Long getIdUFRestricao() {
        return idUFRestricao;
    }

    public void setIdUFRestricao(Long idUFRestricao) {
        this.idUFRestricao = idUFRestricao;
    }

    public String getCodigoUnidadeFiltro() {
        return codigoUnidadeFiltro;
    }

    public void setCodigoUnidadeFiltro(String codigoUnidadeFiltro) {
        this.codigoUnidadeFiltro = codigoUnidadeFiltro;
    }

    public String getNomeUnidadeFiltro() {
        return nomeUnidadeFiltro;
    }

    public void setNomeUnidadeFiltro(String nomeUnidadeFiltro) {
        this.nomeUnidadeFiltro = nomeUnidadeFiltro;
    }

    public TipoRestricaoEnum getTipoRestricao() {
        return tipoRestricao;
    }

    public void setTipoRestricao(TipoRestricaoEnum tipoRestricao) {
        this.tipoRestricao = tipoRestricao;
    }

    public TipoRestricaoEnum[] getListaTiposRestricaoEnum() {
        return listaTiposRestricaoEnum;
    }

    public void setListaTiposRestricaoEnum(TipoRestricaoEnum[] listaTiposRestricaoEnum) {
        this.listaTiposRestricaoEnum = listaTiposRestricaoEnum;
    }

    public List<CTDAUFRestricaoVO> getListaCtdaUFRestricao() {
        return listaCtdaUFRestricao;
    }

    public void setListaCtdaUFRestricao(List<CTDAUFRestricaoVO> listaCtdaUFRestricao) {
        this.listaCtdaUFRestricao = listaCtdaUFRestricao;
    }

    public UFVO getUFRestricao() {
        return UFRestricao;
    }

    public CTDAUFRestricaoVO getCtdaUFRestricaoExcluir() {
        return ctdaUFRestricaoExcluir;
    }

    public void setCtdaUFRestricaoExcluir(CTDAUFRestricaoVO ctdaUFRestricaoExcluir) {
        this.ctdaUFRestricaoExcluir = ctdaUFRestricaoExcluir;
    }

    public void setUFRestricao(UFVO uFRestricao) {
        UFRestricao = uFRestricao;
    }

}
