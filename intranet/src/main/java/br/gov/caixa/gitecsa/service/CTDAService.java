package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.CTDAVO;

@Stateless
public class CTDAService extends AbstractService<CTDAVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(CTDAVO entity) {
    }

    @Override
    protected void validaRegras(CTDAVO entity) {
        duplicidade(entity);
    }

    private void duplicidade(CTDAVO entity) {
        List<CTDAVO> lista = null;
        try {
            lista = findAll();

            for (CTDAVO item : lista) {
                if (entity.getId() == null) {
                    if (item.getNome().equalsIgnoreCase(entity.getNome().trim())) {
                        mensagens.add(MensagemUtils.obterMensagem("MI018"));
                        break;
                    }
                } else {
                    if ((!item.getId().equals(entity.getId())) && item.getNome().trim().equalsIgnoreCase(entity.getNome().trim())) {
                        mensagens.add(MensagemUtils.obterMensagem("MI018"));
                        break;
                    }

                }
            }
        } catch (Exception e) {
            return;
        }
    }

    @Override
    protected void validaRegrasExcluir(CTDAVO entity) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CTDAVO> findAll() {
        try {

            Criteria criteria = getSession().createCriteria(CTDAVO.class).createAlias("ctdaUF", "ctdaUF", JoinType.LEFT_OUTER_JOIN)
                    .createAlias("ctdaUF.uf", "uf", JoinType.LEFT_OUTER_JOIN)
                    .createAlias("ctdaUF.ctdaUFRestricoes", "ctdaUFRestricoes", JoinType.LEFT_OUTER_JOIN).createAlias("base", "base", JoinType.INNER_JOIN)
                    .createAlias("base.unidade", "unidade", JoinType.INNER_JOIN).createAlias("unidade.uf", "ufBase", JoinType.INNER_JOIN)
                    .createAlias("municipio", "municipio", JoinType.INNER_JOIN);

            criteria.addOrder(Order.desc("base.nome"));
            criteria.addOrder(Order.desc("uf.nome"));

            return mount((List<CTDAVO>) criteria.list());

        } catch (Exception e) {
            if (mensagens == null || mensagens.isEmpty()) {
                mensagens = new ArrayList<String>();
            }
            mensagens.add(MensagemUtils.obterMensagem("MA012"));
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "CTDA", "findAll"));
        }
        return null;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<CTDAVO> mount(List<CTDAVO> list) {
        Map<Long, CTDAVO> map = new HashMap<Long, CTDAVO>();
        for (CTDAVO obj : list) {
            Collections.sort(new ArrayList(obj.getCtdaUF()));
            if (!map.containsKey(obj.getId())) {
                map.put((Long) obj.getId(), obj);
            }
        }

        return (new ArrayList<CTDAVO>(map.values()));

    }

    public CTDAVO buscarPorCTDAVO(CTDAVO entity) {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT c FROM CTDAVO c ");
        hql.append(" Join Fetch c.base ");
        hql.append(" Join Fetch c.municipio ");
        hql.append(" Where c.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), CTDAVO.class);

        query.setParameter("id", entity.getId());

        try {
            return (CTDAVO) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public void delete(CTDAVO entity) throws AppException {

        StringBuilder hql = new StringBuilder();

        hql.append(" DELETE FROM CTDAVO c ");
        hql.append(" Where c.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString());

        query.setParameter("id", entity.getId());

        query.executeUpdate();

    }

    @Override
    public CTDAVO getById(Object id) {
        try {

            StringBuilder hql = new StringBuilder();

            hql.append("select c from CTDAVO c ");
            hql.append("left join fetch c.ctdaUF cUF ");
            hql.append("left join fetch cUF.ctdaUFRestricoes cUFR ");
            hql.append("left join fetch c.base base ");
            hql.append("left join fetch base.unidade ");
            hql.append("where c.id = :id");

            TypedQuery<CTDAVO> query = getEntityManager().createQuery(hql.toString(), CTDAVO.class);
            query.setParameter("id", id);

            try {
                return query.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }

        } catch (Exception e) {

            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "CTDA", "getById"));

            return null;
        }
    }

}
