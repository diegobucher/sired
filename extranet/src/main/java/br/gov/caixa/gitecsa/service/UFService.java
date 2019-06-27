package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.vo.UFVO;

@Stateless
public class UFService extends AbstractService<UFVO> implements	Serializable {
	private static final long serialVersionUID = 3016183975736128968L;

	@Override
	protected void validaCampos(UFVO entity) {	}

	@Override
	protected void validaRegras(UFVO entity) {	}

	@Override
	protected void validaRegrasExcluir(UFVO entity) {	}

	@SuppressWarnings("unchecked")
	public List<UFVO> findAssociadoUnidade(){
		Criteria criteria = getSession().createCriteria(UFVO.class)		
				.createAlias("unidades", "unidade", JoinType.INNER_JOIN);

		criteria.addOrder(Order.desc("nome"));
		
		return  ((List<UFVO>)criteria.list());
		
	}
	
	@SuppressWarnings("unchecked")
	public List<UFVO> findAssociadoCTDA(){
		Criteria criteria = getSession().createCriteria(UFVO.class)		
				.createAlias("ctdaUfs", "ctda", JoinType.INNER_JOIN);

		criteria.addOrder(Order.desc("nome"));
		
		return  ((List<UFVO>)criteria.list());
		
	}
	
	
}
	