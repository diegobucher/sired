package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.extra.dao.impl.BaseDAOImpl;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless
public class BaseService extends AbstractService<BaseVO> implements Serializable {

    private static final long serialVersionUID = -8122036734005220491L;

    @Inject
    private BaseDAOImpl baseDAO;

    @Override
    protected void validaRegras(BaseVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(BaseVO entity) throws BusinessException {

    }

    public List<BaseVO> consultaBasesEmpresaContrato() {
        return baseDAO.consultaBasesEmpresaContrato();
    }

    @Override
    public List<BaseVO> findAll() {
        return baseDAO.findAll();
    }

    public BaseVO getBaseByUnidade(UnidadeVO unidadeVO) {
        return baseDAO.getBaseByUnidade(unidadeVO);
    }

    public List<BaseVO> consultaBasePorIdUnidade(long idUnidade) throws AppException {
        return baseDAO.consultaBasePorIdUnidade(idUnidade);
    }

    @Override
    protected void validaCamposObrigatorios(BaseVO entity) {
        // do nothing
    }

    @Override
    protected GenericDAO<BaseVO> getDAO() {
        return baseDAO;
    }

}
