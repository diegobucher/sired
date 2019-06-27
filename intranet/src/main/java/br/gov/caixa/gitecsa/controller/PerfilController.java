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
import br.gov.caixa.gitecsa.service.FuncionalidadeService;
import br.gov.caixa.gitecsa.service.PerfilService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.FuncionalidadeVO;
import br.gov.caixa.gitecsa.sired.vo.PerfilFuncionalidadePK;
import br.gov.caixa.gitecsa.sired.vo.PerfilFuncionalidadeVO;
import br.gov.caixa.gitecsa.sired.vo.PerfilVO;

@Named
@ViewScoped
public class PerfilController extends BaseConsultaCRUD<PerfilVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private PerfilService service;

    @Inject
    private FuncionalidadeService serviceFuncionalidade;

    private DualListModel<FuncionalidadeVO> dualList;

    private List<PerfilVO> lista;

    private static String NOME_RELATORIO = "RELATÓRIO DE PERFIL DE USUÁRIOS";

    private boolean faseEditar;

    private List<PerfilVO> listaFiltro;

    @Override
    protected PerfilVO newInstance() {
        return new PerfilVO();

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
        setLista(service.findAll());
        listaFiltro = getLista();
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
     * Salva
     * 
     * @throws AppException
     */
    public void salvar() {
        getInstance();
        if (!faseEditar)
            getInstance().setPerfilFuncionalidade(new ArrayList<PerfilFuncionalidadeVO>());
        try {
            super.save();
        } catch (BusinessException e) {
            return;
        } catch (AppException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            return;
        }

        getInstance().setPerfilFuncionalidade(new ArrayList<PerfilFuncionalidadeVO>());
        for (FuncionalidadeVO func : dualList.getTarget()) {
            PerfilFuncionalidadeVO pfunc = new PerfilFuncionalidadeVO();
            pfunc.setPerfil(getInstance());
            pfunc.setFuncionalidade(func);
            PerfilFuncionalidadePK pk = new PerfilFuncionalidadePK();
            pk.setNuFuncionalidade((Long) func.getId());
            pk.setNuPerfil((Long) getInstance().getId());
            pfunc.setId(pk);
            getInstance().getPerfilFuncionalidade().add(pfunc);
        }
        try {
            service.salvarListaFuncionalidade(getInstance());
        } catch (BusinessException e) {
            return;
        } catch (AppException e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            return;
        }

        setInstanceFilter(null);

        obterLista();

    }

    private void iniciarDualList() {
        List<FuncionalidadeVO> target = new ArrayList<FuncionalidadeVO>();
        dualList = new DualListModel<FuncionalidadeVO>(serviceFuncionalidade.findAll(), target);

        ordenarDualList();
    }

    private List<FuncionalidadeVO> getNaoAssociados(List<FuncionalidadeVO> itens, List<FuncionalidadeVO> itensCadastrados) {
        List<FuncionalidadeVO> source = new ArrayList<FuncionalidadeVO>();
        boolean encontrado = false;
        for (FuncionalidadeVO doc : itens) {
            encontrado = false;
            for (FuncionalidadeVO grupoDoc : itensCadastrados) {
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
    public void edita(PerfilVO vo) throws AppException {

        faseEditar = true;
        super.edita(vo);

        PerfilVO filter = new PerfilVO();
        filter.setId(getInstance().getId());

        List<FuncionalidadeVO> source = getNaoAssociados(serviceFuncionalidade.findAll(), vo.getFuncionalidades());
        List<FuncionalidadeVO> target = vo.getFuncionalidades();
        dualList = new DualListModel<FuncionalidadeVO>(source, target);
        ordenarDualList();

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
        setInstance(new PerfilVO());
        iniciarDualList();
    }

    public void novo() {
        limparForm();
        faseEditar = false;
    }

    public void ordenarDualList() {

        Collections.sort(dualList.getTarget(), new Comparator<FuncionalidadeVO>() {

            @Override
            public int compare(FuncionalidadeVO o1, FuncionalidadeVO o2) {

                return o1.getNomeFuncionalidadePaiFilha().compareToIgnoreCase(o2.getNomeFuncionalidadePaiFilha());
            }
        });

        Collections.sort(dualList.getSource(), new Comparator<FuncionalidadeVO>() {

            @Override
            public int compare(FuncionalidadeVO o1, FuncionalidadeVO o2) {

                return o1.getNomeFuncionalidadePaiFilha().compareToIgnoreCase(o2.getNomeFuncionalidadePaiFilha());
            }
        });
    }

    @Override
    protected AbstractService<PerfilVO> getService() {
        return service;
    }

    public boolean isFaseEditar() {
        return faseEditar;
    }

    public void setFaseEditar(boolean faseEditar) {
        this.faseEditar = faseEditar;
    }

    public List<PerfilVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<PerfilVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public List<PerfilVO> getLista() {
        return lista;
    }

    public void setLista(List<PerfilVO> lista) {
        this.lista = lista;
    }

    public DualListModel<FuncionalidadeVO> getDualList() {
        return dualList;
    }

    public void setDualList(DualListModel<FuncionalidadeVO> dualList) {
        this.dualList = dualList;
    }

    public FuncionalidadeService getServiceFuncionalidade() {
        return serviceFuncionalidade;
    }

    public void setServiceFuncionalidade(FuncionalidadeService serviceFuncionalidade) {
        this.serviceFuncionalidade = serviceFuncionalidade;
    }

}
