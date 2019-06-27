package br.gov.caixa.gitecsa.arquitetura.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.event.ActionEvent;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.FilterVisitor;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

/**
 * Classe base para CRUD de classes que utilizam o ViewScoped com apenas uma tela.
 * 
 * @author cagalvaom
 * 
 * @param <T>
 *            Parametro tipado
 */
public abstract class BaseViewCrud<T extends BaseEntity> extends AppBaseController implements Serializable {

    private static final long serialVersionUID = 1L;

    private T instance;

    private Long id;

    public BaseViewCrud() {
    }

    // =======================================================================================================
    // Template method
    // =======================================================================================================
    /**
     * Retorna a service responsável pelo controle da página
     * 
     * @return AppService<T>
     */
    protected abstract AbstractService<T> getService();

    /**
     * Criacao da nova instancia
     * 
     * @return T
     */
    protected abstract T newInstance();

    /**
     * Retorna a entity pelo id
     * 
     * @return T
     */
    protected T load(Long id) throws AppException {
        return getService().getById(id);
    }

    // =======================================================================================================
    // Domain methods
    // =======================================================================================================

    /**
     * Lista todas os objetos da classe
     * 
     * @return
     * @throws AppException
     */
    protected List<T> getAll() throws AppException {
        return getService().findAll();
    }

    protected List<T> getByParameters(T instance) throws AppException {
        return getService().findByParameters(instance);
    }

    protected List<T> getByParameters(T instance, FilterVisitor visitor) throws AppException {
        return getService().findByParameters(instance, visitor);
    }

    /**
     * Finaliza a edicao de um registro Normalmente esse metodo deve ser invocado chamando um metodo de atualizacao na entidade(update)
     * 
     * @return
     */
    protected void updateImpl(T referenceValue) throws RequiredException, BusinessException, AppException {
        getService().update(referenceValue);
        facesMessager.info(MensagemUtils.obterMensagem("MS015"));
        limparForm();
    }

    /**
     * Finaliza a criacao de um registro Normalmente esse metodo deve ser invocado chamando um metodo de criacao na entidade(insert)
     * 
     * @return
     */
    protected void saveImpl(T referenceValue) throws RequiredException, BusinessException, AppException {
        getService().save(referenceValue);
        facesMessager.info(MensagemUtils.obterMensagem("MS043"));
        limparForm();
    }

    /**
     * Finaliza a remocao de um registro Normalmente esse metodo deve ser invocado chamando um metodo de remocao na entidade(delete)
     * 
     * @return
     */
    protected void deleteImpl(T referenceValue) throws BusinessException, AppException {
        getService().delete(referenceValue);
        facesMessager.info(MensagemUtils.obterMensagem("MS044", referenceValue));
    }

    /**
     * Indica se a instancia e nova, ou uma ja existente
     * 
     * @return
     */
    public boolean isManaged() {
        return instance.getId() != null;
    }

    public T loadInstance() {
        try {
            return load(getId());
        } catch (AppException e) {
            getRootErrorMessage(e);
        }

        return null;
    }

    public List<T> allInstance() {
        try {
            return getAll();
        } catch (AppException e) {
            getRootErrorMessage(e);
        }

        return null;
    }

    /**
     * Método utilizado para limpar o formulario na tela de consulta.
     */
    public void limparForm() {
        id = null;
        instance = newInstance();
    }

    /**
     * Persiste ou atualiza uma instancia na base de dados.
     */
    public void save() {
        try {
            if (isManaged()) {
                updateImpl(getInstance());
            } else {
                saveImpl(getInstance());
            }
        } catch (RequiredException re) {
            // facesMessager.warn(Util.formatRequiredMessage(re));
            facesMessager.addMessageError(re);
        } catch (BusinessException be) {
            facesMessager.addMessageError(be);
            /* facesMessager.warn(Util.formatBusinessMessage(be)); */
        } catch (AppException e) {
            facesMessager.addMessageError(getRootErrorMessage(e));
        }
    }

    /**
     * Remove uma entidade
     * 
     * @return
     */
    public void delete(ActionEvent event) {
        try {
            deleteImpl(instance);
            // limparForm();
        } catch (BusinessException be) {
            /* facesMessager.warn(Util.formatBusinessMessage(be)); */
        } catch (AppException e) {
            getRootErrorMessage(e);
        }
    }

    // =======================================================================================================
    // Getters/Setters
    // =======================================================================================================
    public T getInstance() {
        if (instance == null) {
            if (getId() != null) {
                instance = loadInstance();
            } else {
                instance = newInstance();
            }
        }
        return instance;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Método responsável por informar se a instance é nula ou não
     * 
     * @return
     */
    public boolean instanceIsNull() {
        return this.instance == null;
    }
}
