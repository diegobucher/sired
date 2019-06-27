package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

public interface TramiteRequisicaoDAO extends GenericDAO<TramiteRequisicaoVO> {
    List<TramiteRequisicaoVO> findByRequisicao(RequisicaoVO requisicao);

    List<TramiteRequisicaoVO> findAtendimentosRequisicao(RequisicaoVO requisicao);

    List<TramiteRequisicaoVO> consultaTramitesEmailSolicitante();

    List<TramiteRequisicaoVO> consultaTramitesEmailBase();

    List<TramiteRequisicaoVO> consultaTramitesEmailTerceirizada();
}
