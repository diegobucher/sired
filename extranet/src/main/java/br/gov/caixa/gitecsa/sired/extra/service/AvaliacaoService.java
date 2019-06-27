package br.gov.caixa.gitecsa.sired.extra.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.extra.dao.AvaliacaoDAO;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;

@Stateless
public class AvaliacaoService extends AbstractService<AvaliacaoRequisicaoVO> {

    private static final long serialVersionUID = -880302247729514482L;

    @Inject
    private AvaliacaoDAO avaliacaoDAO;

    public AvaliacaoRequisicaoVO obterPorRequisicao(Long idRequisicao) {
        return avaliacaoDAO.obterPorRequisicao(idRequisicao);
    }

    @Override
    protected void validaRegras(AvaliacaoRequisicaoVO entity) {
        // do nothing
    }

    @Override
    protected void validaRegrasExcluir(AvaliacaoRequisicaoVO entity) {
        // do nothing
    }

    @Override
    protected void validaCamposObrigatorios(AvaliacaoRequisicaoVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected GenericDAO<AvaliacaoRequisicaoVO> getDAO() {
        return avaliacaoDAO;
    }

}
