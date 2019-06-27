package br.gov.caixa.gitecsa.service;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

@Deprecated
// @Stateless
public class TramiteRequisicaoDocumentoService extends AbstractService<TramiteRequisicaoVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @Override
    protected void validaCampos(TramiteRequisicaoVO entity) {

    }

    @Override
    protected void validaRegras(TramiteRequisicaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(TramiteRequisicaoVO entity) throws AppException {

    }

}
