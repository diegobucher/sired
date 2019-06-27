package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;

import org.hibernate.Hibernate;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.UnidadeGrupoVO;

@Stateless
public class UnidadeGrupoService extends AbstractService<UnidadeGrupoVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(UnidadeGrupoVO entity) {
    }

    @Override
    protected void validaRegras(UnidadeGrupoVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(UnidadeGrupoVO entity) {
    }

    @Override
    public List<UnidadeGrupoVO> findByParameters(UnidadeGrupoVO object) throws AppException {
        List<UnidadeGrupoVO> lista = super.findByParameters(object);
        for (UnidadeGrupoVO unidadeGrupoVO : lista) {
            Hibernate.initialize(unidadeGrupoVO.getOperacao());
        }
        return lista;
    }

}
