package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.extra.dao.EmpresaDAO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;

public class EmpresaDAOImpl extends GenericDAOImpl<EmpresaVO> implements EmpresaDAO {

    @Override
    public EmpresaVO obterEmpresaCNPJ(Long cnpj) {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT empresa FROM EmpresaVO empresa ");
        hql.append(" WHERE empresa.cnpj = :cnpj");

        Query query = getEntityManager().createQuery(hql.toString(), EmpresaVO.class);
        query.setParameter("cnpj", cnpj);

        try {
            return (EmpresaVO) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
