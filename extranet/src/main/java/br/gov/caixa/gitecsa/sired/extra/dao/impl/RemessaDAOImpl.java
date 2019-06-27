package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.extra.dao.RemessaDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;

public class RemessaDAOImpl extends GenericDAOImpl<RemessaVO> implements RemessaDAO {

  @Override
  public RemessaVO obterRemessaComMovimentosDiarios(long id) {
    try {
      StringBuilder hql = new StringBuilder("");
      
      hql.append(" SELECT Distinct remessa  ");
      hql.append(" FROM RemessaVO remessa ");
      hql.append(" LEFT JOIN FETCH remessa.unidadeSolicitante unidadeSolicitante ");
      hql.append(" LEFT JOIN FETCH remessa.base base ");
      hql.append(" LEFT JOIN FETCH remessa.empresaContrato ec  ");
      hql.append(" LEFT JOIN FETCH remessa.movimentosDiarioList movimentosDiarioList ");
      hql.append(" LEFT JOIN FETCH movimentosDiarioList.unidadeGeradora unidadeGeradora ");
      hql.append(" LEFT JOIN FETCH remessa.tramiteRemessaAtual t  ");
      hql.append(" LEFT JOIN FETCH t.situacao s  ");
      hql.append(" WHERE remessa.id = :idRemessa ");
      
      TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);
      
      query.setParameter("idRemessa", id);
      
      return query.getSingleResult();
      
    } catch (Exception e) {
      return null;
    }
  }
  
  @Override
  public RemessaVO obterRemessaComListaDocumentos(Long id) {
    try {
      StringBuilder hql = new StringBuilder("");
      
      hql.append(" SELECT Distinct remessa  ");
      hql.append(" FROM RemessaVO remessa ");
      hql.append(" LEFT JOIN FETCH remessa.remessaDocumentos remessaDocumentos ");
      hql.append(" LEFT JOIN FETCH remessaDocumentos.documento documento ");
      hql.append(" LEFT JOIN FETCH remessaDocumentos.unidadeGeradora unidadeGeradora  ");
      hql.append(" LEFT JOIN FETCH remessa.tramiteRemessaAtual t  ");
      hql.append(" LEFT JOIN FETCH t.situacao s  ");
      hql.append(" WHERE remessa.id = :idRemessa ");
      
      TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);
      
      query.setParameter("idRemessa", id);
      
      return query.getSingleResult();
      
    } catch (Exception e) {
      return null;
    }
  }

}
