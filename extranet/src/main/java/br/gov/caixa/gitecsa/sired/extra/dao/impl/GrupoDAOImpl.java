package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.extra.dao.GrupoDAO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

public class GrupoDAOImpl extends GenericDAOImpl<GrupoVO> implements GrupoDAO {

    public GrupoVO getById(Object id) {
        Criteria criteria = getSession().createCriteria(GrupoVO.class).createAlias("grupoCampos", "campos", JoinType.LEFT_OUTER_JOIN);

        criteria.add(Restrictions.eq("id", id));

        criteria.addOrder(Order.desc("nome"));

        GrupoVO obj = (GrupoVO) criteria.uniqueResult();

        if (obj != null) {
            obj.getGrupoCampos().toArray();
        }

        return obj;
    }

    public GrupoVO obterGrupo(DocumentoVO documento) {
        Criteria criteria = getSession().createCriteria(GrupoVO.class).createAlias("documentos", "docs", JoinType.INNER_JOIN);

        criteria.add(Restrictions.eq("docs.id", documento.getId()));

        GrupoVO obj = (GrupoVO) criteria.uniqueResult();

        if (obj != null) {
            List<GrupoCampoVO> list = new ArrayList<GrupoCampoVO>(obj.getGrupoCampos());

            Collections.sort(list, new Comparator<GrupoCampoVO>() {
                @Override
                public int compare(GrupoCampoVO arquivo1, GrupoCampoVO arquivo2) {

                    if (ObjectUtils.isNullOrEmpty(arquivo1.getOrdem())) {
                        arquivo1.setOrdem(50);
                    }
                    if (ObjectUtils.isNullOrEmpty(arquivo2.getOrdem())) {
                        arquivo2.setOrdem(50);
                    }

                    return arquivo1.getOrdem().compareTo(arquivo2.getOrdem());
                }

            });

            obj.setGrupoCampos(new HashSet<GrupoCampoVO>(list));
        }

        return obj;
    }

    @SuppressWarnings("unchecked")
    public List<TipoDemandaVO> obterDemandas() {
        Criteria criteria = getSession().createCriteria(TipoDemandaVO.class);

        return criteria.list();
    }

}
