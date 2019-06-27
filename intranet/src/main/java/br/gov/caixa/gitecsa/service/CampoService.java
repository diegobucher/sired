package br.gov.caixa.gitecsa.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;

@Stateless
public class CampoService extends AbstractService<CampoVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    private GrupoService serviceGrupo;

    @Override
    protected void validaCampos(CampoVO entity) {
    }

    @Override
    protected void validaRegras(CampoVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(CampoVO entity) {
    }

}
