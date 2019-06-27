package br.gov.caixa.gitecsa.sired.arquitetura.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class GenericDAOImpl<T> extends AbstractGenericDAOImpl implements GenericDAO<T> {

	private Class<T> persistentClass;

	@SuppressWarnings("unchecked")
	public GenericDAOImpl() {
		this.persistentClass =	(Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public T save(final T entity) {
		getEntityManager().persist(entity);
		
		return entity;
	}

	public T update(final T entity) {
		return getEntityManager().merge(entity);
	}    

	public void delete(final T entity) {
		final T attachedEntity = getEntityManager().merge(entity);
		
		getEntityManager().remove(attachedEntity);
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return getEntityManager().createQuery("select a from " + persistentClass.getName() + " a")
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll(final int first, final int max) {
		final org.hibernate.Query query =
				getSession().createQuery("select a from " + persistentClass.getName() + " a");
		
		query.setFirstResult(first);
		
		query.setFetchSize(max);
		
		query.setMaxResults(max);

		return query.list();
	}

	@SuppressWarnings("unchecked")
	public T findById(final Serializable id) {
		return (T) getSession().get(persistentClass, id);
	}
	
	public T find(final Class<T> classe, final Serializable id) {
		return getEntityManager().find(classe, id);
	}

  /**
   * @return the persistentClass
   */
  public Class<T> getPersistentClass() {
    return persistentClass;
  }
}
