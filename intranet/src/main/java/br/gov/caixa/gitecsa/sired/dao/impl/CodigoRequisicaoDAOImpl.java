package br.gov.caixa.gitecsa.sired.dao.impl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.hibernate.CacheMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.dao.CodigoRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.dto.CodigoRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@ApplicationScoped
public class CodigoRequisicaoDAOImpl implements CodigoRequisicaoDAO {

    private static final String TABLE_NAME = "redsm001.redtbs08_codigo_requisicao";

    @PersistenceUnit(unitName = "siredPU")
    private EntityManagerFactory emf;

    private EntityManager em;

    @PostConstruct
    private void init() throws BusinessException {
        this.em = this.emf.createEntityManager();
    }

    public CodigoRequisicaoDTO findByUnidadeAno(UnidadeVO unidade, Integer ano) {

        StringBuilder sql = new StringBuilder();
        sql.append(String.format(" Select nu_requisicao From %s ", TABLE_NAME));
        sql.append(" Where nu_unidade_a02 = :idUnidade And nu_ano = :ano ");

        SQLQuery query = getSession().createSQLQuery(sql.toString());
        query.setParameter("idUnidade", unidade.getId());
        query.setParameter("ano", ano);

        try {

            CodigoRequisicaoDTO sequence = new CodigoRequisicaoDTO();
            sequence.setNuRequisicao((Integer) query.uniqueResult());
            sequence.setUnidade(unidade);
            sequence.setNuAno(ano);

            return (!ObjectUtils.isNullOrEmpty(sequence.getNuRequisicao())) ? sequence : null;

        } catch (Exception e) {
            return null;
        }
    }

    public void insert(CodigoRequisicaoDTO codigoRequisicao) {
        String sql = String.format("Insert Into %s (nu_unidade_a02, nu_ano, nu_requisicao) Values (:idUnidade, :ano, :sequencial) ", TABLE_NAME);

        SQLQuery query = getSession().createSQLQuery(sql);
        query.setParameter("idUnidade", codigoRequisicao.getUnidade().getId());
        query.setParameter("ano", codigoRequisicao.getNuAno());
        query.setParameter("sequencial", codigoRequisicao.getNuRequisicao());
        query.executeUpdate();
    }

    public void update(CodigoRequisicaoDTO codigoRequisicao) {
        String sql = String.format("Update %s Set nu_requisicao = :sequencial Where nu_unidade_a02 = :idUnidade And nu_ano = :ano ", TABLE_NAME);

        SQLQuery query = getSession().createSQLQuery(sql);
        query.setParameter("idUnidade", codigoRequisicao.getUnidade().getId());
        query.setParameter("ano", codigoRequisicao.getNuAno());
        query.setParameter("sequencial", codigoRequisicao.getNuRequisicao());
        query.executeUpdate();
    }

    @Override
    public EntityManager getEntityManager() {
        return this.em;
    }

    @Override
    public Session getSession() {
        Session session = this.em.unwrap(Session.class);
        session.setCacheMode(CacheMode.IGNORE);

        return session;
    }
}
