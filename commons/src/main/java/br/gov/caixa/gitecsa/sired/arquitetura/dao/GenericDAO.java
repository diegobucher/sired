package br.gov.caixa.gitecsa.sired.arquitetura.dao;

import java.io.Serializable;
import java.util.List;

public interface GenericDAO<T> {
    public T save(T entity);
    
    public T update(T entity);  
    
    public void delete(T entity);
    
    public List<T> findAll();
    
    public List<T> findAll(int first, int max);
    
    public T findById(Serializable id);
    
    public T find(Class<T> classe, Serializable id);
}
