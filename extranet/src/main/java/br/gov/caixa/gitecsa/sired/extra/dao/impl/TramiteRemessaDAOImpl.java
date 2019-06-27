package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.util.List;

import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.extra.dao.TramiteRemessaDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;

public class TramiteRemessaDAOImpl extends GenericDAOImpl<TramiteRemessaVO> implements TramiteRemessaDAO {

  @Override
  public SituacaoRemessaVO buscarSituacaoRemessa(Long id) {
    
    StringBuilder hql = new StringBuilder();
    hql.append(" Select sit ");
    hql.append(" From SituacaoRemessaVO sit ");
    hql.append(" Where sit.id = :id ");

    Query query = getEntityManager().createQuery(hql.toString(), SituacaoRemessaVO.class);
    query.setParameter("id", id);

    return (SituacaoRemessaVO) query.getSingleResult();
    
  }
  
  public List<TramiteRemessaVO> findByRemessa(RemessaVO remessa) throws AppException{

    StringBuilder hql = new StringBuilder();
    hql.append("SELECT tramite FROM TramiteRemessaVO tramite ");
    hql.append("JOIN FETCH tramite.situacao ");
    hql.append("WHERE tramite.remessa.id = " + remessa.getId());
    hql.append(" ORDER BY tramite.dataTramiteRemessa");

    Query query = getEntityManager().createQuery(hql.toString(), TramiteRemessaVO.class);

    return query.getResultList();
}

}
