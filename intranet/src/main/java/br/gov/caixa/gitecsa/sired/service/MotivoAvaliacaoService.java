package br.gov.caixa.gitecsa.sired.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.MotivoAvaliacaoDAO;
import br.gov.caixa.gitecsa.sired.vo.MotivoAvaliacaoVO;

@Stateless(name = "avaliacaoService")
public class MotivoAvaliacaoService extends AbstractService<MotivoAvaliacaoVO> {

    private static final long serialVersionUID = -7448186678271039372L;

    @Inject
    private MotivoAvaliacaoDAO dao;

    @Override
    protected void validaCamposObrigatorios(MotivoAvaliacaoVO entity) {
    }

    @Override
    protected void validaRegras(MotivoAvaliacaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(MotivoAvaliacaoVO entity) {

    }

    @Override
    protected GenericDAO<MotivoAvaliacaoVO> getDAO() {
        return this.dao;
    }

}
