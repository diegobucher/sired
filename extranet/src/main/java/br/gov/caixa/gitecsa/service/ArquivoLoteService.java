package br.gov.caixa.gitecsa.service;

import java.io.Serializable;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;

@Deprecated
// @Stateless
public class ArquivoLoteService extends AbstractService<ArquivoLoteVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(ArquivoLoteVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegras(ArquivoLoteVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegrasExcluir(ArquivoLoteVO entity) throws AppException {
        // TODO Auto-generated method stub

    }

}
