package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.DualListModel;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.service.GrupoService;
import br.gov.caixa.gitecsa.service.OperacaoService;
import br.gov.caixa.gitecsa.service.UFService;
import br.gov.caixa.gitecsa.service.UnidadeGrupoOperacaoService;
import br.gov.caixa.gitecsa.service.UnidadeGrupoService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.util.JavaScriptUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.OperacaoVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeGrupoOperacaoPK;
import br.gov.caixa.gitecsa.sired.vo.UnidadeGrupoOperacaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeGrupoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Named
@ViewScoped
public class AreaMeioController extends BaseConsultaCRUD<UnidadeVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private UnidadeService serviceUnidade;
    @Inject
    private UFService UFService;
    @Inject
    private OperacaoService operacaoService;
    @Inject
    private GrupoService grupoService;
    @Inject
    private UnidadeGrupoOperacaoService unidadeGrupoOperacaoService;
    @Inject
    private UnidadeGrupoService unidadeGrupoService;

    private DualListModel<UFVO> dualList;

    private DualListModel<GrupoVO> dualListGrupo;

    private List<UnidadeVO> lista;

    private static String NOME_RELATORIO = "RELATÓRIO DE ÁREA MEIO";

    private boolean faseEditar;

    private List<UnidadeVO> listaFiltro;

    private String codigoFiltro;

    private Long idGrupoRestricao;
    private String idOperacao;

    private List<GrupoVO> listaGruposRestricao;
    private List<OperacaoVO> listaOperacoes;

    private List<UnidadeGrupoOperacaoVO> listaUnidadeGrupoOperacao;

    private UnidadeGrupoOperacaoVO unidadeGrupoOperacaoExcluir;

    private UnidadeGrupoOperacaoVO unidadeGrupoOpExcluir;
    private boolean isGravacaoAlteracaoFinal;
    
    private UnidadeVO unidadeAreaMeio = new UnidadeVO();
    
    private Boolean consultaHabilitada;

    @Override
    protected UnidadeVO newInstance() {
        return new UnidadeVO();

    }

    @PostConstruct
    protected void init() throws AppException {
        try {
            this.listaUnidadeGrupoOperacao = new ArrayList<UnidadeGrupoOperacaoVO>();
            this.consultaHabilitada = false;
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

    /**
     * Carrega a tela em branco
     * 
     * @throws AppException
     */
    public void loadTelaAssociada() {
        codigoFiltro = null;
        setInstance(newInstance());

        // LISTA DE UF
        dualList = new DualListModel<UFVO>((new ArrayList<UFVO>()), (new ArrayList<UFVO>()));

        // LISTA DE GRUPO
        dualListGrupo = new DualListModel<GrupoVO>((new ArrayList<GrupoVO>()), (new ArrayList<GrupoVO>()));
        listaGruposRestricao = new ArrayList<GrupoVO>();
        listaOperacoes = new ArrayList<OperacaoVO>();
        listaUnidadeGrupoOperacao = new ArrayList<UnidadeGrupoOperacaoVO>();
        desabilitarPesquisarAssociacao = false;
        disableAssociacao = true;
    }

    /**
     * Limpa o filtro de pesquisa
     */
    @Override
    public void limparFiltro() {
        super.limparFiltro();
        contextoController.limpar();
        contextoController.setObjectFilter(null);
        codigoFiltro = null;
        idGrupoRestricao = null;
    }

    public void modificarCodigoPesquisaAssociacao() {
        String filtro = codigoFiltro;
        loadTelaAssociada();
        codigoFiltro = filtro;
        getInstance().setNome(null);
    }
    
    public void pesquisarUnidadeAreaMeio() {
        try {
            this.consultaHabilitada = false;
            if (!ObjectUtils.isNullOrEmpty((this.unidadeAreaMeio)) && !ObjectUtils.isNullOrEmpty(this.unidadeAreaMeio.getId())) {
                UnidadeVO unidade = this.serviceUnidade.getById(this.unidadeAreaMeio.getId());
                if (ObjectUtils.isNullOrEmpty(unidade)) {
                    throw new BusinessException(MensagemUtils.obterMensagem("MA020"));
                }
                this.unidadeAreaMeio = unidade;
                this.consultaHabilitada = true;
            } else {
                this.unidadeAreaMeio = new UnidadeVO();
            }
            JavaScriptUtils.update("formConsulta:pnlBotoes");
        } catch (BusinessException e) {
            facesMessager.addMessageError(e.getMessage());
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
        }
    }
    
    public void localizar() {
        
        if (!ObjectUtils.isNullOrEmpty(this.unidadeAreaMeio) && !ObjectUtils.isNullOrEmpty(this.unidadeAreaMeio.getId())) {
            List<UnidadeVO> unidades = serviceUnidade.findById((Long)this.unidadeAreaMeio.getId());
            setLista(unidades);
            listaFiltro = getLista();
            if (ObjectUtils.isNullOrEmpty(unidades) || unidades.isEmpty()) {
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MI017"));
            }
        } 
    }
    
    public void localizarTodas() {
    	unidadeAreaMeio = new UnidadeVO();
    	JavaScriptUtils.update("formConsulta:cgcUnidade","formConsulta:nomeUnidade");
    	
        setLista(serviceUnidade.findAll());
        listaFiltro = getLista();
    }
    
    public Boolean isConsultaHabilitada() {
        return this.consultaHabilitada;
    }

    /**
     * Salva
     * 
     * @throws AppException
     */
    public void salvar() throws AppException {

        if (this.getInstance().getId() != null) {
            UnidadeVO obj = null;

            try {
                obj = serviceUnidade.getById(getInstance().getId());
            } catch (Exception e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                return;
            }

            obj.setUfs(new HashSet<UFVO>(dualList.getTarget()));

            // GRUPO E RESTRICAO
            obj.getUnidadeGrupos().clear();
            List<UnidadeGrupoVO> listaUnidadeGrupos = new ArrayList<UnidadeGrupoVO>();
            for (GrupoVO grupo : dualListGrupo.getTarget()) {

                UnidadeGrupoVO unidadeGrupo = new UnidadeGrupoVO();
                unidadeGrupo.setGrupo(grupo);
                unidadeGrupo.setUnidade(obj);
                try {
                    listaUnidadeGrupos = unidadeGrupoService.findByParameters(unidadeGrupo);
                } catch (AppException e) {
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                    return;
                }

                if (listaUnidadeGrupos.size() > 0) {
                    unidadeGrupo = listaUnidadeGrupos.get(0);
                    unidadeGrupo.setUnidade(obj);
                    unidadeGrupo.getOperacao().clear();
                }

                for (UnidadeGrupoOperacaoVO uniGrupOPVo : listaUnidadeGrupoOperacao) {

                    if (uniGrupOPVo.getUnidadeGrupo().getGrupo().getId().equals(grupo.getId())) {
                        if (unidadeGrupo.getOperacao() == null) {
                            unidadeGrupo.setOperacao(new HashSet<OperacaoVO>());
                        }
                        OperacaoVO opVO = new OperacaoVO();
                        opVO.setId(uniGrupOPVo.getOperacao().getId());
                        unidadeGrupo.getOperacao().add(opVO);
                    }
                }
                
                obj.getUnidadeGrupos().add(unidadeGrupo);
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
            codigoFiltro = obj.getId().toString();

            setMensagemPersonalizada(null);

            setInstanceFilter(null);
            reConsultar();

            pesquisaAssociacao();
            isGravacaoAlteracaoFinal = true;
        }

    }

    public void incluirRestricaoGrupoDocumento() throws AppException {
        isGravacaoAlteracaoFinal = false;

        UnidadeVO obj = this.getInstance();

        if (idGrupoRestricao == null) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI014", "geral.label.grupoDocumentos"));
        }

        if (idOperacao == null) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI014", "areameio.label.apenasOperacao"));
        }

        if (obj.getId() != null && idGrupoRestricao != null && idOperacao != null) {
            if (restricaoJaExistente(idGrupoRestricao, idOperacao)) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA032"));
                return;
            } else {
                GrupoVO grupoVO = grupoService.getById(Long.parseLong(idGrupoRestricao.toString()));
                OperacaoVO operacaoVO = operacaoService.getById(idOperacao.toString());

                UnidadeGrupoVO unidadeGrupo = new UnidadeGrupoVO();
                unidadeGrupo.setUnidade(obj);
                unidadeGrupo.setGrupo(grupoVO);

                UnidadeGrupoOperacaoVO unidadeGrupoOperacao = new UnidadeGrupoOperacaoVO();
                unidadeGrupoOperacao.setUnidadeGrupo(unidadeGrupo);
                unidadeGrupoOperacao.setOperacao(operacaoVO);

                listaUnidadeGrupoOperacao.add(unidadeGrupoOperacao);

                idGrupoRestricao = null;
                idOperacao = null;

                desabilitarPesquisarAssociacao = true;
                disableAssociacao = false;

                facesMessager.info(MensagemUtils.obterMensagem("MS013"));
            }
        }
    }

    private boolean restricaoJaExistente(Long idGrupoRestricao, String idOperacao) {
        for (UnidadeGrupoOperacaoVO uniGrupoOp : listaUnidadeGrupoOperacao) {
            if (uniGrupoOp.getOperacao().getId().toString().equalsIgnoreCase(idOperacao)
                    && uniGrupoOp.getUnidadeGrupo().getGrupo().getId().equals(idGrupoRestricao)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Carrega o dual list para associação
     * 
     * @throws AppException
     */
    public void pesquisaAssociacao() throws AppException {
        if (codigoFiltro == null) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MI014", "areameio.label.codigoAreaMeio"));
            return;
        }

        Long codigo = Long.parseLong(codigoFiltro);
        listaUnidadeGrupoOperacao = new ArrayList<UnidadeGrupoOperacaoVO>();
        List<UFVO> sourceUF = new ArrayList<UFVO>();
        List<UFVO> targetUF = new ArrayList<UFVO>();
        List<GrupoVO> sourceGrupo = new ArrayList<GrupoVO>();
        List<GrupoVO> targetGrupo = new ArrayList<GrupoVO>();

        if (codigo != null) {

            UnidadeVO unidadeFiltro = new UnidadeVO();
            unidadeFiltro.setId(codigo);

            UFVO filter = new UFVO();
            filter.setUnidades(new ArrayList<UnidadeVO>());
            filter.getUnidades().add(unidadeFiltro);

            UnidadeGrupoVO unidadeGrupoFiltro = new UnidadeGrupoVO();
            unidadeGrupoFiltro.setUnidade(unidadeFiltro);

            getUnidadeFiltro(codigo, unidadeFiltro);

            if (getInstance().getId() != null) {

                UnidadeVO unidadeByID = serviceUnidade.getById(getInstance().getId());
                unidadeByID.getUfs().toArray();
                unidadeByID.getUnidadeGrupos().toArray();

                // UF
                // obter as UF cadastrados para a Unidade
                List<UFVO> itensCadastrados = new ArrayList<UFVO>(unidadeByID.getUfs());
                List<UFVO> todosItens = UFService.findAll();
                sourceUF = getNaoAssociados(todosItens, itensCadastrados);
                targetUF = itensCadastrados;

                // GRUPO
                List<GrupoVO> itensCadastradosGrupo = new ArrayList<GrupoVO>();

                for (UnidadeGrupoVO unidadeGrupoVo : unidadeByID.getUnidadeGrupos()) {
                    itensCadastradosGrupo.add(unidadeGrupoVo.getGrupo());
                }

                List<GrupoVO> todosItensGrupo = grupoService.findAll();
                sourceGrupo = getGrupoNaoAssociados(todosItensGrupo, itensCadastradosGrupo);
                targetGrupo = itensCadastradosGrupo;
                // RESTRIÇÃO
                listaGruposRestricao = itensCadastradosGrupo;
                listaOperacoes = operacaoService.findAll();

                // GRUPO RESTRICOES
                UnidadeGrupoOperacaoVO filterUnidadeOpVO = new UnidadeGrupoOperacaoVO();
                filterUnidadeOpVO.setUnidadeGrupo(unidadeGrupoFiltro);

                listaUnidadeGrupoOperacao = mount(unidadeGrupoOperacaoService.findEager(filterUnidadeOpVO));

            }
        }

        dualList = new DualListModel<UFVO>(sourceUF, targetUF);
        dualListGrupo = new DualListModel<GrupoVO>(sourceGrupo, targetGrupo);
        ordenarDualList();
        ordenarDualListGrupo();

        desabilitarPesquisarAssociacao = false;
    }

    private List<UnidadeGrupoOperacaoVO> mount(List<UnidadeGrupoOperacaoVO> list) {
        Map<UnidadeGrupoOperacaoPK, UnidadeGrupoOperacaoVO> map = new HashMap<UnidadeGrupoOperacaoPK, UnidadeGrupoOperacaoVO>();
        for (UnidadeGrupoOperacaoVO obj : list) {

            if (!map.containsKey(obj.getId())) {
                map.put((UnidadeGrupoOperacaoPK) obj.getId(), obj);
            }
        }

        return (new ArrayList<UnidadeGrupoOperacaoVO>(map.values()));

    }

    private void getUnidadeFiltro(Long codigo, UnidadeVO unidadeFilto) {
        UnidadeVO unidade = serviceUnidade.getById(codigo);

        if (unidade == null) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
            setInstance(null);
            desabilitarPesquisarAssociacao = false;
            disableAssociacao = true;
        } else {

            // SO DEVE SER MOSTRADA O QUE FOR AREA MEIO, OU SEJA, DIFERENTE DE
            // PV
            if (unidade.getTipoUnidade().getIndicadorUnidade().equalsIgnoreCase(SiredUtils.ABREV_PONTO_VENDA)) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MI025"));
                setInstance(null);
                desabilitarPesquisarAssociacao = false;
                disableAssociacao = true;
                return;
            }

            setInstance(unidade);
            desabilitarPesquisarAssociacao = true;
            disableAssociacao = false;
        }
    }

    public void ordenaDualListaRestricoes() {
        listaGruposRestricao = dualListGrupo.getTarget();
        ordenarDualListGrupo();
    }

    private List<UFVO> getNaoAssociados(List<UFVO> itens, List<UFVO> itensCadastrados) {
        List<UFVO> source = new ArrayList<UFVO>();
        boolean encontrado = false;
        for (UFVO doc : itens) {
            encontrado = false;
            for (UFVO grupoDoc : itensCadastrados) {
                if (doc.getId().equals(grupoDoc.getId())) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                source.add(doc);
            }
        }
        return source;
    }

    private List<GrupoVO> getGrupoNaoAssociados(List<GrupoVO> itens, List<GrupoVO> itensCadastrados) {
        List<GrupoVO> source = new ArrayList<GrupoVO>();
        boolean encontrado = false;
        for (GrupoVO doc : itens) {
            encontrado = false;
            for (GrupoVO grupoDoc : itensCadastrados) {
                if (doc.getId().equals(grupoDoc.getId())) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                source.add(doc);
            }
        }
        return source;
    }

    @Override
    public void edita(UnidadeVO vo) throws AppException {

        setInstance(serviceUnidade.getById(vo.getId()));
        faseEditar = true;

        codigoFiltro = vo.getId().toString();
        pesquisaAssociacao();
        desabilitarPesquisarAssociacao = true;
        disableAssociacao = false;
        isGravacaoAlteracaoFinal = false;

    }

    public void preExportar(Object document) {
        RelatorioSiredUtil relatorio = new RelatorioSiredUtil(NOME_RELATORIO, document);
        relatorio.preExportar();
    }

    /**
     * Excluir Restricao
     * 
     * @throws AppException
     */
    public void excluirRestricao() {

        List<UnidadeGrupoOperacaoVO> listaUnidadeGrupoOperacaoAux = new ArrayList<UnidadeGrupoOperacaoVO>();
        listaUnidadeGrupoOperacaoAux.addAll(listaUnidadeGrupoOperacao);

        for (UnidadeGrupoOperacaoVO uniGruOP : listaUnidadeGrupoOperacaoAux) {
            if (uniGruOP.getOperacao().getId().equals(unidadeGrupoOperacaoExcluir.getOperacao().getId())
                    && uniGruOP.getUnidadeGrupo().getGrupo().getId().equals(unidadeGrupoOperacaoExcluir.getUnidadeGrupo().getGrupo().getId())
                    && uniGruOP.getUnidadeGrupo().getUnidade().getId().equals(unidadeGrupoOperacaoExcluir.getUnidadeGrupo().getUnidade().getId())) {
                listaUnidadeGrupoOperacao.remove(uniGruOP);
                facesMessager.info(MensagemUtils.obterMensagem("MS016"));
                break;
            }
        }

        isGravacaoAlteracaoFinal = false;
        desabilitarPesquisarAssociacao = true;

    }

    private void limparForm() {
        setInstance(new UnidadeVO());
        codigoFiltro = null;
    }

    public void novo() {
        limparForm();
        loadTelaAssociada();
        faseEditar = false;
    }

    public void alterar() throws AppException {
        faseEditar = true;
        // o salvar chama o novo que muda o status do formulario
        salvar();

        faseEditar = true;

        isGravacaoAlteracaoFinal = true;

    }

    public void ordenarDualList() {

        desabilitarPesquisarAssociacao = true;

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

    private void ordenarDualListGrupo() {
        desabilitarPesquisarAssociacao = true;

        Collections.sort(dualListGrupo.getTarget(), new Comparator<GrupoVO>() {

            @Override
            public int compare(GrupoVO o1, GrupoVO o2) {

                return o1.getNome().compareToIgnoreCase(o2.getNome());
            }
        });

        Collections.sort(dualListGrupo.getSource(), new Comparator<GrupoVO>() {

            @Override
            public int compare(GrupoVO o1, GrupoVO o2) {

                return o1.getNome().compareToIgnoreCase(o2.getNome());
            }
        });
    }

    @Override
    protected AbstractService<UnidadeVO> getService() {
        return serviceUnidade;
    }

    public UnidadeService getServiceUnidade() {
        return serviceUnidade;
    }

    public void setServiceUnidade(UnidadeService serviceUnidade) {
        this.serviceUnidade = serviceUnidade;
    }

    public UFService getUFService() {
        return UFService;
    }

    public void setUFService(UFService uFService) {
        UFService = uFService;
    }

    public OperacaoService getOperacaoService() {
        return operacaoService;
    }

    public void setOperacaoService(OperacaoService operacaoService) {
        this.operacaoService = operacaoService;
    }

    public GrupoService getGrupoService() {
        return grupoService;
    }

    public void setGrupoService(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    public UnidadeGrupoService getUnidadeGrupoService() {
        return unidadeGrupoService;
    }

    public void setUnidadeGrupoService(UnidadeGrupoService unidadeGrupoService) {
        this.unidadeGrupoService = unidadeGrupoService;
    }

    public DualListModel<UFVO> getDualList() {
        return dualList;
    }

    public void setDualList(DualListModel<UFVO> dualList) {
        this.dualList = dualList;
    }

    public DualListModel<GrupoVO> getDualListGrupo() {
        return dualListGrupo;
    }

    public void setDualListGrupo(DualListModel<GrupoVO> dualListGrupo) {
        this.dualListGrupo = dualListGrupo;
    }

    public List<UnidadeVO> getLista() {
        return lista;
    }

    public void setLista(List<UnidadeVO> lista) {
        this.lista = lista;
    }

    public boolean isFaseEditar() {
        return faseEditar;
    }

    public void setFaseEditar(boolean faseEditar) {
        this.faseEditar = faseEditar;
    }

    public List<UnidadeVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<UnidadeVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public String getCodigoFiltro() {
        return codigoFiltro;
    }

    public void setCodigoFiltro(String codigoFiltro) {
        this.codigoFiltro = codigoFiltro;
    }

    public Long getIdGrupoRestricao() {
        return idGrupoRestricao;
    }

    public void setIdGrupoRestricao(Long idGrupoRestricao) {
        this.idGrupoRestricao = idGrupoRestricao;
    }

    public List<GrupoVO> getListaGruposRestricao() {
        return listaGruposRestricao;
    }

    public void setListaGruposRestricao(List<GrupoVO> listaGruposRestricao) {
        this.listaGruposRestricao = listaGruposRestricao;
    }

    public List<OperacaoVO> getListaOperacoes() {
        return listaOperacoes;
    }

    public String getIdOperacao() {
        return idOperacao;
    }

    public void setIdOperacao(String idOperacao) {
        this.idOperacao = idOperacao;
    }

    public void setListaOperacoes(List<OperacaoVO> listaOperacoes) {
        this.listaOperacoes = listaOperacoes;
    }

    public List<UnidadeGrupoOperacaoVO> getListaUnidadeGrupoOperacao() {
        return listaUnidadeGrupoOperacao;
    }

    public void setListaUnidadeGrupoOperacao(List<UnidadeGrupoOperacaoVO> listaUnidadeGrupoOperacao) {
        this.listaUnidadeGrupoOperacao = listaUnidadeGrupoOperacao;
    }

    public void unidadeGrupoOperacaoExcluir(UnidadeGrupoOperacaoVO unidadeGrupoOperacao) {
        this.unidadeGrupoOperacaoExcluir = unidadeGrupoOperacao;
    }

    public UnidadeGrupoOperacaoVO getUnidadeGrupoOperacaoExcluir() {
        return unidadeGrupoOperacaoExcluir;
    }

    public void setUnidadeGrupoOperacaoExcluir(UnidadeGrupoOperacaoVO unidadeGrupoOperacaoExcluir) {
        this.unidadeGrupoOperacaoExcluir = unidadeGrupoOperacaoExcluir;
    }

    public UnidadeGrupoOperacaoVO getUnidadeGrupoOpExcluir() {
        return unidadeGrupoOpExcluir;
    }

    public void setUnidadeGrupoOpExcluir(UnidadeGrupoOperacaoVO unidadeGrupoOpExcluir) {
        this.unidadeGrupoOpExcluir = unidadeGrupoOpExcluir;
    }

    public boolean isGravacaoAlteracaoFinal() {
        return isGravacaoAlteracaoFinal;
    }

    public void setGravacaoAlteracaoFinal(boolean isGravacaoAlteracaoFinal) {
        this.isGravacaoAlteracaoFinal = isGravacaoAlteracaoFinal;
    }

    public UnidadeVO getUnidadeAreaMeio() {
        return unidadeAreaMeio;
    }

    public void setUnidadeAreaMeio(UnidadeVO unidadeAreaMeio) {
        this.unidadeAreaMeio = unidadeAreaMeio;
    }

}
