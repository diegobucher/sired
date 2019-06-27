package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.AuditoriaDAO;
import br.gov.caixa.gitecsa.sired.enumerator.TipoAuditoriaEnum;
import br.gov.caixa.gitecsa.sired.vo.AuditoriaVO;
import br.gov.caixa.gitecsa.sired.vo.TipoAuditoriaVO;

public class AuditoriaDAOImpl extends GenericDAOImpl<AuditoriaVO> implements AuditoriaDAO {

	@Override
	public TipoAuditoriaVO findTipoAuditoriaById(final Integer id) {
		
		StringBuilder hql = new StringBuilder();
		
		hql.append("Select t From TipoAuditoriaVO t Where t.id = :id");
		
		TypedQuery<TipoAuditoriaVO> query = this.getEntityManager().createQuery(hql.toString(), TipoAuditoriaVO.class);
		query.setParameter("id", id);
		
		try {			
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<AuditoriaVO> findAllByIdentificador(final Integer identificador) {
		StringBuilder hql = new StringBuilder();
		
		hql.append(" SELECT a ");
		hql.append(" FROM AuditoriaVO a ");
		hql.append(" INNER JOIN a.tipo tipo ");
		hql.append(" WHERE a.identificador = :id ");
		hql.append(" AND tipo.id = :tipoAuditoria ");
		
		TypedQuery<AuditoriaVO> query = this.getEntityManager().createQuery(hql.toString(), AuditoriaVO.class);
		
		query.setParameter("id", identificador);
		query.setParameter("tipoAuditoria", TipoAuditoriaEnum.DOWNLOAD_EFETUADO.getId());
		
		return query.getResultList();
	}

}
