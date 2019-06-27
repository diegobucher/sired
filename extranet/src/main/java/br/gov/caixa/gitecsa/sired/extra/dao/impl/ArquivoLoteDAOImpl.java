package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.util.List;

import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.extra.dao.ArquivoLoteDAO;
import br.gov.caixa.gitecsa.sired.extra.dto.FiltroArquivoLoteDTO;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;

public class ArquivoLoteDAOImpl extends GenericDAOImpl<ArquivoLoteVO> implements ArquivoLoteDAO {

    @SuppressWarnings("unchecked")
    @Override
    public List<ArquivoLoteVO> consultar(FiltroArquivoLoteDTO filtro, Integer offset, Integer limit) {

        StringBuilder hql = new StringBuilder();
        hql.append(" Select a From ArquivoLoteVO a Join a.empresa e ");
        hql.append(" Where 0 = 0 ");
        hql.append(this.buildSqlConsulta(filtro));
        hql.append(" Order by a.dataEnvioArquivo Desc ");

        Query query = getEntityManager().createQuery(hql.toString(), ArquivoLoteVO.class);
        query = this.bindParametersConsulta(query, filtro);

        return query.setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    public Integer count(FiltroArquivoLoteDTO filtro) {

        StringBuilder hql = new StringBuilder();
        hql.append(" Select Count(a) From ArquivoLoteVO a Join a.empresa e ");
        hql.append(" Where 0 = 0 ");
        hql.append(this.buildSqlConsulta(filtro));

        Query query = getEntityManager().createQuery(hql.toString());
        query = this.bindParametersConsulta(query, filtro);

        try {
            return ((Long) query.getSingleResult()).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private StringBuilder buildSqlConsulta(FiltroArquivoLoteDTO filtro) {

        StringBuilder hql = new StringBuilder();
        hql.append(" And e.cnpj = :cnpj ");
        hql.append(" And a.dataEnvioArquivo between :dataInicio and :dataFim ");

        return hql;
    }

    private Query bindParametersConsulta(Query query, FiltroArquivoLoteDTO filtro) {

        query.setParameter("cnpj", filtro.getUsuario().getNuCnpj());
        query.setParameter("dataInicio", filtro.getDataInicio());
        query.setParameter("dataFim", filtro.getDataFim());

        return query;
    }

}
