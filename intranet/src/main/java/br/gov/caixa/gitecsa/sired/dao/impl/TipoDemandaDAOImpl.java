package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.dao.TipoDemandaDAO;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

public class TipoDemandaDAOImpl extends GenericDAOImpl<TipoDemandaVO> implements TipoDemandaDAO {

    @Override
    public List<TipoDemandaVO> findByDocumento(DocumentoVO documento) throws DataBaseException {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT d FROM TipoDemandaVO d ");
        hql.append(" Where d.icSetorial = :setorial ");

        TypedQuery<TipoDemandaVO> query = getEntityManager().createQuery(hql.toString(), TipoDemandaVO.class);
        query.setParameter("setorial", documento.getIcSetorial());
        
        try {
        	return query.getResultList();
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public TipoDemandaVO findByNomeESetorial(String nome, TipoDocumentoEnum setorial) throws DataBaseException {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT d FROM TipoDemandaVO d ");
        hql.append(" Where d.nome = :nome and d.icSetorial = :setorial ");

        TypedQuery<TipoDemandaVO> query = getEntityManager().createQuery(hql.toString(), TipoDemandaVO.class);
        query.setParameter("nome", nome);
        query.setParameter("setorial", setorial);
        
        try {
        	return query.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

}
