package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.extra.dao.EmpresaDAO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;

@Stateless
public class EmpresaService extends AbstractService<EmpresaVO> implements Serializable {

    private static final long serialVersionUID = 4008809861294739426L;

    @Inject
    private EmpresaDAO empresaDAO;

    public EmpresaVO obterEmpresaCNPJ(Long cnpj) {
        return empresaDAO.obterEmpresaCNPJ(cnpj);
    }

    @Override
    protected void validaCamposObrigatorios(EmpresaVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected void validaRegras(EmpresaVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected void validaRegrasExcluir(EmpresaVO entity) throws BusinessException {
        // do nothing
    }

    @Override
    protected GenericDAO<EmpresaVO> getDAO() {
        return empresaDAO;
    }

}
