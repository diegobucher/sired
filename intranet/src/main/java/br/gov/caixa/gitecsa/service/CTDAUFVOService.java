package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import org.hibernate.Hibernate;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.CTDAUFVO;

@Stateless
public class CTDAUFVOService extends AbstractService<CTDAUFVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(CTDAUFVO entity) {
    }

    @Override
    protected void validaRegras(CTDAUFVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(CTDAUFVO entity) {
    }

    @SuppressWarnings("unchecked")
    public List<CTDAUFVO> buscarPorCTDAUFVO(CTDAUFVO entity) {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT ctda FROM CTDAUFVO ctda ");
        hql.append(" JOIN FETCH ctda.uf uf ");
        hql.append(" JOIN FETCH ctda.ctda ctdac ");
        hql.append(" JOIN FETCH ctdac.base ");
        hql.append(" WHERE ctdac = :ctda ");

        Query query = getEntityManager().createQuery(hql.toString(), CTDAUFVO.class);

        query.setParameter("ctda", entity.getCtda());

        return query.getResultList();
    }

    @Override
    public List<CTDAUFVO> findByParameters(CTDAUFVO object) throws AppException {
        List<CTDAUFVO> lista = super.findByParameters(object);
        for (CTDAUFVO ctdaufVO : lista) {
            Hibernate.initialize(ctdaufVO.getCtdaUFRestricoes());
        }
        return lista;
    }

}
