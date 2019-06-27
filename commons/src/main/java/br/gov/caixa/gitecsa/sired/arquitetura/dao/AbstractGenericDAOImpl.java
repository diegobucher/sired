package br.gov.caixa.gitecsa.sired.arquitetura.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;

public abstract class AbstractGenericDAOImpl {

    @PersistenceContext(unitName = "siredPU")
    private EntityManager em;

    public Session getSession() {
        return em.unwrap(Session.class);
    }

    public void setEntityManager(final EntityManager em) {
        this.em = em;
    }

    public EntityManager getEntityManager() {
        return this.em;
    }

}
