package br.gov.caixa.gitecsa.service;

import javax.ejb.Stateless;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.vo.OcorrenciaAtendimentoVO;

@Stateless
public class OcorrenciaAtendimentoService extends AbstractService<OcorrenciaAtendimentoVO> {

    private static final long serialVersionUID = 164679799737053722L;

    public OcorrenciaAtendimentoVO findById(Long id) {
        Criteria criteria = getSession().createCriteria(OcorrenciaAtendimentoVO.class);
        criteria.add(Restrictions.eq("id", id));
        return (OcorrenciaAtendimentoVO) criteria.uniqueResult();
    }

    @Override
    protected void validaCampos(OcorrenciaAtendimentoVO entity) {

    }

    @Override
    protected void validaRegras(OcorrenciaAtendimentoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(OcorrenciaAtendimentoVO entity) throws AppException {

    }

}
