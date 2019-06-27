package br.gov.caixa.gitecsa.sired.dao.impl;

import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.BaseDAO;
import br.gov.caixa.gitecsa.sired.enumerator.AbrangenciaEnum;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class BaseDAOImpl extends GenericDAOImpl<BaseVO> implements BaseDAO {

    public BaseVO findByLoteSequenciaEUnidade(String numLoteSequencia, UnidadeVO unidade) {

        StringBuilder hql = new StringBuilder();
        hql.append(" Select b From BaseVO b, ViewAbrangenciaVO v ");
        hql.append(" Left Join Fetch b.loteSequencia l ");
        hql.append(" Where v.idUnidadeBase = :idUnidade ");
        hql.append(" And l.id = :idLote ");
        hql.append(" And v.abrangencia = :abrangencia ");
        hql.append(" And b.id = v.idBase ");

        Query query = getEntityManager().createQuery(hql.toString(), BaseVO.class);
        query.setParameter("idUnidade", unidade.getId());
        query.setParameter("idLote", numLoteSequencia);
        query.setParameter("abrangencia", AbrangenciaEnum.PERTENCE);

        try {
            return (BaseVO) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
