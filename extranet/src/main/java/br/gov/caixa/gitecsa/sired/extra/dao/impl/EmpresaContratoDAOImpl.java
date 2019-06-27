package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.extra.dao.BaseDAO;
import br.gov.caixa.gitecsa.sired.extra.dao.EmpresaContratoDAO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public class EmpresaContratoDAOImpl extends GenericDAOImpl<EmpresaContratoVO> implements EmpresaContratoDAO {

    @Inject
    private BaseDAO baseDAO;

    @SuppressWarnings("unchecked")
    @Override
    public EmpresaContratoVO buscarContratoVigente(RequisicaoVO vo) throws AppException {
        Date dataAtual = Calendar.getInstance().getTime();

        List<BaseVO> baseVOs = baseDAO.consultaBasePorIdUnidade(Long.parseLong(vo.getUnidadeSolicitante().getId().toString()));
        BaseVO baseVO = baseVOs.get(0);

        Criteria criteria = getSession().createCriteria(EmpresaContratoVO.class);
        criteria.add(Restrictions.eq("base", baseVO));
        criteria.add(Restrictions.ge("dataFimVigencia", dataAtual));
        criteria.add(Restrictions.le("dataInicioVigencia", dataAtual));

        List<EmpresaContratoVO> result = (List<EmpresaContratoVO>) criteria.list();

        if (result.size() > 0) {
            return result.get(0);
        }

        return null;
    }

}
