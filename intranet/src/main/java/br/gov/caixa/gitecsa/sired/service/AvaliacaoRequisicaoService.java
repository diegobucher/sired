package br.gov.caixa.gitecsa.sired.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.AvaliacaoRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

@Stateless(name = "avaliacaoReqService")
public class AvaliacaoRequisicaoService extends AbstractService<AvaliacaoRequisicaoVO> {

    private static final long serialVersionUID = -6426623835947617362L;

    @Inject
    private AvaliacaoRequisicaoDAO dao;

    @Override
    protected void validaCamposObrigatorios(AvaliacaoRequisicaoVO entity) {

    }

    @Override
    protected void validaRegras(AvaliacaoRequisicaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(AvaliacaoRequisicaoVO entity) {

    }

    @Override
    protected GenericDAO<AvaliacaoRequisicaoVO> getDAO() {
        return this.dao;
    }

    public AvaliacaoRequisicaoVO findByRequisicao(RequisicaoVO requisicao) {
        return this.dao.findByRequisicao(requisicao);
    }

    public Boolean hasAvaliacao(RequisicaoVO requisicao) {
        return this.dao.hasAvaliacao(requisicao);
    }

}
