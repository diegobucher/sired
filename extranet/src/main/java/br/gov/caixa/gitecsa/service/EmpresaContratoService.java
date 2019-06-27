package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;

@Deprecated
// @Stateless
public class EmpresaContratoService extends AbstractService<EmpresaContratoVO> implements Serializable {

    private static final long serialVersionUID = 3016183975736128968L;

    @Override
    protected void validaCampos(EmpresaContratoVO entity) {
    }

    @Override
    protected void validaRegras(EmpresaContratoVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(EmpresaContratoVO entity) {
    }

    @SuppressWarnings("unchecked")
    public List<EmpresaContratoVO> buscarContratos(EmpresaVO empresa) throws AppException {
        Criteria criteria = getSession().createCriteria(EmpresaContratoVO.class);
        criteria.add(Restrictions.eq("empresa", empresa));

        return (List<EmpresaContratoVO>) criteria.list();
    }
}
