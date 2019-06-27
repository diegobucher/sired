package br.gov.caixa.gitecsa.arquitetura.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.component.datatable.DataTable;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Util;

/**
 * Classe que concentra o fluxo de uma tela de consulta que pode redirecionar para cadastro,edição.
 * Sendo possível excluir e filtrar
 * 
 * @author wfjesus
 *
 * @param <T>
 */
public abstract class BaseConsultaRD<T extends BaseEntity> extends AppBaseController {

    private static final long serialVersionUID = 4674809733896204945L;

    protected static final String CONSULTA_MESSAGES = "formConsulta:messages";

    protected static final String DATA_TABLE_CRUD = ":consultaForm:dataTableCrud";

    private T instanceFilter;

    private List<T> lista;

    private T instanceExcluir;

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

    /**
     * Sufixo do nome tela que o controller está vinculada, Isso serve
     * Para tornar possível voltar para tela chamadora.
     * 
     * @return
     */
    protected abstract String getTelaOrigem();

    protected abstract String getSufixoTela();

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
     * Redireciona para tela de cadastro
     * 
     * @return
     */
    public String novo() {
        contextoController.limpar();
        contextoController.setTelaOrigem(getTelaOrigem());
        return includeRedirect("cadastro-".concat(getSufixoTela()));
    }

    /**
     * Método responsável por redirecionara para tela que edita o registro selecionado
     * 
     * @param t
     *            - Objeto que representa a linha selecionada
     * @return
     */
    public String editar(T t) {
        this.contextoController.limpar();
        contextoController.setObject(t);
        contextoController.setTelaOrigem("consulta-".concat(getSufixoTela()));
        return includeRedirect("cadastro-".concat(getSufixoTela()));
    }

    /**
     * Método responsável por consultar os registros de acordo aos filtros informados
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void consultar() {
        try {
            lista = getService().findByParameters(instanceFilter);
            T cloneFilter = (T) BeanUtils.cloneBean(instanceFilter);
            contextoController.setObjectFilter(cloneFilter);
            if (Util.isNullOuVazio(lista)) {
                facesMessager.addMessageError("general.crud.noItemFound");
            }
        } catch (RequiredException re) {
            for (String message : re.getErroList())
                facesMessager.addMessageError(message);
            lista = new ArrayList<T>();
        } catch (BusinessException be) {
            for (String message : be.getErroList())
                facesMessager.addMessageError(message);
            lista = new ArrayList<T>();
        } catch (AppException ae) {
            facesMessager.addMessageError(ae.getMessage());
            lista = new ArrayList<T>();
        } catch (Exception e) {
            facesMessager.addMessageError(e.getMessage());
            lista = new ArrayList<T>();
        }
    }

    /**
     * Usado para refazer a consulta após a inclusão/alteração de um registro
     */
    @SuppressWarnings("unchecked")
    public void reConsultar() {
        try {
            Object obj = contextoController.getObjectFilter();
            if (obj != null && newInstance().getClass().isInstance(obj)) {
                setInstanceFilter((T) obj);
                T cloneFilter = (T) BeanUtils.cloneBean(getInstanceFilter());
                contextoController.setObjectFilter(cloneFilter);
                setLista(getService().findByParameters(getInstanceFilter()));
            }
        } catch (AppException ae) {
            logger.error(ae);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Método responsável por excluir o registro selecionado
     * 
     * @param t
     *            - Objeto que repesenta a linha selecionada
     * @throws BusinessException
     * @throws Exception
     */
    public void excluir() {
        try {
            getService().delete(instanceExcluir);
            lista.remove(instanceExcluir);
            facesMessager.addMessageInfo("general.crud.excluido");
        } catch (BusinessException be) {
            for (String e : be.getErroList())
                facesMessager.addMessageError(e);
        } catch (Exception e) {
            facesMessager.addMessageError(e.getMessage());
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método utilizado na tag metadata do tipo preRenderView da tela de lista.
     */
    @SuppressWarnings("unchecked")
    public void prepareToResult() throws Exception {

        if (contextoController.getCrudMessage() != null) {
            facesMessager.info(contextoController.getCrudMessage());
            contextoController.setCrudMessage(null);
        }
        if (!Util.isNullOuVazio(contextoController.getObjectFilter())) {
            this.setInstanceFilter((T) contextoController.getObjectFilter());
            lista = getService().findByParameters(instanceFilter);
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

    /**
     * Retorna a instância do objeto que será excluído
     * 
     * @return T
     */
    public T getInstanceExcluir() {
        return instanceExcluir;
    }

    /**
     * Guarda uma instância do objeto que será excluído
     */
    public void setInstanceExcluir(T instanceExcluir) {
        this.instanceExcluir = instanceExcluir;
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
}
