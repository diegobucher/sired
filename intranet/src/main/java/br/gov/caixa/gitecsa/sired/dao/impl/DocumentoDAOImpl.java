package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.dao.DocumentoDAO;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentalEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;

public class DocumentoDAOImpl extends GenericDAOImpl<DocumentoVO> implements DocumentoDAO {

    public List<DocumentoVO> consultar(DocumentoVO filtro, Integer offset, Integer limit) {

        StringBuilder hql = new StringBuilder(" Select documento ");
        hql.append(" From DocumentoVO documento ");
        hql.append(" Join Fetch documento.grupo grupo ");
        hql.append(" Where 0 = 0 ");

        hql.append(this.buildSqlConsulta(filtro));
        hql.append(" Order by documento.nome asc ");

        TypedQuery<DocumentoVO> query = getEntityManager().createQuery(hql.toString(), DocumentoVO.class);
        this.bindParametersConsulta(query, filtro);

        return query.setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public Integer count(DocumentoVO filtro) {

        StringBuilder hql = new StringBuilder(" Select count(documento.id) ");
        hql.append(" From DocumentoVO documento ");
        hql.append(" Inner Join documento.grupo grupo ");
        hql.append(" Where 0 = 0 ");
        hql.append(this.buildSqlConsulta(filtro));

        Query query = getEntityManager().createQuery(hql.toString());
        this.bindParametersConsulta(query, filtro);

        try {
            return ((Long) query.getSingleResult()).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private StringBuilder buildSqlConsulta(DocumentoVO filtro) {

        StringBuilder hql = new StringBuilder();

        if (StringUtils.isNotBlank(filtro.getNome())) {
            hql.append(" and upper(documento.nome) like :nome ");
        }

        if (!Util.isNullOuVazio(filtro.getGrupo()) && !Util.isNullOuVazio(filtro.getGrupo().getTipoSolicitacao())) {
            hql.append(" and grupo.tipoSolicitacao = :tipoSolicitacao ");
        }

        return hql;
    }

    private Query bindParametersConsulta(Query query, DocumentoVO filtro) {

        if (StringUtils.isNotBlank(filtro.getNome())) {
            query.setParameter("nome", "%" + filtro.getNome().toUpperCase() + "%");
        }

        if (!Util.isNullOuVazio(filtro.getGrupo()) && !Util.isNullOuVazio(filtro.getGrupo().getTipoSolicitacao())) {
            query.setParameter("tipoSolicitacao", filtro.getGrupo().getTipoSolicitacao());
        }

        return query;
    }

    @Override
    public DocumentoVO findByIdEager(Long id) {

        StringBuilder hql = new StringBuilder();

        hql.append(" Select d From DocumentoVO d ");
        hql.append(" Left Join Fetch d.grupo g ");
        hql.append(" Left Join Fetch g.grupoCampos gc ");
        hql.append(" Left Join Fetch gc.campo ");
        hql.append(" Where d.id = :id ");
        hql.append(" Order by gc.ordem asc ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("id", id);

        try {
            return (DocumentoVO) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<GrupoCampoVO> obterUltimaVersaoCamposDocumento(DocumentoVO documento) {
        
        StringBuilder hql = new StringBuilder();

        hql.append(" Select d From DocumentoVO d ");
        hql.append(" Left Join Fetch d.grupo g ");
        hql.append(" Left Join Fetch g.grupoCampos gc ");
        hql.append(" Left Join Fetch gc.campo ");
        hql.append(" Where d.id = :id ");
        hql.append(" Order by gc.ordem asc ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("id", documento.getId());

        try {
            DocumentoVO entity = (DocumentoVO) query.getSingleResult();
            return entity.getGrupo().getGrupoCampos();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<DocumentoVO> obterListaDocumentosPorFiltro(String nomeFiltro) throws DataBaseException {
      try {
        StringBuilder hql = new StringBuilder();
        
        hql.append(" SELECT documento ");
        hql.append(" FROM DocumentoVO documento ");
        hql.append(" INNER JOIN FETCH documento.grupo grupo ");
        hql.append(" WHERE 1 = 1 ");
        hql.append(" AND upper(documento.nome) like :nome ");
        hql.append(" AND grupo.tipoSolicitacao = :tipoSolicitacao ");
        hql.append(" AND documento.tipoDocumental = :tipoDocumental ");
        
        TypedQuery<DocumentoVO> query = this.getEntityManager().createQuery(hql.toString(), DocumentoVO.class);
        
        query.setParameter("nome", ("%" + nomeFiltro.toUpperCase() + "%"));
        query.setParameter("tipoSolicitacao", TipoSolicitacaoEnum.REMESSA);
        query.setParameter("tipoDocumental", TipoDocumentalEnum.VALOR_PADRAO);
        
        return query.getResultList();
      } catch (Exception e){
        throw new DataBaseException(e.getMessage(), e);
      }
    }

  @Override
  public DocumentoVO buscarMovimentoDiario() throws DataBaseException {
    try {
      StringBuilder hql = new StringBuilder();

      hql.append(" SELECT DISTINCT documento ");
      hql.append(" FROM DocumentoVO documento ");
      hql.append(" INNER JOIN FETCH documento.grupo grupo ");
      hql.append(" WHERE grupo.tipoSolicitacao = :tipoSolicitacao ");
      hql.append(" AND documento.tipoDocumental = :tipoDocumental ");

      TypedQuery<DocumentoVO> query = this.getEntityManager().createQuery(hql.toString(), DocumentoVO.class);

      query.setParameter("tipoSolicitacao", TipoSolicitacaoEnum.REMESSA);
      query.setParameter("tipoDocumental", TipoDocumentalEnum.REMESSA_MOVIMENTO_DIARIO_TIPO_C);

      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (Exception e) {
      throw new DataBaseException(e.getMessage(), e);
    }
  }

}