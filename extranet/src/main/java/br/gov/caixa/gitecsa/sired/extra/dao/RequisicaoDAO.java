package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface RequisicaoDAO extends GenericDAO<RequisicaoVO> {

    List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro);

    List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, Integer offset, Integer limit);

    List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas);

    List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas, Integer offset, Integer limit);

    Integer count(FiltroRequisicaoDTO filtro);

    Integer count(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas);

    RequisicaoVO obterPorNumeroID(Long numeroIdentificacao);

    RequisicaoVO findByIdEager(Long id);

}
