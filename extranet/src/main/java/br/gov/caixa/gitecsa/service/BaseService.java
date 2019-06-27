package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Deprecated
// @Stateless
public class BaseService extends AbstractService<BaseVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @Override
    protected void validaCampos(BaseVO entity) {
    }

    @Override
    protected void validaRegras(BaseVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(BaseVO entity) throws AppException {

    }

    @SuppressWarnings("unchecked")
    public List<BaseVO> consultaBasesEmpresaContrato() throws Exception {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT empresaContrato FROM EmpresaVO empresa ");
        hql.append(" JOIN empresa.empresaContratos empresaContrato join fetch empresaContrato.base base ");
        hql.append(" WHERE empresa.cnpj = :pCnpj");

        Query query = entityManagerSistema.createQuery(hql.toString(), EmpresaContratoVO.class);
        query.setParameter("pCnpj", JSFUtil.getUsuario().getNuCnpj());

        List<EmpresaContratoVO> contratos = query.getResultList();
        List<BaseVO> bases = new ArrayList<BaseVO>();
        for (EmpresaContratoVO contrato : contratos) {
            bases.add(contrato.getBase());
        }
        return bases;
    }

}
