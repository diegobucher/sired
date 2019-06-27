package br.gov.caixa.gitecsa.sired.dao.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.dao.UnidadeDAO;
import br.gov.caixa.gitecsa.sired.enumerator.AbrangenciaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoRestricaoEnum;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class UnidadeDAOImpl extends GenericDAOImpl<UnidadeVO> implements UnidadeDAO {

    @Override
    public UnidadeVO findByIdEager(Long id) throws DataBaseException {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u ");
        hql.append(" FROM UnidadeVO u ");
        hql.append(" Join Fetch u.tipoUnidade ");
        hql.append(" Where u.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("id", id);

        try {
            return (UnidadeVO) query.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean hasRestricaoUnidadeConfigurada(UnidadeVO unidade) throws DataBaseException {

        StringBuilder hql = new StringBuilder();

        hql.append(" Select count(ug.id) From UnidadeGrupoVO ug ");
        hql.append(" Join ug.unidade u ");
        hql.append(" Where u.id = :unidade ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("unidade", unidade.getId());

        try {
            return (((Long) query.getSingleResult()).longValue() > 0) ? true : false;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean hasPermissaoDocumentoUnidade(DocumentoVO documento, UnidadeVO unidade) throws DataBaseException {

        StringBuilder hql = new StringBuilder();

        hql.append(" Select count(d.id) From DocumentoVO d ");
        hql.append(" Join d.grupo g ");
        hql.append(" Join g.unidadeGrupos ug ");
        hql.append(" Join ug.unidade u ");
        hql.append(" Where d.id = :documento And u.id = :unidade ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("documento", documento.getId());
        query.setParameter("unidade", unidade.getId());

        try {
            return (((Long) query.getSingleResult()).longValue() > 0) ? true : false;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public UnidadeVO findByUnidadeVinculadora(UnidadeVO unidade, UnidadeVO unidadeVinculadora) throws DataBaseException {

        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u ");
        hql.append(" FROM UnidadeVO u ");
        hql.append(" Where u.unidadeVinculadora.id = :idVinculadora AND u.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("idVinculadora", unidadeVinculadora.getId());
        query.setParameter("id", unidade.getId());

        try {
            return (UnidadeVO) query.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public UnidadeVO findByAbrangenciaUf(UnidadeVO unidadeAreaMeio) throws DataBaseException {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u ");
        hql.append(" FROM UnidadeVO u ");
        hql.append(" Join Fetch u.ufs ufs ");
        hql.append(" Where u.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("id", unidadeAreaMeio.getId());

        try {
            return (UnidadeVO) query.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public UnidadeVO findByAbrangenciaCtda(UnidadeVO unidade, UnidadeVO unidadeCtda) throws DataBaseException {

        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u ");
        hql.append(" FROM UnidadeVO u, ViewAbrangenciaVO v ");
        hql.append(" WHERE v.idUnidadeBase = :idUnidadeCtda ");
        hql.append(" AND v.idUnidadeSolicitada = :idUnidadeSolicitada ");
        hql.append(" AND v.abrangencia = :abrangencia ");
        hql.append(" AND u.id = v.idUnidadeSolicitada ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("idUnidadeCtda", unidadeCtda.getId());
        query.setParameter("idUnidadeSolicitada", unidade.getId());
        query.setParameter("abrangencia", AbrangenciaEnum.PERTENCE);

        try {
            return (UnidadeVO) query.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UnidadeVO> findAllComBase() throws DataBaseException {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT unidade FROM UnidadeVO unidade, CTDAVO ctda, CTDAUFVO ufctda, BaseVO base ");
        hql.append(" WHERE base = ctda.base ");
        hql.append(" AND ufctda.ctda = ctda ");
        hql.append(" AND unidade.uf = ufctda.uf ");
        hql.append(" AND unidade.icAtivo = :ativo ");
        hql.append(" ORDER BY unidade.id, unidade.nome ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("ativo", BigDecimal.ONE.intValue());
        query.setParameter("excluir", TipoRestricaoEnum.EXCLUIR.getValor());
        
        try {
        	return query.getResultList();
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UnidadeVO> findAllByUnidadeVinculadora(UnidadeVO unidade) throws DataBaseException {

        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u FROM UnidadeVO u ");
        hql.append(" Where u.unidadeVinculadora.id = :idVinculadora or u.id = :idUnidade ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("idVinculadora", unidade.getId());
        query.setParameter("idUnidade", unidade.getId());
        
        try {
        	return query.getResultList();
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public void update(List<UnidadeVO> listUnidade) {
        int contador = 1;
        for (UnidadeVO entity : listUnidade) {
            this.getEntityManager().merge(entity);

            if (contador % 100 == 0) {
                this.getEntityManager().flush();
                this.getEntityManager().clear();
            }
            contador++;
        }
    }

    @Override
    public void persist(List<UnidadeVO> listUnidade) {
        int contador = 1;
        for (UnidadeVO entity : listUnidade) {
            this.getEntityManager().persist(entity);

            if (contador % 100 == 0) {
                this.getEntityManager().flush();
                this.getEntityManager().clear();
            }
            contador++;
        }
    }

    @Override
    public List<UnidadeVO> findAllByEager() throws DataBaseException {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u ");
        hql.append(" FROM UnidadeVO u ");
        hql.append(" Left Join Fetch u.unidadeGrupos ug ");

        TypedQuery<UnidadeVO> query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);

        try {
            return query.getResultList();
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean hasRestricaoUnidadeGrupoConfigurada(UnidadeVO unidade) throws DataBaseException {

        StringBuilder hql = new StringBuilder();

        hql.append(" Select count(ug.id) From UnidadeGrupoVO ug ");
        hql.append(" Join ug.unidade u ");
        hql.append(" Where u.id = :unidade ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("unidade", unidade.getId());

        try {
            return (((Long) query.getSingleResult()).longValue() > 0) ? true : false;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

}
