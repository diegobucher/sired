package br.gov.caixa.gitecsa.sired.dao;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public interface AvaliacaoRequisicaoDAO extends GenericDAO<AvaliacaoRequisicaoVO> {
    AvaliacaoRequisicaoVO findByRequisicao(RequisicaoVO requisicao);

    Boolean hasAvaliacao(RequisicaoVO requisicao);
}
