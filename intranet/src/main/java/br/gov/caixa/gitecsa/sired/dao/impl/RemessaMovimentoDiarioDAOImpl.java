package br.gov.caixa.gitecsa.sired.dao.impl;

import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.RemessaMovimentoDiarioDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;

public class RemessaMovimentoDiarioDAOImpl extends GenericDAOImpl<RemessaMovimentoDiarioVO> implements RemessaMovimentoDiarioDAO{

  @Override
  public RemessaMovimentoDiarioVO findByUnidadeDtMovimento(RemessaMovimentoDiarioVO remessaMovDiarioVO) {
    
    StringBuilder hql = new StringBuilder("Select r");
    hql.append(" From RemessaMovimentoDiarioVO r ");
    hql.append(" Join Fetch r.remessa rem ");
    hql.append(" Where r.dataMovimento in (:dataMovimento) ");
    hql.append(" And r.unidadeGeradora in (:unidadeGeradora) ");
    hql.append(" Order by r.id ");

    TypedQuery<RemessaMovimentoDiarioVO> query = getEntityManager().createQuery(hql.toString(), RemessaMovimentoDiarioVO.class);

    query.setParameter("dataMovimento", remessaMovDiarioVO.getDataMovimento());
    query.setParameter("unidadeGeradora", remessaMovDiarioVO.getUnidadeGeradora());
    
    try {
      return query.getResultList().get(0);
    }catch(Exception e) {
      return null;
    }
  }
  
}
