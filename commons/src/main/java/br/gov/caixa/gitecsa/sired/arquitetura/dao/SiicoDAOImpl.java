package br.gov.caixa.gitecsa.sired.arquitetura.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;

public class SiicoDAOImpl<T> extends GenericDAOImpl<T> {
    
    @PersistenceContext(unitName = "siicoPU")
    private EntityManager em;
    
    @Override
    public EntityManager getEntityManager() {
        return this.em;
    }
    
    public Session getSessionSiico() {
      return em.unwrap(Session.class);
  }
}
