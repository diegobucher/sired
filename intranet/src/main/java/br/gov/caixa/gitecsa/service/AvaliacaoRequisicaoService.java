package br.gov.caixa.gitecsa.service;

import javax.ejb.Singleton;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;

@Singleton
public class AvaliacaoRequisicaoService extends AbstractService<AvaliacaoRequisicaoVO> {

    @Override
    protected void validaCampos(AvaliacaoRequisicaoVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegras(AvaliacaoRequisicaoVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegrasExcluir(AvaliacaoRequisicaoVO entity) throws AppException {
        // TODO Auto-generated method stub

    }

}
