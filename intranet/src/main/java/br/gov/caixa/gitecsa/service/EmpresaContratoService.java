package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless
public class EmpresaContratoService extends AbstractService<EmpresaContratoVO> implements Serializable {

    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;

    @Override
    protected void validaCampos(EmpresaContratoVO entity) {
    }

    @Override
    protected void validaRegras(EmpresaContratoVO entity) {
        regraDataInicialMenorDataFinal(entity);
    }

    private void regraDataInicialMenorDataFinal(EmpresaContratoVO entity) {
        try {
            if (entity.getDataInicioVigencia().after(entity.getDataFimVigencia()))
                mensagens.add(MensagemUtils.obterMensagem("MA018"));
        } catch (Exception e) {
            Logger logger = LogUtils.getLogger(this.getClass().getName());
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    protected void validaRegrasExcluir(EmpresaContratoVO entity) {
      
    }

    @SuppressWarnings("unchecked")
    public EmpresaContratoVO buscarContratoVigente(UnidadeVO vo) throws AppException {

        Date dataAtual = Calendar.getInstance().getTime();

        List<BaseVO> baseVOs = tramiteRequisicaoDocumentoService.consultaBasePorIdUnidade(Long.parseLong(vo.getId().toString()));
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

    public List<EmpresaContratoVO> find(EmpresaContratoVO filter) {

        StringBuilder hql = new StringBuilder();
        hql.append(" Select ec From EmpresaContratoVO ec ");
        hql.append(" Join Fetch ec.base b ");
        hql.append(" Join Fetch ec.empresa e ");
        hql.append(" Where e.id = :empresa And ec.numeroContrato = :contrato ");

        TypedQuery<EmpresaContratoVO> query = getEntityManager().createQuery(hql.toString(), EmpresaContratoVO.class);
        query.setParameter("empresa", filter.getEmpresa().getId());
        query.setParameter("contrato", filter.getNumeroContrato());

        return query.getResultList();
    }

    public EmpresaContratoVO findById(Long id) {

        StringBuilder hql = new StringBuilder();
        hql.append(" Select ec From EmpresaContratoVO ec ");
        hql.append(" Join Fetch ec.base b ");
        hql.append(" Join Fetch ec.empresa e ");
        hql.append(" Where ec.id = :id ");

        TypedQuery<EmpresaContratoVO> query = getEntityManager().createQuery(hql.toString(), EmpresaContratoVO.class);
        query.setParameter("id", id);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Boolean existeOutroContratoVigente(EmpresaContratoVO contrato) {

        StringBuilder hql = new StringBuilder();
        hql.append(" Select Count(c) From EmpresaContratoVO c ");
        hql.append(" Join c.base b ");
        hql.append(" Where ( ");
        hql.append(" (c.dataInicioVigencia <= :dtInicio And c.dataFimVigencia >= :dtFim) "); // ST1
        hql.append(" Or (c.dataInicioVigencia >= :dtInicio And c.dataFimVigencia >= :dtFim And c.dataInicioVigencia <= :dtFim) "); // ST2
        hql.append(" Or (c.dataInicioVigencia >= :dtInicio And c.dataFimVigencia <= :dtFim) "); // ST3
        hql.append(" Or (c.dataInicioVigencia <= :dtInicio And c.dataFimVigencia <= :dtFim And c.dataFimVigencia >= :dtInicio) "); // ST4
        hql.append(" ) And (b.id = :baseId) ");

        Query query = getEntityManager().createQuery(hql.toString());

        query.setParameter("baseId", contrato.getBase().getId());
        query.setParameter("dtInicio", contrato.getDataInicioVigencia());
        query.setParameter("dtFim", contrato.getDataFimVigencia());

        Long count = (Long) query.getSingleResult();
        return (count > 0) ? true : false;
    }

}
