package br.gov.caixa.gitecsa.sired.dao;

import java.util.Date;
import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroFaturamentoRemessaDTO;
import br.gov.caixa.gitecsa.sired.dto.ResumoAtendimentoRemessaDTO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.ViewRelatorioFaturamentoABVO;

public interface RemessaDAO extends GenericDAO<RemessaVO> {

    List<RemessaVO> relatorioFaturamentoTipoC(FiltroFaturamentoRemessaDTO filtro);

    List<ViewRelatorioFaturamentoABVO> relatorioFaturamentoTipoAB(FiltroFaturamentoRemessaDTO filtro);

    List<ResumoAtendimentoRemessaDTO> getResumoAtendimentos(Date dataInicio, Date dataFim);
    
    Integer getQtdItensPorBase(BaseVO base, Date dataInicio, Date dataFim);

    Integer getQtdRemessasPorBase(BaseVO base, Date dataInicio, Date dataFim);

    Integer getQtdRemessasDentroPrazoPorBase(BaseVO base, Integer prazo, Date dataInicio, Date dataFim);

    RemessaVO obterRemessaComMovimentosDiarios(long id);

    RemessaVO obterRemessaComListaDocumentos(Long id);

    List<RemessaVO> findAllPendentesConfirmacaoTipoAb();

    List<RemessaVO> findAllPendentesConfirmacaoTipoC();

    RemessaVO findByIdFetchAll(RemessaVO remessaVO);

    List<RemessaVO> findAllConferidasConfirmadas();

    List<RemessaVO> pesquisaAbertasHojeAB(Date hojeMeiaNoite, Date hoje);

    List<RemessaVO> pesquisaAbertasHojeC(Date hojeMeiaNoite, Date hoje);

}
