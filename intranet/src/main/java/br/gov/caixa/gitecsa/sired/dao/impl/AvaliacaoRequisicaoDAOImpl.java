package br.gov.caixa.gitecsa.sired.dao.impl;

import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.AvaliacaoRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public class AvaliacaoRequisicaoDAOImpl extends GenericDAOImpl<AvaliacaoRequisicaoVO> implements AvaliacaoRequisicaoDAO {

    @Override
    public AvaliacaoRequisicaoVO findByRequisicao(RequisicaoVO requisicao) {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT a ");
        hql.append(" FROM AvaliacaoRequisicaoVO a ");
        hql.append(" Join Fetch a.tramite t ");
        hql.append(" Join Fetch t.requisicao r ");
        hql.append(" Join Fetch a.motivoAvaliacao m ");
        hql.append(" Where r.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), AvaliacaoRequisicaoVO.class);
        query.setParameter("id", requisicao.getId());

        try {
            return (AvaliacaoRequisicaoVO) query.setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Boolean hasAvaliacao(RequisicaoVO requisicao) {
        StringBuilder hql = new StringBuilder();
        hql.append(" Select Count(a.id) ");
        hql.append(" From AvaliacaoRequisicaoVO a ");
        hql.append(" Join a.tramite t ");
        hql.append(" Join t.requisicao r ");
        hql.append(" Where r.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("id", requisicao.getId());

        try {
            return (((Long) query.getSingleResult()).intValue() > 0) ? true : false;
        } catch (Exception e) {
            return false;
        }
    }

}
