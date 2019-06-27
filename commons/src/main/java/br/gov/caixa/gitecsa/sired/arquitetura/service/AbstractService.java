package br.gov.caixa.gitecsa.sired.arquitetura.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.interceptor.AppLogInterceptor;
import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;

@Interceptors(AppLogInterceptor.class)
@Stateless
public abstract class AbstractService<T> implements GenericService<T> {

    private static final long serialVersionUID = -2143374652356315279L;

    @Inject 
    protected Logger logger;

    protected List<String> mensagens;

    protected abstract void validaCamposObrigatorios(T entity) throws BusinessException;

    protected abstract void validaRegras(T entity) throws BusinessException;

    protected abstract void validaRegrasExcluir(T entity) throws BusinessException;

    protected abstract GenericDAO<T> getDAO();

    private boolean editando;

    protected List<T> updateLote(Collection<T> listEntity) throws RequiredException, BusinessException, Exception {
        List<T> listReturn = new ArrayList<T>();

        for (T entity : listEntity) {
            listReturn.add(updateImpl(entity));
        }

        return listReturn;
    }

    public List<T> findAll() {
        return getDAO().findAll();
    }

    public T findById(Long id) {
        return getDAO().findById(id);
    }

    public T save(T entity) throws RequiredException, BusinessException, Exception {
        processValidations(entity, false);

        return saveImpl(entity);
    }

    protected T saveImpl(T entity) throws RequiredException, BusinessException, Exception {
        getDAO().save(entity);
        return entity;
    }

    public T update(T entity) throws RequiredException, BusinessException, Exception {
        processValidations(entity, true);
        return updateImpl(entity);
    }

    protected T updateImpl(T entity) throws RequiredException, BusinessException, Exception {
        getDAO().update(entity);
        return entity;
    }

    public List<T> update(Collection<T> listEntity) throws RequiredException, BusinessException, Exception {
        processValidations(listEntity, true);
        return updateLote(listEntity);
    }

    public void remove(T entity) throws BusinessException, Exception {
        processDeleteValidations(entity);
        removeImpl(entity);
    }
    
    public Map<Object, T> findAllAsMap() throws DataBaseException {
        
        List<T> listEntity = this.findAll();
        Map<Object, T> mapEntity = new HashMap<Object, T>();
        
        for (T entity : listEntity) {
            mapEntity.put(((BaseEntity)entity).getId(), entity);
        }
        
        return mapEntity;
    }

    protected void removeImpl(T entity) throws BusinessException, Exception {
        getDAO().delete(entity);
    }

    protected void processValidations(T entity, boolean editando) throws RequiredException, BusinessException {
        setEditando(editando);

        mensagens = new ArrayList<String>();

        validaCamposObrigatorios(entity);
        
        if (!ObjectUtils.isNullOrEmpty(mensagens)) {
            throw new RequiredException(mensagens);
        }

        validaRegras(entity);

        if (!ObjectUtils.isNullOrEmpty(mensagens)) {
            throw new BusinessException(mensagens);
        }
    }

    protected void processValidations(Collection<T> listEntity, boolean editando) throws RequiredException, BusinessException {
        setEditando(editando);

        mensagens = new ArrayList<String>();

        for (T entity : listEntity) {
            validaCamposObrigatorios(entity);
        }

        if (!ObjectUtils.isNullOrEmpty(mensagens)) {
            throw new RequiredException(mensagens);
        }

        for (T entity : listEntity) {
            validaRegras(entity);
        }

        if (!ObjectUtils.isNullOrEmpty(mensagens)) {
            throw new BusinessException(mensagens);
        }
    }

    protected void processDeleteValidations(T entity) throws BusinessException {
        mensagens = new ArrayList<String>();

        validaRegrasExcluir(entity);

        if (!ObjectUtils.isNullOrEmpty(mensagens)) {
            throw new BusinessException(mensagens);
        }
    }

    protected boolean isEditando() {
        return editando;
    }

    protected void setEditando(boolean editando) {
        this.editando = editando;
    }

}
