package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.util.Date;

import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.extra.dao.FeriadoDAO;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class FeriadoDAOImpl extends GenericDAOImpl<FeriadoVO> implements FeriadoDAO {

    @Override
    public Boolean isFeriado(final Date data, final UnidadeVO unidadeVO) {

    	StringBuilder hql = new StringBuilder();

        hql.append(" Select count(f.id) From FeriadoVO f ");
        hql.append(" Where ( ");
        hql.append(" (f.uf = :uf And f.municipio = :municipio) ");
        hql.append(" Or (f.uf = :uf And f.municipio is null) ");
        hql.append(" Or (f.uf is null And f.municipio is null) ");
        hql.append(" ) And f.data = :data ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("uf", unidadeVO.getUf());
        query.setParameter("municipio", unidadeVO.getMunicipio());
        query.setParameter("data", data);

        try {
            return (((Long) query.getSingleResult()).longValue() > 0) ? true : false;
        } catch (Exception e) {
            return false;
        }
    }
}
