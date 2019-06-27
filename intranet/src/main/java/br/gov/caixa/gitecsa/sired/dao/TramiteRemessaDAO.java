package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;

public interface TramiteRemessaDAO extends GenericDAO<TramiteRemessaVO> {
    List<TramiteRemessaVO> findByRemessa(RemessaVO remessa);

    List<TramiteRemessaVO> consultaTramitesEmailSolicitante();

    List<TramiteRemessaVO> consultaTramitesEmailTerceirizada();

    String consultaTramiteAbertura(Long idRequisicao);

    List<TramiteRemessaVO> consultaTramitesEmailBase();
}
