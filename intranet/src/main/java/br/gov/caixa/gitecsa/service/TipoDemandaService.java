package br.gov.caixa.gitecsa.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

@Stateless
public class TipoDemandaService extends AbstractService<TipoDemandaVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @Inject
    private GrupoService serviceGrupo;

    @Override
    protected void validaCampos(TipoDemandaVO entity) {
    }

    @Override
    protected void validaRegras(TipoDemandaVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(TipoDemandaVO entity) {
    }

    @SuppressWarnings("unchecked")
    public List<String> consultaDescricaoTiposDemanda() throws Exception {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT DISTINCT demanda.nome FROM TipoDemandaVO demanda ORDER BY demanda.nome ");
        Query query = entityManagerSistema.createQuery(hql.toString(), String.class);
        List<String> operacoes = query.getResultList();
        return operacoes;
    }

}
