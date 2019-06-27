package br.gov.caixa.gitecsa.service;

import javax.ejb.Stateless;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.BaseAtendimentoVO;

@Stateless
public class BaseAtendimentoService extends AbstractService<BaseAtendimentoVO> {

    private static final long serialVersionUID = -3249551108132913554L;

    @Override
    protected void validaCampos(BaseAtendimentoVO entity) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void validaRegras(BaseAtendimentoVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegrasExcluir(BaseAtendimentoVO entity) throws AppException {
        // TODO Auto-generated method stub

    }

}
