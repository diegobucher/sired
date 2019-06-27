package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.extra.dao.AvaliacaoDAO;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;

public class AvaliacaoDAOImpl extends GenericDAOImpl<AvaliacaoRequisicaoVO> implements AvaliacaoDAO {

    public AvaliacaoRequisicaoVO obterPorRequisicao(Long idRequisicao) {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT a ");
        hql.append(" FROM AvaliacaoRequisicaoVO a ");
        hql.append(" Join Fetch a.tramite t ");
        hql.append(" Join Fetch t.requisicao r ");
        hql.append(" Join Fetch a.motivoAvaliacao m ");
        hql.append(" Where r.id = :id ");
        hql.append(" order by r.id asc");

        Query query = getEntityManager().createQuery(hql.toString(), AvaliacaoRequisicaoVO.class);
        query.setParameter("id", idRequisicao);
        query.setMaxResults(1);

        try {
            return (AvaliacaoRequisicaoVO) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
