package br.gov.caixa.gitecsa.service;

import javax.ejb.Stateless;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.BaseLoteSeqVO;

@Stateless
public class BaseLoteSeqService extends AbstractService<BaseLoteSeqVO> {

    @Override
    protected void validaCampos(BaseLoteSeqVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegras(BaseLoteSeqVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegrasExcluir(BaseLoteSeqVO entity) throws AppException {
        // TODO Auto-generated method stub

    }

}
