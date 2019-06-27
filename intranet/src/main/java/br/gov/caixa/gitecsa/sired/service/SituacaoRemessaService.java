package br.gov.caixa.gitecsa.sired.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.SituacaoRemessaDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;

@Stateless(name = "situacaoRemessaRCService")
public class SituacaoRemessaService extends AbstractService<SituacaoRemessaVO> {

    private static final long serialVersionUID = 7989719620382598049L;

    @Inject
    private SituacaoRemessaDAO dao;

    @Override
    protected void validaCamposObrigatorios(SituacaoRemessaVO entity) {

    }

    @Override
    protected void validaRegras(SituacaoRemessaVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(SituacaoRemessaVO entity) {

    }

    @Override
    protected GenericDAO<SituacaoRemessaVO> getDAO() {
        return this.dao;
    }

    public SituacaoRemessaVO findByEnum(SituacaoRemessaEnum situacaoEnum) {
        return this.dao.findById(situacaoEnum.getId());
    }
}
