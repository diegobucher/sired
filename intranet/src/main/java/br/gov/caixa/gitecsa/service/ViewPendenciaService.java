package br.gov.caixa.gitecsa.service;

import javax.ejb.Stateless;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.ViewPendenciaVO;

@Stateless
public class ViewPendenciaService extends AbstractService<ViewPendenciaVO> {

    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(ViewPendenciaVO entity) {
    }

    @Override
    protected void validaRegras(ViewPendenciaVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(ViewPendenciaVO entity) throws AppException {
    }
}
