package br.gov.caixa.gitecsa.service;

import java.io.Serializable;

import javax.ejb.Stateless;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;

@Stateless
public class SuporteService extends AbstractService<SuporteVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(SuporteVO entity) {
    }

    @Override
    protected void validaRegras(SuporteVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(SuporteVO entity) {
    }

}
