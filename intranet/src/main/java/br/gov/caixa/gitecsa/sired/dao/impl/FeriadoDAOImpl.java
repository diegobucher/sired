package br.gov.caixa.gitecsa.sired.dao.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.dao.FeriadoDAO;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class FeriadoDAOImpl extends GenericDAOImpl<FeriadoVO> implements FeriadoDAO {

    @Override
    public Date findProximaDataUtil(final Date data, final Integer dias, final UnidadeVO unidadeVO) throws DataBaseException {

        int diasUteis = BigDecimal.ZERO.intValue();
        Calendar dia = Calendar.getInstance();
        dia.setTime(data);

        while (diasUteis < dias) {
            dia.add(Calendar.DAY_OF_MONTH, BigDecimal.ONE.intValue());
            int diaSemana = dia.get(Calendar.DAY_OF_WEEK);

            if (diaSemana != Calendar.SATURDAY && diaSemana != Calendar.SUNDAY && !isFeriado(dia.getTime(), unidadeVO)) {
                diasUteis++;
            }
        }

        return dia.getTime();
    }

    @Override
    public Boolean isFeriado(final Date data, final UnidadeVO unidadeVO) throws DataBaseException {

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
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public List<FeriadoVO> findByPeriodo(final Date dtInicio, final Date dtFim, final UnidadeVO unidadeVO) throws DataBaseException {

        StringBuilder hql = new StringBuilder();

        hql.append(" Select f From FeriadoVO f ");
        hql.append(" Where 0 = 0 ");
        if(unidadeVO.getUf() != null) {
          hql.append(" And f.uf = coalesce(:uf, f.uf) ");
        }
        if(unidadeVO.getMunicipio() != null) {
          hql.append(" And f.municipio = coalesce(:municipio, f.municipio) ");
        }
        hql.append(" And f.data >= :dtInicio And f.data <= :dtFim ");

        TypedQuery<FeriadoVO> query = getEntityManager().createQuery(hql.toString(), FeriadoVO.class);
        
        if(unidadeVO.getUf() != null) {
          query.setParameter("uf", unidadeVO.getUf());
        }
        if(unidadeVO.getMunicipio() != null) {
          query.setParameter("municipio", unidadeVO.getMunicipio());
        }
        query.setParameter("dtInicio", dtInicio);
        query.setParameter("dtFim", dtFim);
        
        try {
        	return query.getResultList();
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public void update(List<FeriadoVO> listFeriado) {
        int contador = 1;
        for (FeriadoVO entity : listFeriado) {
            this.getEntityManager().merge(entity);

            if (contador % 100 == 0) {
                this.getEntityManager().flush();
                this.getEntityManager().clear();
            }
            contador++;
        }
    }

    @Override
    public void persist(List<FeriadoVO> listFeriado) {
        int contador = 1;
        for (FeriadoVO entity : listFeriado) {
            this.getEntityManager().persist(entity);

            if (contador % 100 == 0) {
                this.getEntityManager().flush();
                this.getEntityManager().clear();
            }
            contador++;
        }
    }
}
