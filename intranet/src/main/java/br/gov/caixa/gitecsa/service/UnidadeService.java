package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.TipoRestricaoEnum;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class UnidadeService extends AbstractService<UnidadeVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    /*
     * @Inject
     * 
     * @DataRepository private EntityManager entityManagerSistema;
     */

    @Inject
    BaseService baseService;

    @Inject
    TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;

    @Override
    protected void validaCampos(UnidadeVO entity) {

    }

    @Override
    protected void validaRegras(UnidadeVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(UnidadeVO entity) {

    }

    @Override
    public List<UnidadeVO> findAll() {
        try {

            StringBuilder hql = new StringBuilder();

            hql.append("select distinct u from UnidadeVO u ");
            hql.append("left join fetch u.unidadeGrupos ug ");
            hql.append("left join fetch u.ufs uf ");
            hql.append("left join fetch ug.operacao o ");
            hql.append("join fetch ug.grupo g ");
            hql.append("where ug.unidade is not null or uf.unidades is not empty");

            TypedQuery<UnidadeVO> query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);

            return query.getResultList();

        } catch (Exception e) {
            if (mensagens == null || mensagens.isEmpty()) {
                mensagens = new ArrayList<String>();
            }
            mensagens.add(MensagemUtils.obterMensagem("MA012"));
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "Unidade", "findAll"));
        }
        return null;

    }
    
    public List<UnidadeVO> findById(Long id) {
        try {

            StringBuilder hql = new StringBuilder();

            hql.append("select distinct u from UnidadeVO u ");
            hql.append("left join fetch u.unidadeGrupos ug ");
            hql.append("left join fetch u.ufs uf ");
            hql.append("left join fetch u.municipio m ");
            hql.append("left join fetch u.tipoUnidade tu ");
            hql.append("left join fetch u.unidadeVinculadora uv ");
            hql.append("left join fetch ug.operacao o ");
            hql.append("join fetch ug.grupo g ");
            hql.append("where u.id = :id");

            TypedQuery<UnidadeVO> query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
            query.setParameter("id", id);

            return query.getResultList();

        } catch (Exception e) {
            if (mensagens == null || mensagens.isEmpty()) {
                mensagens = new ArrayList<String>();
            }
            mensagens.add(MensagemUtils.obterMensagem("MA012"));
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "Unidade", "findAll"));
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    public List<UnidadeVO> unidadesAtivas() {

        Criteria criteria = getSession().createCriteria(UnidadeVO.class);

        criteria.add(Restrictions.eq("icAtivo", new Integer(1)));

        return (List<UnidadeVO>) criteria.list();

    }

    @SuppressWarnings("unchecked")
    public List<UnidadeVO> findByGrupo(GrupoVO grupo) throws AppException {

        Criteria criteria = getSession().createCriteria(UnidadeVO.class).createAlias("unidadeGrupos", "grupos", JoinType.INNER_JOIN)
                .createAlias("grupos.grupo", "grupo", JoinType.INNER_JOIN);

        criteria.add(Restrictions.eq("grupo.id", grupo.getId()));

        return (List<UnidadeVO>) criteria.list();

    }

    @Override
    public UnidadeVO getById(Object id) {
        try {

            StringBuilder hql = new StringBuilder();

            hql.append("select u from UnidadeVO u ");
            hql.append("left join fetch u.unidadeGrupos ug ");
            hql.append("left join fetch ug.grupo g ");
            hql.append("left join fetch u.uf uf ");
            hql.append("left join fetch u.municipio m ");
            hql.append("left join fetch u.ufs ufs ");
            hql.append("left join fetch u.tipoUnidade tu ");
            hql.append("left join fetch u.unidadeVinculadora uv ");
            hql.append("where u.id = :id");

            TypedQuery<UnidadeVO> query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);

            query.setParameter("id", id);

            try {
                return query.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }

        } catch (Exception e) {

            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "Unidade", "getById"));

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<DocumentoVO> findAssociacaoBase() {
        Criteria criteria = getSession().createCriteria(DocumentoVO.class).createAlias("grupos", "grupo", JoinType.INNER_JOIN);

        criteria.addOrder(Order.desc("nome"));

        return ((List<DocumentoVO>) criteria.list());

    }

    /**
     * Consulta os documentos associados a uma UnidadeGrupo. Implementação da RN014.
     * 
     * @param idUnidade
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<DocumentoVO> consultaDocumentoPorUnidadeGrupo(Long idUnidade) throws Exception {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT documento FROM DocumentoVO documento ");
        hql.append(" JOIN documento.grupo gp ");
        hql.append(" JOIN gp.unidadeGrupos unidadeGrupo ");
        hql.append(" JOIN unidadeGrupo.unidade unidade ");
        hql.append(" WHERE unidade.id = " + idUnidade);

        Query query = getEntityManager().createQuery(hql.toString(), DocumentoVO.class);

        List<DocumentoVO> documentos = query.getResultList();

        return documentos;
    }

    /**
     * Consulta as unidades de determinada base.
     * 
     * @param base
     * @return
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<UnidadeVO> consultaUnidadePorBase(BaseVO base) throws AppException {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT unidade FROM UnidadeVO unidade, CTDAVO ctda, CTDAUFVO ufctda, BaseVO base  ");
        hql.append(" WHERE base = ctda.base");
        hql.append(" AND ufctda.ctda = ctda");
        hql.append(" AND unidade.uf = ufctda.uf");
        hql.append(" AND base.id = " + base.getId().toString());
        hql.append(" AND unidade.icAtivo = " + new Integer(1));
        hql.append(" AND unidade NOT IN ( SELECT rest.unidade FROM CTDAUFRestricaoVO rest ");
        hql.append(" WHERE rest.ctdaUF = ufctda AND rest.tipoResticao = " + TipoRestricaoEnum.EXCLUIR.getValor() + " )");
        hql.append(" ORDER BY unidade.id, unidade.nome ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);

        return query.getResultList();

    }

    /**
     * Consulta todas unidades vinculadas a alguma base.
     * 
     * @param base
     * @return
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<UnidadeVO> consultaUnidadesComBase() throws AppException {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT unidade FROM UnidadeVO unidade, CTDAVO ctda, CTDAUFVO ufctda, BaseVO base ");
        hql.append(" WHERE base = ctda.base");
        hql.append(" AND ufctda.ctda = ctda");
        hql.append(" AND unidade.uf = ufctda.uf");
        hql.append(" AND unidade.icAtivo = " + new Integer(1));
        hql.append(" AND unidade NOT IN ( SELECT rest.unidade FROM CTDAUFRestricaoVO rest ");
        hql.append(" WHERE rest.ctdaUF = ufctda AND rest.tipoResticao = " + TipoRestricaoEnum.EXCLUIR.getValor() + " )");
        hql.append(" ORDER BY unidade.id, unidade.nome ");

        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);

        return query.getResultList();

    }

    /**
     * Consulta a lista de unidades que o usuário logado possui acesso de acordo com o seu perfil. Esse método implementa a RN017 para a
     * consulta de requisições.
     * 
     * @param usuario
     * @return
     */
    public List<UnidadeVO> consultarUnidadesAutorizadasPorPerfil(UsuarioLdap usuario) {

        List<UnidadeVO> unidadesAutorizadas = new ArrayList<UnidadeVO>();

        try {
            /*
             * Se usuário é GESTOR ou AUDITOR, então deve poder acessar qualquer unidade vinculada a alguma Base.
             */
            if (JSFUtil.isPerfil("GEST") || JSFUtil.isPerfil("ADT")) {
                return consultaUnidadesComBase();
            } else if (JSFUtil.isPerfil("ADM") || JSFUtil.isPerfil("SUP")) {
                /*
                 * Se o usuário é ADMINISTRADOR ou SUPORTE, ele só visualiza requisições e remessas da Base da sua unidade de lotação.
                 */
                List<BaseVO> basesUnidade = tramiteRequisicaoDocumentoService.consultaBasePorIdUnidade(usuario.getCoUnidade());
                if (!Util.isNullOuVazio(basesUnidade)) {
                    return consultaUnidadePorBase(basesUnidade.get(0));
                } else {
                    // Unidade não possui base vinculada.
                    return null;
                }
            } else {
                UnidadeVO unidadeLotacao = null;
                UnidadeVO filtro = new UnidadeVO();
                filtro.setId(usuario.getCoUnidade().longValue());
                unidadeLotacao = carregarLazyPropertiesUnidade(filtro);
                if (JSFUtil.isPerfil("GER")) {
                    /*
                     * RN017 - Gerente (sigla GER) - o sistema deve permitir a visualização e reabertura das requisições e remessas de
                     * documentos de todos os usuários da Unidade, bem como das respectivas Unidades subordinadas
                     */
                    unidadesAutorizadas = consultarUnidadesSubordinadas(unidadeLotacao);
                } else {
                    unidadesAutorizadas.add(unidadeLotacao);
                }
                Collections.sort(unidadesAutorizadas);
                return unidadesAutorizadas;
            }

        } catch (Exception e) {
            if (mensagens == null || mensagens.isEmpty()) {
                mensagens = new ArrayList<String>();
            }
            mensagens.add(MensagemUtils.obterMensagem("MA012"));
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "Unidade", "consultarUnidadesAutorizadasPorPerfil"));
        }

        return null;
    }

    /**
     * Consulta a unidade do parâmetro na base de dados e carrega a lista de UFs associadas.
     */
    @SuppressWarnings("unchecked")
    public UnidadeVO carregarLazyPropertiesUnidade(UnidadeVO unidade) {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT unidade FROM UnidadeVO unidade ");
        hql.append(" join fetch unidade.unidadeVinculadora ");
        hql.append(" join fetch unidade.tipoUnidade ");
        hql.append(" WHERE unidade.icAtivo = " + new Integer(1));
        hql.append(" AND unidade.id = " + unidade.getId());
        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);
        List<UnidadeVO> unidades = query.getResultList();

        if (!Util.isNullOuVazio(unidades)) {
            for (UnidadeVO un : unidades) {
                Hibernate.initialize(un.getUfs());
            }
        }

        return (!Util.isNullOuVazio(unidades)) ? unidades.get(0) : null;
    }

    /**
     * Consulta as unidades subordinadas a unidade do parâmetro.
     */
    @SuppressWarnings("unchecked")
    public List<UnidadeVO> consultarUnidadesSubordinadas(UnidadeVO unidade) {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT unidade FROM UnidadeVO unidade ");
        hql.append(" WHERE unidade.icAtivo = " + new Integer(1));
        hql.append(" AND (unidade.id = " + unidade.getId() + " OR unidade.unidadeVinculadora.id = " + unidade.getId() + ")");
        hql.append(" ORDER BY unidade.id ");
        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);

        return query.getResultList();

    }

    /**
     * Consulta as unidades da abrangência de uma área meio. Apenas as unidades que são do tipo PV, ou seja, não são área-meio. Esse metodo
     * é usado para identificar as unidades autorizadas para a unidade geradora da requisição.
     */
    @SuppressWarnings("unchecked")
    public List<UnidadeVO> consultarUnidadesPorAbrangencia(UnidadeVO unidade) {

        StringBuilder hql = new StringBuilder();

        StringBuilder filtro = new StringBuilder();
        for (UFVO uf : unidade.getUfs()) {
            filtro.append("'" + (String) uf.getId().toString().trim() + "',");
        }

        filtro.deleteCharAt(filtro.length() - 1);

        hql.append(" SELECT unidade FROM UnidadeVO unidade ");
        hql.append(" INNER JOIN unidade.tipoUnidade tp ");
        hql.append(" WHERE unidade.icAtivo = " + new Integer(1));
        hql.append(" AND tp.indicadorUnidade = 'PV'");
        hql.append(" AND unidade.uf.id IN (" + filtro + ")");
        hql.append(" ORDER BY unidade.id ");
        Query query = getEntityManager().createQuery(hql.toString(), UnidadeVO.class);

        return query.getResultList();

    }

}
