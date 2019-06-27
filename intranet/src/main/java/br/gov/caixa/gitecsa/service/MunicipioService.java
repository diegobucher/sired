package br.gov.caixa.gitecsa.service;

import java.io.Serializable;

import javax.ejb.Stateless;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;

@Stateless
public class MunicipioService extends AbstractService<MunicipioVO> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(MunicipioVO entity) {

    }

    @Override
    protected void validaRegras(MunicipioVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(MunicipioVO entity) {

    }

}
