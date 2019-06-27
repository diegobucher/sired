package br.gov.caixa.gitecsa.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.gov.caixa.gitecsa.repository.DataSiicoRepository;
import br.gov.caixa.gitecsa.siico.ViewFeriado;
import br.gov.caixa.gitecsa.siico.ViewMunicipio;
import br.gov.caixa.gitecsa.siico.ViewTipoUnidade;
import br.gov.caixa.gitecsa.siico.ViewUnidade;

@TransactionManagement(TransactionManagementType.BEAN)
@Stateless
public class AcessoSiicoService {

    @Inject
    @DataSiicoRepository
    private EntityManager entityManagerSiico;

    @SuppressWarnings("unchecked")
    public List<ViewTipoUnidade> findAllTipoUnidade() {
        return entityManagerSiico.createQuery(("FROM ViewTipoUnidade")).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<ViewUnidade> findAllUnidade() {
        return entityManagerSiico.createQuery(("FROM ViewUnidade")).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<ViewFeriado> findAllFeriado() {
        return entityManagerSiico.createQuery(("FROM ViewFeriado")).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<ViewMunicipio> findAllMunicipio() {
        return entityManagerSiico.createQuery(("FROM ViewMunicipio")).getResultList();
    }

}
