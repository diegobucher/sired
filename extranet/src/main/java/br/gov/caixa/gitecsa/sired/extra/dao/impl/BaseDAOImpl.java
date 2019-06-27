package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.TipoRestricaoEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.BaseDAO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.LoteSequenciaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

public class BaseDAOImpl extends GenericDAOImpl<BaseVO> implements BaseDAO {

    @SuppressWarnings("unchecked")
    @Override
    public List<BaseVO> findAll() {
        Criteria criteria = getSession().createCriteria(BaseVO.class).createAlias("loteSequencia", "loteSequencia", JoinType.LEFT_OUTER_JOIN);

        criteria.addOrder(Order.asc("nome"));

        return mountBase(((List<BaseVO>) criteria.list()));
    }

    private List<BaseVO> mountBase(List<BaseVO> list) {
        Map<Long, BaseVO> map = new HashMap<Long, BaseVO>();

        List<BaseVO> listaNova = new ArrayList<BaseVO>();

        for (BaseVO baseVO : list) {
            if (!map.containsKey(baseVO.getId())) {
                map.put((Long) baseVO.getId(), baseVO);
                listaNova.add(baseVO);
            }
        }

        return listaNova;
    }

    @SuppressWarnings("unchecked")
    public List<BaseVO> consultaBasePorIdUnidade(long idUnidade) {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT base FROM BaseVO base, CTDAVO ctda, CTDAUFVO ufctda, UnidadeVO unidade ");
        hql.append(" WHERE base = ctda.base");
        hql.append(" AND ufctda.ctda = ctda");
        hql.append(" AND unidade.uf = ufctda.uf");
        hql.append(" AND unidade.id = :idUnidade");
        hql.append(" AND unidade NOT IN ( SELECT rest.unidade FROM CTDAUFRestricaoVO rest ");
        hql.append(" WHERE rest.ctdaUF = ufctda AND rest.tipoResticao = " + TipoRestricaoEnum.EXCLUIR.getValor() + " )");

        Query query = getEntityManager().createQuery(hql.toString(), BaseVO.class);
        query.setParameter("idUnidade", idUnidade);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LoteSequenciaVO> loteSequenciaVinculado(String base, UnidadeVO unidadeVO) throws AppException {
        List<BaseVO> bases = consultaBasePorIdUnidade((Integer) unidadeVO.getId());
        BaseVO baseVO = bases.get(0);

        Criteria criteria = getSession().createCriteria(LoteSequenciaVO.class);
        criteria.createAlias("bases", "bas", JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("bas.id", baseVO.getId()));
        criteria.add(Restrictions.eq("id", base));

        return (List<LoteSequenciaVO>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public BaseVO getBaseByUnidade(UnidadeVO unidadeVO) {
        Criteria criteria = getSession().createCriteria(BaseVO.class).createAlias("loteSequencia", "loteSequencia", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("unidade", "unidade", JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("unidade.id", unidadeVO.getId()));
        List<BaseVO> list = mountBase((List<BaseVO>) criteria.list());

        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public List<BaseVO> consultaBasesEmpresaContrato() {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT empresaContrato FROM EmpresaVO empresa ");
        hql.append(" JOIN empresa.empresaContratos empresaContrato join fetch empresaContrato.base base ");
        hql.append(" WHERE empresa.cnpj = :pCnpj");

        Query query = getEntityManager().createQuery(hql.toString(), EmpresaContratoVO.class);
        query.setParameter("pCnpj", JSFUtil.getUsuario().getNuCnpj());

        List<EmpresaContratoVO> contratos = query.getResultList();

        List<BaseVO> bases = new ArrayList<BaseVO>();

        for (EmpresaContratoVO contrato : contratos) {
            bases.add(contrato.getBase());
        }

        return bases;
    }

    @SuppressWarnings("unchecked")
    public List<EmpresaContratoVO> relacionamentoEmpresaContrato(Long idBase) throws AppException {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT empresaContrato FROM EmpresaVO empresa ");
        hql.append(" WHERE empresa.base.id = :id");

        Query query = getEntityManager().createQuery(hql.toString(), EmpresaContratoVO.class);
        query.setParameter("id", idBase);

        List<EmpresaContratoVO> lista = (List<EmpresaContratoVO>) query.getResultList();

        return lista;
    }
}
