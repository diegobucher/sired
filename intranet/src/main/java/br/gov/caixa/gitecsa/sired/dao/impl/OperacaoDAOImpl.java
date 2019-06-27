package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.dao.OperacaoDAO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.OperacaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class OperacaoDAOImpl extends GenericDAOImpl<OperacaoVO> implements OperacaoDAO {

    public List<OperacaoVO> findAllByAreaMeioEDocumento(UnidadeVO unidade, DocumentoVO documento) {

        StringBuilder hql = new StringBuilder();

        hql.append(" Select o FROM DocumentoVO d ");
        hql.append(" Inner Join d.grupo gp ");
        hql.append(" Inner Join gp.unidadeGrupos ug ");
        hql.append(" inner Join ug.operacao o ");
        hql.append(" Inner Join ug.unidade u ");
        hql.append(" Where d.id = :documento And u.id = :unidade ");

        TypedQuery<OperacaoVO> query = getEntityManager().createQuery(hql.toString(), OperacaoVO.class);
        query.setParameter("documento", documento.getId());
        query.setParameter("unidade", unidade.getId());

        return query.getResultList();
    }

	@Override
	public OperacaoVO findById(String id) throws PersistenceException, Exception {
		StringBuilder hql = new StringBuilder();

        hql.append(" Select o FROM OperacaoVO o ");
        hql.append(" Where o.id = :id ");

        TypedQuery<OperacaoVO> query = getEntityManager().createQuery(hql.toString(), OperacaoVO.class);
		if (id.length() != 3) {
			id = "000" + id;
			id = id.substring(id.length() - 3);
		}
        query.setParameter("id", id);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new DataBaseException("Operação inválida: " + id);
		} catch (PersistenceException e) {
			throw new DataBaseException(e);
		} catch (Exception e) {
			throw e;
		}
	}
}
