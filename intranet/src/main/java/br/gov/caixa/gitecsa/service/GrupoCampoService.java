package br.gov.caixa.gitecsa.service;

import javax.ejb.Stateless;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;

@Stateless
public class GrupoCampoService extends AbstractService<GrupoCampoVO> {

    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(GrupoCampoVO entity) {
    }

    @Override
    protected void validaRegras(GrupoCampoVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(GrupoCampoVO entity) {
    }

}
