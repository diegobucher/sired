package br.gov.caixa.gitecsa.sired.arquitetura.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;


/**
 * Interface contendo operações de consulta e manutenção ao banco de dados
 *
 * @param <T> Entidade sobre a qual serão realizadas as operações de consulta e manutenção.
 * 
 * @author alima
 * 
 */
public interface GenericService<T> extends Serializable {

	T findById(Long id) throws Exception;
	
	List<T> findAll() throws Exception;
	
	T save(T entity) throws RequiredException, BusinessException, Exception;
	
	T update(T entity) throws RequiredException, BusinessException, Exception;
	
	List<T> update(Collection<T> listEntity) throws RequiredException, BusinessException, Exception;
	
	void remove(T entity) throws BusinessException, Exception;
	
}
