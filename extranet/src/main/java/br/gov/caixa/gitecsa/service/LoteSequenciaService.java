package br.gov.caixa.gitecsa.service;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.LoteSequenciaVO;

@Deprecated
//@Stateless
public class LoteSequenciaService extends AbstractService<LoteSequenciaVO> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void validaCampos(LoteSequenciaVO entity) {
	}

	@Override
	protected void validaRegras(LoteSequenciaVO entity) {

	}

	@Override
	protected void validaRegrasExcluir(LoteSequenciaVO entity) {
	}
	
	@SuppressWarnings("unchecked")
	public List<LoteSequenciaVO> findAssociadoBase() {
		Criteria criteria = getSession().createCriteria(LoteSequenciaVO.class).createAlias("bases", "base", JoinType.INNER_JOIN);

		criteria.addOrder(Order.desc("id"));

		return ((List<LoteSequenciaVO>) criteria.list());

	}

}
