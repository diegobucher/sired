package br.gov.caixa.gitecsa.arquitetura.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.component.datatable.DataTable;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

/**
 * Classe que concentra o fluxo de uma tela de consulta que pode redirecionar para cadastro,edição.
 * Sendo possível excluir e filtrar
 * 
 * @author wfjesus
 *
 * @param <T>
 */
public abstract class BaseConsulta<T extends BaseEntity> extends AppBaseController {

    private static final long serialVersionUID = 4674809733896204945L;

    protected static final String CONSULTA_MESSAGES = "formConsulta:messages";

    protected static final String DATA_TABLE_CRUD = ":consultaForm:dataTableCrud";

    private T instanceFilter;

    private List<T> lista;

    @Inject
    protected ContextoController contextoController;

    /**
     * Método responsável por criar a instancia que será utilzado pelo filtro da consulta
     * 
     * @return
     */
    protected abstract T newInstance();

    /**
     * Método responsável por vincular o service da Entidade parametrizada
     * 
     * @return
     */
    protected abstract AbstractService<T> getService();

    public abstract String nomeFuncionalidade();

    @PostConstruct
    public void inicializar() {
        lista = new ArrayList<T>();
        instanceFilter = newInstance();
    }

    /**
     * 
     * @return Retorna a instância utilizada para filtrar a consulta
     */
    public T getInstanceFilter() {
        if (instanceFilter == null) {
            instanceFilter = newInstance();
        }
        return instanceFilter;
    }

    public void setInstanceFilter(T instanceFilter) {
        this.instanceFilter = instanceFilter;
    }

    /**
     * Método responsável por limpar os campos de filtro
     */
    public void limparFiltro() {
        instanceFilter = newInstance();
        lista = new ArrayList<T>();
    }

    /**
     * Método responsável por consultar os registros de acordo aos filtros informados
     * 
     * @throws Exception
     */
    public void consultar() {
        try {
            lista = getService().findByParameters(instanceFilter);
        } catch (AppException ae) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(ae.getMessage(), nomeFuncionalidade(), "localizar"));
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), nomeFuncionalidade(), "localizar"));
        }
    }

    /**
     * limpa a ordenação e paginação da data table
     */
    public void resetDatatable() {
        final DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(DATA_TABLE_CRUD);
        if (table != null) {
            table.reset();
            table.setSortBy(null);
        }
    }

    /**
     * Procura mensagem no contexto(sessão) e caso exista adiciona no
     * facesMessager e limpa o contexto
     */
    public void updateMessage() {
        if (contextoController.getCrudMessage() != null) {
            facesMessager.info(contextoController.getCrudMessage());
            contextoController.setCrudMessage(null);
        }
    }

    /**
     * Lista exibida na grid de resultados da consulta
     * 
     * @return List<T>
     */
    public List<T> getLista() {
        return lista;
    }

    /**
     * Guarda a lista que será exibida na grid de resultados da consulta
     */
    public void setLista(List<T> lista) {
        this.lista = lista;
    }
}
