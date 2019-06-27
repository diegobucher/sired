package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.CTDAUFRestricaoVO;

@Stateless
public class CTDAUFRestricaoService extends AbstractService<CTDAUFRestricaoVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(CTDAUFRestricaoVO entity) {
    }

    @Override
    protected void validaRegras(CTDAUFRestricaoVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(CTDAUFRestricaoVO entity) {
    }

    @SuppressWarnings("unchecked")
    public List<CTDAUFRestricaoVO> findAllEager() {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT c FROM CTDAUFRestricaoVO c ");
        hql.append(" Join Fetch c.ctdaUF ctda ");
        hql.append(" Join Fetch ctda.uf ");
        hql.append(" Join Fetch c.unidade ");
        Query query = getEntityManager().createQuery(hql.toString(), CTDAUFRestricaoVO.class);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<CTDAUFRestricaoVO> buscarPorCTDAUFRestricao(CTDAUFRestricaoVO entity) {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT c FROM CTDAUFRestricaoVO c ");
        hql.append(" Join Fetch c.ctdaUF ctdaUF ");
        hql.append(" Join Fetch ctdaUF.uf ");
        hql.append(" Join Fetch c.unidade ");
        hql.append(" Where ctdaUF.ctda.id = :ctdaUF ");

        Query query = getEntityManager().createQuery(hql.toString(), CTDAUFRestricaoVO.class);

        query.setParameter("ctdaUF", entity.getCtdaUF().getCtda().getId());

        return query.getResultList();
    }
}
