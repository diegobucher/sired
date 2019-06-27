package br.gov.caixa.gitecsa.sired.dao.impl;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.dao.ParametroSistemaDAO;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;

public class ParametroSistemaDAOImpl extends GenericDAOImpl<ParametroSistemaVO> implements ParametroSistemaDAO {
	
    public ParametroSistemaVO findById(Long id) throws DataBaseException {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT p FROM ParametroSistemaVO p ");
        hql.append(" WHERE nuParametroSistema = :id");

        Query query = getEntityManager().createQuery(hql.toString(), ParametroSistemaVO.class);
        query.setParameter("id", id);

        try {
            return (ParametroSistemaVO) query.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    public ParametroSistemaVO findByNome(String nome) throws DataBaseException {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT p FROM ParametroSistemaVO p ");
        hql.append(" WHERE noParametroSistema = :nome ");

        Query query = getEntityManager().createQuery(hql.toString(), ParametroSistemaVO.class);
        query.setParameter("nome", nome);

        try {
            return (ParametroSistemaVO) query.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }
}
