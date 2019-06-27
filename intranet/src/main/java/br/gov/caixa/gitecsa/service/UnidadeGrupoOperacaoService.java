package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.UnidadeGrupoOperacaoVO;

@Stateless
public class UnidadeGrupoOperacaoService extends AbstractService<UnidadeGrupoOperacaoVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(UnidadeGrupoOperacaoVO entity) {
    }

    @Override
    protected void validaRegras(UnidadeGrupoOperacaoVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(UnidadeGrupoOperacaoVO entity) {
    }
    
    public List<UnidadeGrupoOperacaoVO> findEager(UnidadeGrupoOperacaoVO obj) {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT ugo FROM UnidadeGrupoOperacaoVO ugo ");
        hql.append(" JOIN FETCH ugo.unidadeGrupo ug ");
        hql.append(" JOIN FETCH ug.unidade ");
        hql.append(" JOIN FETCH ug.grupo ");
        hql.append(" JOIN FETCH ugo.operacao op ");
        hql.append(" WHERE ug.unidade.id = :id ");
        
        TypedQuery<UnidadeGrupoOperacaoVO> query = getEntityManager().createQuery(hql.toString(), UnidadeGrupoOperacaoVO.class);
        query.setParameter("id", obj.getUnidadeGrupo().getUnidade().getId());

        return query.getResultList();
    }
}
