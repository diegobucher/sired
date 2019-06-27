package br.gov.caixa.gitecsa.service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.FuncionalidadeVO;
import br.gov.caixa.gitecsa.sired.vo.PerfilVO;

@Stateless
public class FuncionalidadeService extends AbstractService<FuncionalidadeVO> {
    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @Inject
    private transient Logger log = Logger.getLogger(FuncionalidadeService.class);

    /**
     * Retorna lista do nome dos perfis de um usuario no sistema pelos grupos retornados do LDAP
     * 
     * @return List<String>
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<String> consultarPerfis(List<String> gruposLdap) throws AppException {

        try {
            Criteria criteria = getSession().createCriteria(PerfilVO.class);
            criteria.add(Restrictions.in("descricao", gruposLdap));
            List<PerfilVO> listaPerfil = criteria.list();
            List<String> listaPerfilStr = new ArrayList<String>();

            for (PerfilVO perfilVO : listaPerfil) {
                listaPerfilStr.add(perfilVO.getDescricao());
            }

            return listaPerfilStr;

        } catch (Exception ej) {

            AppException edb = new AppException("Falha ao acessar os dados. Contate o suporte.");
            log.debug("ERRO - REALIZA ACESSO USUARIO. MENSAGEM: " + ej.getMessage(), ej);
            throw edb;
        }
    }

    /**
     * Retorna lista do nome dos perfis padrao do sistema
     * 
     * @return List<String>
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<String> consultarGruposPadrao() throws AppException {

        List<String> descricaoFuncionalidade = null;

        Criteria criteria = getSession().createCriteria(PerfilVO.class);
        criteria.add(Restrictions.eq("indicadorPadrao", 1));

        List<PerfilVO> listaPerfil = criteria.list();

        if (listaPerfil != null) {
            descricaoFuncionalidade = new ArrayList<String>();
            for (PerfilVO perfil : listaPerfil) {
                descricaoFuncionalidade.add(perfil.getDescricao());
            }
        }

        return descricaoFuncionalidade;
    }

    /**
     * Retorna lista do nome dos perfis padrao do sistema
     * 
     * @return List<String>
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<String> consultarGruposGestor() throws AppException {

        List<String> descricaoFuncionalidade = null;

        Criteria criteria = getSession().createCriteria(PerfilVO.class);
        criteria.add(Restrictions.eq("descricao", "GESTOR").ignoreCase());

        List<PerfilVO> listaPerfil = criteria.list();

        if (listaPerfil != null) {
            descricaoFuncionalidade = new ArrayList<String>();
            for (PerfilVO perfil : listaPerfil) {
                descricaoFuncionalidade.add(perfil.getDescricao());
            }
        }

        return descricaoFuncionalidade;
    }

    /**
     * Retorna lista de Menu do sistema do tipo pai, de acordo com o grupo do usuario informado
     * 
     * @param pGrupoUsuario
     * @return List
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<FuncionalidadeVO> consultarMenuPaiPorGrupo(String pGrupoUsuario) throws AppException {
        List<FuncionalidadeVO> listaMenuVO = null;

        try {

            Criteria criteria = getSession().createCriteria(FuncionalidadeVO.class).createAlias("funcionalidadePai", "pai", JoinType.LEFT_OUTER_JOIN)
                    .createAlias("listaPerfilFuncionalidadeVO", "perfilfunc", JoinType.INNER_JOIN)
                    .createAlias("perfilfunc.perfil", "perfil", JoinType.INNER_JOIN);

            criteria.add(Restrictions.isNull("pai.id"));
            if (!Util.isNullOuVazio(pGrupoUsuario)) {
                criteria.add(Restrictions.in("perfil.descricao", pGrupoUsuario.toUpperCase().split(",")));
            }

            criteria.addOrder(Order.desc("nome"));

            listaMenuVO = ((List<FuncionalidadeVO>) criteria.list());

        } catch (Exception ej) {

            AppException edb = new AppException("Falha ao acessar os dados. Contate o suporte.");

            log.debug("ERRO - REALIZA ACESSO USUARIO. MENSAGEM: " + ej.getMessage(), ej);

            throw edb;
        }

        return listaMenuVO;
    }

    /**
     * Retorna lista de Menu do sistema do tipo pai, de acordo com o tipo de acesso informado
     * 
     * @param tipoAcesso
     * @return List
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<FuncionalidadeVO> consultarMenuPaiPorTipoAcesso(Integer tipoAcesso) throws AppException {
        List<FuncionalidadeVO> listaMenuVO = null;

        try {

            Criteria criteria = getSession().createCriteria(FuncionalidadeVO.class);

            if (!Util.isNullOuVazio(tipoAcesso)) {
                criteria.add(Restrictions.eq("tipoAcesso", tipoAcesso));
            } else {
                return null;
            }

            criteria.addOrder(Order.asc("id"));

            listaMenuVO = ((List<FuncionalidadeVO>) criteria.list());

        } catch (Exception ej) {

            AppException edb = new AppException("Falha ao acessar os dados. Contate o suporte.");

            log.debug("ERRO - REALIZA ACESSO USUARIO. MENSAGEM: " + ej.getMessage(), ej);

            throw edb;
        }

        return listaMenuVO;
    }

    /**
     * Retorna Menu pai de acordo com a pk
     * 
     * @param pCodigoPai
     * @return FuncionalidadeVO
     * @throws AppException
     */
    public FuncionalidadeVO consultarMenuPaiPorPk(Long pCodigoPai) throws AppException {
        FuncionalidadeVO menuVO = null;

        try {
            menuVO = this.getById(pCodigoPai);
        } catch (Exception ej) {

            AppException edb = new AppException("Falha ao acessar os dados. Contate o suporte.");
            log.debug("ERRO - REALIZA ACESSO USUARIO. MENSAGEM: " + ej.getMessage(), ej);
            throw edb;
        }

        return menuVO;
    }

    /**
     * Retorna lista de menu do sistema do tipo filho, de acordo com o grupo do usuario informado
     * 
     * @param pGrupoUsuario
     * @param pCodigoPai
     * @return List
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<FuncionalidadeVO> consultarFuncionalidadeSistemaFilhoPorGrupo(String pGrupoUsuario, Short pCodigoPai) throws AppException {
        List<FuncionalidadeVO> listaMenuVO = null;
        StringBuilder hql = new StringBuilder();

        try {

            hql.append(" SELECT DISTINCT menuFilho FROM FuncionalidadeVO menuFilho");
            hql.append(" JOIN FETCH menuFilho.listaPerfilFuncionalidadeVO  lista");
            hql.append(" JOIN FETCH lista.perfil perfil      ");
            hql.append(" WHERE menuFilho.funcionalidadePai.id = " + pCodigoPai);

            if (!Util.isNullOuVazio(pGrupoUsuario)) {

                pGrupoUsuario = pGrupoUsuario.replace(",", "','");

                pGrupoUsuario = "'" + pGrupoUsuario + "'";

                hql.append(" AND UPPER(perfil.descricao) IN (").append(pGrupoUsuario.toUpperCase()).append(")");

            }

            hql.append(" ORDER BY menuFilho.id ");

            Query query = entityManagerSistema.createQuery(hql.toString(), FuncionalidadeVO.class);

            return query.getResultList();

        } catch (Exception ej) {
            AppException edb = new AppException("Falha ao acessar os dados. Contate o suporte.");
            log.debug("ERRO - REALIZA ACESSO USUARIO. MENSAGEM: " + edb.getMessage(), edb);
        }

        return listaMenuVO;
    }

    @SuppressWarnings("unchecked")
    public List<FuncionalidadeVO> findByPerfil(PerfilVO perfil) {
        Criteria criteria = getSession().createCriteria(FuncionalidadeVO.class).createAlias("funcionalidadePai", "pai", JoinType.LEFT_OUTER_JOIN)
                .createAlias("listaPerfilFuncionalidadeVO", "perfilfunc", JoinType.INNER_JOIN).createAlias("perfilfunc.perfil", "perfil", JoinType.INNER_JOIN);

        criteria.add(Restrictions.eq("perfil.id", perfil.getId()));

        criteria.addOrder(Order.asc("this.nome"));

        return ((List<FuncionalidadeVO>) criteria.list());

    }

    /**
     * Retorna lista de Menu do sistema do tipo pai, de acordo com o grupo do usuario informado
     * 
     * @param pGrupoUsuario
     * @return List
     * @throws AppException
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<FuncionalidadeVO> findAll() {
        List<FuncionalidadeVO> listaMenuVO = null;

        Criteria criteria = getSession().createCriteria(FuncionalidadeVO.class).createAlias("funcionalidadePai", "pai", JoinType.LEFT_OUTER_JOIN);
        // .createAlias("listaPerfilFuncionalidadeVO", "perfilfunc", JoinType.LEFT_OUTER_JOIN)
        // .createAlias("perfilfunc.perfil", "perfil", JoinType.LEFT_OUTER_JOIN);
        criteria.addOrder(Order.desc("nome"));

        listaMenuVO = ((List<FuncionalidadeVO>) criteria.list());

        return listaMenuVO;
    }

    @Override
    protected void validaCampos(FuncionalidadeVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegras(FuncionalidadeVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegrasExcluir(FuncionalidadeVO entity) {
        // TODO Auto-generated method stub

    }

}
