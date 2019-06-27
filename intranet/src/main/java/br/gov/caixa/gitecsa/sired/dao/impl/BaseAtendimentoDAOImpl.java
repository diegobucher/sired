package br.gov.caixa.gitecsa.sired.dao.impl;

import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.BaseAtendimentoDAO;
import br.gov.caixa.gitecsa.sired.vo.BaseAtendimentoVO;

public class BaseAtendimentoDAOImpl extends GenericDAOImpl<BaseAtendimentoVO> implements BaseAtendimentoDAO {

    @Override
    public BaseAtendimentoVO findByIdEager(Long id) {
        StringBuilder hql = new StringBuilder();
        hql.append(" Select ba ");
        hql.append(" From BaseAtendimentoVO ba ");
        hql.append(" Join Fetch ba.base ");
        hql.append(" Join Fetch ba.documento ");
        hql.append(" Where ba.id = :id ");

        TypedQuery<BaseAtendimentoVO> query = getEntityManager().createQuery(hql.toString(), BaseAtendimentoVO.class);
        query.setParameter("id", id);

        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
}
