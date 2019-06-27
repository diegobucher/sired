package br.gov.caixa.gitecsa.sired.extra.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;

public interface AvaliacaoDAO extends GenericDAO<AvaliacaoRequisicaoVO> {

    AvaliacaoRequisicaoVO obterPorRequisicao(Long idRequisicao);

}
