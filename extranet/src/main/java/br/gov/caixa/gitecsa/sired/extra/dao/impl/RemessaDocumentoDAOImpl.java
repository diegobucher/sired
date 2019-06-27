package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.extra.dao.RemessaDocumentoDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;

public class RemessaDocumentoDAOImpl extends GenericDAOImpl<RemessaDocumentoVO> implements RemessaDocumentoDAO{

  @Override
  public Integer consultaRemessaDocumentoAtrelado(RemessaDocumentoVO remessaDocumentoVO) {
    
      StringBuilder hql = new StringBuilder("");
      
      hql.append(" SELECT COUNT(r)  ");
      hql.append(" FROM RemessaDocumentoVO r ");
      hql.append(" LEFT JOIN FETCH r.unidadeGeradora unidadeGeradora ");
      hql.append(" WHERE r.numeroRemessaTipoAB = :numeroRemessaTipoAB ");
      
      Query query = getEntityManager().createQuery(hql.toString());
      
      query.setParameter("numeroRemessaTipoAB", remessaDocumentoVO);
      
      try {
        return ((Long) query.getSingleResult()).intValue();
    } catch (Exception e) {
        return 0;
    }
  }
}
