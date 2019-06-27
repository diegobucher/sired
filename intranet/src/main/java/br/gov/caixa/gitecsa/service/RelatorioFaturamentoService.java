package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.gov.caixa.gitecsa.dto.RelatorioFaturamentoDTO;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.util.FilterVisitor;

@Stateless
public class RelatorioFaturamentoService implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    @DataRepository
    protected EntityManager entityManager;

    public List<RelatorioFaturamentoDTO> consultaRelatorio(FilterVisitor visitor) {
        return null;
    }

}
