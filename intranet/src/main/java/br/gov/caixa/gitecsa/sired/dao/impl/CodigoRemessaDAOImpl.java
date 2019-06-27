package br.gov.caixa.gitecsa.sired.dao.impl;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.hibernate.CacheMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.dao.CodigoRemessaDAO;
import br.gov.caixa.gitecsa.sired.dto.CodigoRemessaDTO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class CodigoRemessaDAOImpl implements CodigoRemessaDAO {
  
  private static final String TABLE_NAME = "redsm001.redtbs11_codigo_remessa";
  
  @PersistenceUnit(unitName = "siredPU")
  private EntityManagerFactory emf;
  
  private EntityManager em;
  
  @PostConstruct
  private void init() throws BusinessException {
      this.em = this.emf.createEntityManager();
  }

  @Override
  public CodigoRemessaDTO findByUnidadeAno(UnidadeVO unidade, Integer ano) {
    StringBuilder sql = new StringBuilder();
    sql.append(String.format(" Select nu_remessa From %s ", TABLE_NAME));
    sql.append(" Where nu_unidade_a02 = :idUnidade And nu_ano = :ano ");

    SQLQuery query = getSession().createSQLQuery(sql.toString());
    query.setParameter("idUnidade", unidade.getId());
    query.setParameter("ano", ano);

    try {

        CodigoRemessaDTO sequence = new CodigoRemessaDTO();
        sequence.setNuRemessa((Integer) query.uniqueResult());
        sequence.setUnidade(unidade);
        sequence.setNuAno(ano);

        return (!ObjectUtils.isNullOrEmpty(sequence.getNuRemessa())) ? sequence : null;

    } catch (Exception e) {
        return null;
    }
  }

  @Override
  public Session getSession() {
    Session session = this.em.unwrap(Session.class);
    session.setCacheMode(CacheMode.IGNORE);
    return session;
  }

  @Override
  public EntityManager getEntityManager() {
    return this.em;
  }


  @Override
  public void insert(CodigoRemessaDTO codigoRemessa) {
    String sql = String.format("Insert Into %s (nu_unidade_a02, nu_ano, nu_remessa) Values (:idUnidade, :ano, :sequencial) ", TABLE_NAME);

    SQLQuery query = getSession().createSQLQuery(sql);
    query.setParameter("idUnidade", codigoRemessa.getUnidade().getId());
    query.setParameter("ano", codigoRemessa.getNuAno());
    query.setParameter("sequencial", codigoRemessa.getNuRemessa());
    query.executeUpdate();
    
  }

  @Override
  public void update(CodigoRemessaDTO codigoRemessa) {
    String sql = String.format("Update %s Set nu_remessa = :sequencial Where nu_unidade_a02 = :idUnidade And nu_ano = :ano ", TABLE_NAME);

    SQLQuery query = getSession().createSQLQuery(sql);
    query.setParameter("idUnidade", codigoRemessa.getUnidade().getId());
    query.setParameter("ano", codigoRemessa.getNuAno());
    query.setParameter("sequencial", codigoRemessa.getNuRemessa());
    query.executeUpdate();
    
  }

}
