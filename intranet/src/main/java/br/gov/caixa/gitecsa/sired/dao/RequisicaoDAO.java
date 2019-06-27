package br.gov.caixa.gitecsa.sired.dao;

import java.util.Date;
import java.util.List;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.dto.ResumoAtendimentoRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface RequisicaoDAO extends GenericDAO<RequisicaoVO> {

    List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro);

    List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, Integer offset, Integer limit);

    List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas);

    List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas, Integer offset, Integer limit);

    List<RequisicaoVO> findUltimasRequisicoesUsuario(UsuarioLdap usuario, Integer limit);

    Integer count(FiltroRequisicaoDTO filtro);

    Integer count(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas);

    RequisicaoVO findByIdEager(Long id);

    List<RequisicaoVO> findRequisicoesEnviadasPorPeriodo(RequisicaoVO requisicao, Date inicio, Date fim);

    List<RequisicaoVO> findAllPendentesFechamento();

    List<ResumoAtendimentoRequisicaoDTO> getResumoAtendimentos(Date dataInicio, Date dataFim);

    RequisicaoVO findByCodigo(Long codigo);

    List<RequisicaoVO> pesquisaAbertasHoje(Date hojeMeiaNoite, Date hoje);
}
