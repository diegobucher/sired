package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.enumerator.AbrangenciaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoRestricaoEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.UnidadeDAO;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

public class UnidadeDAOImpl extends GenericDAOImpl<UnidadeVO> implements UnidadeDAO {

    @Override
    public UnidadeVO findByIdEager(Long id) {
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
        }
    }

    /**
     * Lista todas as unidades permitidas para o perfil do usuário
     * 
     * @param usuario
     * @return A lista de unidades permitidas para o usuário
     */
    public List<UnidadeVO> findAllByPerfil(UsuarioLdap usuario) {

        // Gestores e Auditores: podem ver dados de qualquer unidade desde que relacionadas à uma base
        if (JSFUtil.isPerfil(Constantes.PERFIL_GESTOR) || JSFUtil.isPerfil(Constantes.PERFIL_AUDITOR)) {
            return findAllComBase();
        }

        // Os demais usuários podem ver dados das unidade subordinadas à sua unidade de lotação
        UnidadeVO unidadeUsuario = new UnidadeVO();
        unidadeUsuario.setId(usuario.getCoUnidade().longValue());

        return findAllByUnidadeVinculadora(unidadeUsuario);
    }

    @SuppressWarnings("unchecked")
    public List<UnidadeVO> findAllComBase() {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT unidade FROM UnidadeVO unidade, CTDAVO ctda, CTDAUFVO ufctda, BaseVO base ");
        hql.append(" WHERE base = ctda.base ");
        hql.append(" AND ufctda.ctda = ctda ");
        hql.append(" AND unidade.uf = ufctda.uf ");
        hql.append(" AND unidade.icAtivo = :ativo ");
        // hql.append(" AND unidade NOT IN (SELECT rest.unidade FROM CTDAUFRestricaoVO rest WHERE rest.ctdaUF = ufctda AND rest.tipoResticao = :excluir)");
        hql.append(" ORDER BY unidade.id, unidade.nome ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("ativo", BigDecimal.ONE.intValue());
        query.setParameter("excluir", TipoRestricaoEnum.EXCLUIR.getValor());

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<UnidadeVO> findAllByUnidadeVinculadora(UnidadeVO unidade) {

        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u FROM UnidadeVO u ");
        hql.append(" Where u.unidadeVinculadora.id = :idVinculadora ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("idVinculadora", unidade.getId());

        return query.getResultList();
    }

    @Override
    public UnidadeVO findByUnidadeVinculadora(UnidadeVO unidade, UnidadeVO unidadeVinculadora) {

        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u ");
        hql.append(" FROM UnidadeVO u ");
        hql.append(" Where u.unidadeVinculadora.id = :idVinculadora AND u.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("idVinculadora", unidadeVinculadora.getId());
        query.setParameter("id", unidade.getId());

        try {
            return (UnidadeVO) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public UnidadeVO findByAbrangenciaUf(UnidadeVO unidadeAreaMeio) {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT u ");
        hql.append(" FROM UnidadeVO u ");
        hql.append(" Join Fetch u.ufs ufs ");
        hql.append(" Where u.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        query.setParameter("id", unidadeAreaMeio.getId());

        try {
            return (UnidadeVO) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public UnidadeVO findByAbrangenciaCtda(UnidadeVO unidade, UnidadeVO unidadeCtda) {

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
        } catch (Exception e) {
            return null;
        }
    }

}
