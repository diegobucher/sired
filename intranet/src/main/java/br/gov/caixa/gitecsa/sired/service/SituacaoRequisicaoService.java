package br.gov.caixa.gitecsa.sired.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.SituacaoRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;

@Stateless(name = "situacaoRequisicaoService")
public class SituacaoRequisicaoService extends AbstractService<SituacaoRequisicaoVO> {

    private static final long serialVersionUID = 7989719620382598049L;

    @Inject
    private SituacaoRequisicaoDAO dao;

    @Override
    protected void validaCamposObrigatorios(SituacaoRequisicaoVO entity) {

    }

    @Override
    protected void validaRegras(SituacaoRequisicaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(SituacaoRequisicaoVO entity) {

    }

    @Override
    protected GenericDAO<SituacaoRequisicaoVO> getDAO() {
        return this.dao;
    }

    public SituacaoRequisicaoVO findByEnum(SituacaoRequisicaoEnum situacaoEnum) {
        return this.dao.findById(situacaoEnum.getId());
    }
}
