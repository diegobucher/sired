package br.gov.caixa.gitecsa.sired.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.DocumentoOriginalDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroDocumentoOriginalDTO;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class DocumentoOriginalDAOImpl extends GenericDAOImpl<DocumentoOriginalVO> implements DocumentoOriginalDAO {
    
    private static final String SEPARADOR_NUMERO_REQUISICOES = ",";

    @Override
    public List<DocumentoOriginalVO> consultar(FiltroDocumentoOriginalDTO filtro, Integer offset, Integer limit) {
        return this.consultar(filtro, null, offset, limit);
    }

    @Override
    public List<DocumentoOriginalVO> consultar(FiltroDocumentoOriginalDTO filtro) {
        return this.consultar(filtro, null, null, null);
    }

    @Override
    public List<DocumentoOriginalVO> consultar(FiltroDocumentoOriginalDTO filtro, List<UnidadeVO> unidadesAutorizadas) {
        return this.consultar(filtro, unidadesAutorizadas, null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DocumentoOriginalVO> consultar(FiltroDocumentoOriginalDTO filtro, List<UnidadeVO> unidadesAutorizadas, Integer offset, Integer limit) {

        StringBuilder hql = new StringBuilder("Select Distinct d");
        hql.append(" From DocumentoOriginalVO d ");
        hql.append(" Join Fetch d.requisicao r ");
        hql.append(" Join Fetch r.unidadeSolicitante ");
        hql.append(" Join Fetch r.documento dc ");
        hql.append(" Join Fetch r.requisicaoDocumento rd ");
        hql.append(" Join Fetch rd.unidadeGeradora ");
        hql.append(" Join Fetch dc.grupo g ");
        hql.append(" Join Fetch g.grupoCampos gc ");
        hql.append(" Join Fetch gc.campo ");
        hql.append(" Join Fetch r.tramiteRequisicaoAtual tr ");
        hql.append(" Join Fetch tr.situacaoRequisicao ");
        hql.append(" Left Join Fetch rd.operacao ");
        hql.append(" Left Join Fetch tr.ocorrencia ");
        hql.append(" Left Join fetch tr.suporte ");
        hql.append(" Join Fetch d.base ");
        hql.append(" Join Fetch d.empresaContrato ");
        hql.append(" Join Fetch d.tramiteDocOriginalAtual t ");
        hql.append(" Join Fetch t.situacaoDocOriginal ");
        hql.append(" Where 0 = 0 ");
        hql.append(this.buildSqlConsulta(filtro, unidadesAutorizadas));       
        hql.append(" ORDER BY r.codigoRequisicao ");

        Query query = getEntityManager().createQuery(hql.toString(), DocumentoOriginalVO.class);
        query = this.bindParametersConsulta(query, filtro, unidadesAutorizadas);

        if (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) {
            return query.setFirstResult(offset).setMaxResults(limit).getResultList();
        } else {
            return query.getResultList();
        }
    }
    
    @Override
    public Integer count(FiltroDocumentoOriginalDTO filtro) {
        return this.count(filtro, null);
    }

    @Override
    public Integer count(FiltroDocumentoOriginalDTO filtro, List<UnidadeVO> unidadesAutorizadas) {

        StringBuilder hql = new StringBuilder("Select count(d) ");
        hql.append(" From DocumentoOriginalVO d ");
        hql.append(" Join d.requisicao r ");
        hql.append(" Join d.tramiteDocOriginalAtual t ");
        hql.append(" Where 0 = 0 ");
        hql.append(this.buildSqlConsulta(filtro, unidadesAutorizadas));

        Query query = getEntityManager().createQuery(hql.toString());
        query = this.bindParametersConsulta(query, filtro, unidadesAutorizadas);

        try {
            return ((Long) query.getSingleResult()).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private StringBuilder buildSqlConsulta(FiltroDocumentoOriginalDTO filtro, List<UnidadeVO> unidadesAutorizadas) {

        StringBuilder hql = new StringBuilder();

        if (StringUtils.isNotBlank(filtro.getNumeroRequisicoes())) {
            hql.append(" AND r.codigoRequisicao in (:ids) ");
        }

        if (StringUtils.isNotBlank(filtro.getMatriculaUsuario())) {
            hql.append(" AND upper(r.codigoUsuarioAbertura) = :matriculaUsuario ");
        }

        if (!Util.isNullOuVazio(filtro.getUnidadeSolicitante()) && !Util.isNullOuVazio(filtro.getUnidadeSolicitante().getId())) {
            hql.append(" AND r.unidadeSolicitante = :unidadeSolicitante ");
        }

        if (!Util.isNullOuVazio(filtro.getBase())) {
            hql.append(" AND d.base = :base ");
        } else if (!Util.isNullOuVazio(unidadesAutorizadas)) {
            hql.append(" AND r.unidadeSolicitante in (:unidadesAutorizadas) ");
        }
        
        if (!Util.isNullOuVazio(filtro.getSituacaoDocOriginal()) && !filtro.getSituacaoDocOriginal().getValue().equals(BigDecimal.ZERO.toString())) {
            hql.append(" AND t.situacaoDocOriginal.id = :situacao ");
        }

        if (!Util.isNullOuVazio(filtro.getDataInicio()) && !Util.isNullOuVazio(filtro.getDataFim())) {
            hql.append(" AND r.dataHoraAbertura between :dataInicio and :dataFim ");
        }

        return hql;
    }

    private Query bindParametersConsulta(Query query, FiltroDocumentoOriginalDTO filtro, List<UnidadeVO> unidadesAutorizadas) {

        if (StringUtils.isNotBlank(filtro.getNumeroRequisicoes())) {

            List<Long> ids = new ArrayList<Long>();
            String[] requisicoes = StringUtils.split(filtro.getNumeroRequisicoes(), SEPARADOR_NUMERO_REQUISICOES);

            for (String id : requisicoes) {
                ids.add(Long.valueOf(id));
            }

            query.setParameter("ids", ids);
        }

        if (StringUtils.isNotBlank(filtro.getMatriculaUsuario())) {
            query.setParameter("matriculaUsuario", filtro.getMatriculaUsuario().toUpperCase());
        }

        if (!Util.isNullOuVazio(filtro.getUnidadeSolicitante()) && !Util.isNullOuVazio(filtro.getUnidadeSolicitante().getId())) {
            query.setParameter("unidadeSolicitante", filtro.getUnidadeSolicitante());
        }

        if (!Util.isNullOuVazio(filtro.getBase())) {
            query.setParameter("base", filtro.getBase());
        } else if (!Util.isNullOuVazio(unidadesAutorizadas)) {
            query.setParameter("unidadesAutorizadas", unidadesAutorizadas);
        }
        
        if (!Util.isNullOuVazio(filtro.getSituacaoDocOriginal()) && !filtro.getSituacaoDocOriginal().getValue().equals(BigDecimal.ZERO.toString())) {
            query.setParameter("situacao", filtro.getSituacaoDocOriginal().getId());
        }

        if (!Util.isNullOuVazio(filtro.getDataInicio()) && !Util.isNullOuVazio(filtro.getDataFim())) {
            
            Date dataInicio = DateUtils.fitAtStart(filtro.getDataInicio());
            Date dataFim = DateUtils.fitAtEnd(filtro.getDataFim());
            
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
        }

        return query;
    }

    @Override
    public DocumentoOriginalVO findByIdEager(Long id) {

        StringBuilder hql = new StringBuilder("Select Distinct d");
        hql.append(" From DocumentoOriginalVO d ");
        hql.append(" Join Fetch d.requisicao r ");
        hql.append(" Join Fetch d.base ");
        hql.append(" Join Fetch d.empresaContrato ");
        hql.append(" Join Fetch d.tramiteDocOriginalAtual t ");
        hql.append(" Join Fetch t.situacaoDocOriginal ");
        hql.append(" Where d.id = :id ");

        TypedQuery<DocumentoOriginalVO> query = getEntityManager().createQuery(hql.toString(), DocumentoOriginalVO.class);
        query.setParameter("id", id);

        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public DocumentoOriginalVO findByCodigo(Long codigo) {

        StringBuilder hql = new StringBuilder("Select d");
        hql.append(" From DocumentoOriginalVO d ");
        hql.append(" Join d.requisicao r ");
        hql.append(" Where r.codigoRequisicao = :codigo ");

        TypedQuery<DocumentoOriginalVO> query = getEntityManager().createQuery(hql.toString(), DocumentoOriginalVO.class);
        query.setParameter("codigo", codigo);

        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
