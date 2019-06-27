package br.gov.caixa.gitecsa.service;

import java.io.Serializable;

import javax.ejb.Stateless;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.PerfilFuncionalidadeVO;

@Stateless
public class PerfilFuncionalidadeService extends AbstractService<PerfilFuncionalidadeVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(PerfilFuncionalidadeVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegras(PerfilFuncionalidadeVO entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void validaRegrasExcluir(PerfilFuncionalidadeVO entity) {
        // TODO Auto-generated method stub

    }

}
