package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.Date;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.dao.EmpresaContratoDAO;
import br.gov.caixa.gitecsa.sired.enumerator.AbrangenciaEnum;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.vo.BaseAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class EmpresaContratoDAOImpl extends GenericDAOImpl<EmpresaContratoVO> implements EmpresaContratoDAO {

  @Override
  public EmpresaContratoVO findByAbrangenciaUnidadeEager(UnidadeVO unidade) throws DataBaseException {
    try {
      StringBuilder hql = new StringBuilder();
      hql.append(" Select ec ");
      hql.append(" From EmpresaContratoVO ec, ViewAbrangenciaVO v ");
      hql.append(" Join Fetch ec.base ");
      hql.append(" Join Fetch ec.empresa ");
      hql.append(" Where v.idUnidadeSolicitada = :idUnidadeSolicitada ");
      hql.append(" And v.abrangencia = :abrangencia ");
      hql.append(" And ec.id = v.nuEmpresaContrato ");

      Query query = getEntityManager().createQuery(hql.toString(), EmpresaContratoVO.class);

      query.setParameter("idUnidadeSolicitada", unidade.getId());
      query.setParameter("abrangencia", AbrangenciaEnum.PERTENCE);
      
      return (EmpresaContratoVO) query.getSingleResult();
      
    } catch (NoResultException e) {
      return null;
    } catch (Exception e) {
      throw new DataBaseException(e.getMessage(), e);
    }
  }

    @Override
    public EmpresaContratoVO findByBaseAtendimento(BaseAtendimentoVO baseAtendimento) throws DataBaseException {
        StringBuilder hql = new StringBuilder();
        hql.append(" Select ec ");
        hql.append(" From EmpresaContratoVO ec ");
        hql.append(" Join Fetch ec.empresa ");
        hql.append(" Join Fetch ec.base bs ");
        hql.append(" Where bs.id = :idBaseAtendimento ");
        hql.append(" And ec.dataInicioVigencia <= :data And ec.dataFimVigencia >= :data ");

        TypedQuery<EmpresaContratoVO> query = getEntityManager().createQuery(hql.toString(), EmpresaContratoVO.class);
        query.setParameter("idBaseAtendimento", baseAtendimento.getBase().getId());
        query.setParameter("data", DateUtils.fitAtStart(new Date()));

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }
}
