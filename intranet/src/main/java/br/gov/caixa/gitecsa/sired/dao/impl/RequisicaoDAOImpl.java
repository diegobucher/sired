package br.gov.caixa.gitecsa.sired.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.RequisicaoDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.dto.ResumoAtendimentoRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class RequisicaoDAOImpl extends GenericDAOImpl<RequisicaoVO> implements RequisicaoDAO {

    private static final String SEPARADOR_NUMERO_REQUISICOES = ",";

    @Override
    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, Integer offset, Integer limit) {
        return this.consultar(filtro, null, offset, limit);
    }

    @Override
    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro) {
        return this.consultar(filtro, null, null, null);
    }

    @Override
    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas) {
        return this.consultar(filtro, unidadesAutorizadas, null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas, Integer offset, Integer limit) {

        StringBuilder hql = new StringBuilder("Select Distinct requisicao");
        hql.append(" From RequisicaoVO requisicao ");
        hql.append(" Join Fetch requisicao.unidadeSolicitante ");
        hql.append(" Join Fetch requisicao.requisicaoDocumento reqDocumento ");
        hql.append(" Join Fetch reqDocumento.unidadeGeradora unidadeGeradora ");
        hql.append(" Join Fetch unidadeGeradora.uf ");
        hql.append(" Join Fetch reqDocumento.tipoDemanda ");
        hql.append(" Left Join Fetch reqDocumento.operacao operacao ");
        hql.append(" Join Fetch requisicao.documento documento ");
        hql.append(" Join Fetch documento.grupo grupo ");
        hql.append(" Left Join Fetch grupo.grupoCampos grupoCampos ");
        hql.append(" Left Join Fetch grupoCampos.campo ");
        hql.append(" Join Fetch requisicao.tramiteRequisicaoAtual tramite ");
        hql.append(" Join Fetch tramite.situacaoRequisicao ");
        hql.append(" Left Join Fetch tramite.suporte ");
        hql.append(" Left Join Fetch tramite.ocorrencia ");
        hql.append(" Left Join Fetch requisicao.empresaContrato contrato ");
        hql.append(" Left Join Fetch contrato.base ");
        hql.append(" Where 0 = 0 ");

        hql.append(this.buildSqlConsulta(filtro, unidadesAutorizadas));
        
        hql.append(" ORDER BY requisicao.codigoRequisicao ");

        Query query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);
        query = this.bindParametersConsulta(query, filtro, unidadesAutorizadas);

        if (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) {
            return query.setFirstResult(offset).setMaxResults(limit).getResultList();
        } else {
            return query.getResultList();
        }
    }

    @Override
    public Integer count(FiltroRequisicaoDTO filtro) {
        return this.count(filtro, null);
    }

    @Override
    public Integer count(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas) {

        StringBuilder hql = new StringBuilder("Select count(requisicao) ");
        hql.append(" From RequisicaoVO requisicao ");
        hql.append(" Join requisicao.tramiteRequisicaoAtual tramite ");
        hql.append(" Left Join requisicao.empresaContrato contrato ");
        hql.append(" Where 0 = 0 ");
        hql.append(this.buildSqlConsulta(filtro, unidadesAutorizadas));

        Query query = getEntityManager().createQuery(hql.toString());
        query = this.bindParametersConsulta(query, filtro, unidadesAutorizadas);

        try {
            return ((Long) query.getSingleResult()).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RequisicaoVO> findUltimasRequisicoesUsuario(UsuarioLdap usuario, Integer limit) {

        StringBuilder hql = new StringBuilder("Select Distinct requisicao");
        hql.append(" From RequisicaoVO requisicao ");
        hql.append(" Join Fetch requisicao.unidadeSolicitante ");
        hql.append(" Join Fetch requisicao.documento documento ");
        hql.append(" Join Fetch requisicao.requisicaoDocumento reqDocumento ");
        hql.append(" Join Fetch reqDocumento.tipoDemanda  ");
        hql.append(" Join Fetch reqDocumento.unidadeGeradora unidadeGeradora ");
        hql.append(" Join Fetch unidadeGeradora.uf ");
        hql.append(" Left Join Fetch reqDocumento.operacao operacao ");
        hql.append(" Join Fetch documento.grupo grupo ");
        hql.append(" Join Fetch grupo.grupoCampos grupoCampos ");
        hql.append(" Join Fetch grupoCampos.campo ");
        hql.append(" Join Fetch requisicao.tramiteRequisicaoAtual tramite ");
        hql.append(" Join Fetch tramite.situacaoRequisicao ");
        hql.append(" Left Join Fetch tramite.suporte ");
        hql.append(" Left Join Fetch tramite.ocorrencia ");
        hql.append(" Left Join Fetch requisicao.empresaContrato contrato ");
        hql.append(" Left Join Fetch contrato.base ");
        hql.append(" Where upper(requisicao.codigoUsuarioAbertura) = :matricula");
        hql.append(" ORDER BY requisicao.id desc ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("matricula", usuario.getNuMatricula().toUpperCase());

        return query.setMaxResults(limit).getResultList();
    }

    private StringBuilder buildSqlConsulta(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas) {

        StringBuilder hql = new StringBuilder();

        if (StringUtils.isNotBlank(filtro.getNumeroRequisicoes())) {
            hql.append(" AND requisicao.codigoRequisicao in (:ids) ");
        }

        if (StringUtils.isNotBlank(filtro.getMatriculaUsuario())) {
            hql.append(" AND upper(requisicao.codigoUsuarioAbertura) = :matriculaUsuario ");
        }

        if (!Util.isNullOuVazio(filtro.getUnidadeSolicitante()) && !Util.isNullOuVazio(filtro.getUnidadeSolicitante().getId())) {
            hql.append(" AND requisicao.unidadeSolicitante = :unidadeSolicitante ");
        }

        if (!Util.isNullOuVazio(filtro.getBase())) {
            hql.append(" AND contrato.base = :base ");
        } else if (!Util.isNullOuVazio(unidadesAutorizadas)) {
            hql.append(" AND requisicao.unidadeSolicitante in (:unidadesAutorizadas) ");
        }
        
        String agrupamentoSituacao = (String)RequestUtils.getSessionValue(Constantes.AGRUPAMENTO_SITUACAO_REQUISICAO);
        
        if (Util.isNullOuVazio(filtro.getSituacao()) && !Util.isNullOuVazio(agrupamentoSituacao)) {
            if (agrupamentoSituacao.equals(Constantes.PEND_CAIXA) || agrupamentoSituacao.equals(Constantes.PEND_ATENDIMENTO)) {
                hql.append(" AND tramite.situacaoRequisicao.id in (:situacoesPendentes) ");
            }
        }

        if (!Util.isNullOuVazio(filtro.getSituacao()) && !filtro.getSituacao().getValue().equals(BigDecimal.ZERO.toString())) {
            hql.append(" AND tramite.situacaoRequisicao.id = :situacao ");
        }

        if (!Util.isNullOuVazio(filtro.getMotivo())) {
            hql.append(" AND Exists (Select a From AvaliacaoRequisicaoVO a Join a.tramite t Where a.motivoAvaliacao = :motivo And t.requisicao = requisicao) ");
        }

        if (!Util.isNullOuVazio(filtro.getDataInicio()) && !Util.isNullOuVazio(filtro.getDataFim())) {
            hql.append(" AND requisicao.dataHoraAbertura between :dataInicio and :dataFim ");
        }

        return hql;
    }

    private Query bindParametersConsulta(Query query, FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas) {

        if (StringUtils.isNotBlank(filtro.getNumeroRequisicoes())) {

            List<Long> ids = new ArrayList<Long>();
            String[] requisicoes = StringUtils.split(filtro.getNumeroRequisicoes(), SEPARADOR_NUMERO_REQUISICOES);

            for (String id : requisicoes) {
                ids.add(Long.valueOf(id));
            }

            query.setParameter("ids", ids);
        }

        if (StringUtils.isNotBlank(filtro.getMatriculaUsuario())) {
            query.setParameter("matriculaUsuario", filtro.getMatriculaUsuario().toUpperCase());
        }

        if (!Util.isNullOuVazio(filtro.getUnidadeSolicitante()) && !Util.isNullOuVazio(filtro.getUnidadeSolicitante().getId())) {
            query.setParameter("unidadeSolicitante", filtro.getUnidadeSolicitante());
        }

        if (!Util.isNullOuVazio(filtro.getBase())) {
            query.setParameter("base", filtro.getBase());
        } else if (!Util.isNullOuVazio(unidadesAutorizadas)) {
            query.setParameter("unidadesAutorizadas", unidadesAutorizadas);
        }
        
        String agrupamentoSituacao = (String)RequestUtils.getSessionValue(Constantes.AGRUPAMENTO_SITUACAO_REQUISICAO);
        
        if (Util.isNullOuVazio(filtro.getSituacao()) && !Util.isNullOuVazio(agrupamentoSituacao)) {
            if (agrupamentoSituacao.equals(Constantes.PEND_CAIXA)) {
                
                List<Long> situacoesPendCaixa = new ArrayList<Long>();
                situacoesPendCaixa.add(SituacaoRequisicaoEnum.EM_AUTORIZACAO.getId());
                situacoesPendCaixa.add(SituacaoRequisicaoEnum.ATENDIDA.getId());
                situacoesPendCaixa.add(SituacaoRequisicaoEnum.EM_TRATAMENTO.getId());
                situacoesPendCaixa.add(SituacaoRequisicaoEnum.REABERTA_EM_TRATAMENTO.getId());
                situacoesPendCaixa.add(SituacaoRequisicaoEnum.REATENDIDA.getId());
                query.setParameter("situacoesPendentes", situacoesPendCaixa);
                
            } else if (agrupamentoSituacao.equals(Constantes.PEND_ATENDIMENTO)) {
                
                List<Long> situacoesPendAtendimento = new ArrayList<Long>();
                situacoesPendAtendimento.add(SituacaoRequisicaoEnum.ABERTA.getId());
                situacoesPendAtendimento.add(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId());
                situacoesPendAtendimento.add(SituacaoRequisicaoEnum.PEND_DADOS_FAT.getId());
                situacoesPendAtendimento.add(SituacaoRequisicaoEnum.REABERTA.getId());
                situacoesPendAtendimento.add(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId());
                situacoesPendAtendimento.add(SituacaoRequisicaoEnum.REABERTA_PEND_DADOS_FAT.getId());
                query.setParameter("situacoesPendentes", situacoesPendAtendimento);
            }
        }
        
        if (!Util.isNullOuVazio(filtro.getSituacao()) && !filtro.getSituacao().getValue().equals(BigDecimal.ZERO.toString())) {
            query.setParameter("situacao", filtro.getSituacao().getId());
        }

        if (!Util.isNullOuVazio(filtro.getMotivo())) {
            query.setParameter("motivo", filtro.getMotivo());
        }

        if (!Util.isNullOuVazio(filtro.getDataInicio()) && !Util.isNullOuVazio(filtro.getDataFim())) {
            
            Date dataInicio = DateUtils.fitAtStart(filtro.getDataInicio());
            Date dataFim = DateUtils.fitAtEnd(filtro.getDataFim());
            
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
        }

        return query;
    }

    @Override
    public RequisicaoVO findByIdEager(Long id) {

        StringBuilder hql = new StringBuilder("Select requisicao");
        hql.append(" From RequisicaoVO requisicao ");
        hql.append(" Join Fetch requisicao.unidadeSolicitante ");
        hql.append(" Join Fetch requisicao.documento documento ");
        hql.append(" Join Fetch requisicao.requisicaoDocumento reqDocumento ");
        hql.append(" Join Fetch reqDocumento.unidadeGeradora ");
        hql.append(" Join Fetch reqDocumento.tipoDemanda ");
        hql.append(" Left Join Fetch reqDocumento.operacao ");
        hql.append(" Join Fetch documento.grupo grupo ");
        hql.append(" Left Join Fetch grupo.grupoCampos grpCampos ");
        hql.append(" Left Join Fetch grpCampos.campo ");
        hql.append(" Join Fetch requisicao.tramiteRequisicaoAtual tramite ");
        hql.append(" Join Fetch tramite.situacaoRequisicao ");
        hql.append(" Left Join Fetch tramite.suporte ");
        hql.append(" Left Join Fetch tramite.ocorrencia ");
        hql.append(" Left Join Fetch requisicao.empresaContrato contrato ");
        hql.append(" Left Join Fetch contrato.base ");
        hql.append(" Where requisicao.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);
        query.setParameter("id", id);

        try {
            return (RequisicaoVO) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public RequisicaoVO findByCodigo(Long codigo) {

        StringBuilder hql = new StringBuilder("Select requisicao");
        hql.append(" From RequisicaoVO requisicao ");
        hql.append(" Where requisicao.codigoRequisicao = :codigo ");

        Query query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);
        query.setParameter("codigo", codigo);

        try {
            return (RequisicaoVO) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<RequisicaoVO> findRequisicoesEnviadasPorPeriodo(RequisicaoVO requisicao, Date inicio, Date fim) {

        StringBuilder hql = new StringBuilder("Select r");

        hql.append(" From RequisicaoVO r ");
        hql.append(" Join Fetch r.unidadeSolicitante us ");
        hql.append(" Join Fetch us.municipio ");
        hql.append(" Join Fetch r.documento d ");
        hql.append(" Join Fetch r.requisicaoDocumento rd ");
        hql.append(" Join Fetch r.tramiteRequisicaoAtual t ");
        hql.append(" Join Fetch t.situacaoRequisicao s ");
        hql.append(" Left Join Fetch t.suporte ");
        hql.append(" Left Join Fetch t.ocorrencia ");
        hql.append(" Left Join Fetch rd.operacao ");
        hql.append(" Left Join Fetch rd.tipoDemanda ");
        hql.append(" Left Join Fetch rd.unidadeGeradora ug ");
        hql.append(" Left Join Fetch ug.municipio ");
        hql.append(" Where r.dataHoraAbertura between :inicio and :fim ");
        hql.append(" and d = :documento ");
        if (!ObjectUtils.isNullOrEmpty(requisicao.getCodigoRequisicao())) {
        	hql.append(" and r.codigoRequisicao != :codigo ");
        }
        hql.append(" and s.id != :rascunho and s.id != :cancelada ");
        hql.append(" and r.unidadeSolicitante = :unidadeSolicitante ");

        TypedQuery<RequisicaoVO> query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);
        query.setParameter("inicio", DateUtils.fitAtStart(inicio));
        query.setParameter("fim", DateUtils.fitAtEnd(fim));
        query.setParameter("documento", requisicao.getDocumento());
        if (!ObjectUtils.isNullOrEmpty(requisicao.getCodigoRequisicao())) {
        	query.setParameter("codigo", requisicao.getCodigoRequisicao());	
		}
        
        query.setParameter("rascunho", SituacaoRequisicaoEnum.RASCUNHO.getId());
        query.setParameter("cancelada", SituacaoRequisicaoEnum.CANCELADA.getId());
        query.setParameter("unidadeSolicitante", requisicao.getUnidadeSolicitante());

        return query.getResultList();
    }

    @Override
    public List<RequisicaoVO> findAllPendentesFechamento() {

        StringBuilder hql = new StringBuilder("Select r");
        hql.append(" From RequisicaoVO r ");
        hql.append(" Join Fetch r.unidadeSolicitante ");
        hql.append(" Join Fetch r.tramiteRequisicaoAtual t ");
        hql.append(" Join Fetch t.situacaoRequisicao s ");
        hql.append(" Left Join Fetch t.suporte ");
        hql.append(" Left Join Fetch t.ocorrencia ");
        hql.append(" Where s.id in (:situacoes) ");
        hql.append(" and not exists ( Select a.id From AvaliacaoRequisicaoVO a Where a.tramite = t ) ");

        TypedQuery<RequisicaoVO> query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);

        List<Long> listSituacao = new ArrayList<Long>();
        listSituacao.add(SituacaoRequisicaoEnum.ATENDIDA.getId());
        listSituacao.add(SituacaoRequisicaoEnum.REATENDIDA.getId());
        query.setParameter("situacoes", listSituacao);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ResumoAtendimentoRequisicaoDTO> getResumoAtendimentos(final Date dataInicio, final Date dataFim) {

        StringBuilder sql = new StringBuilder("");
        sql.append(" SELECT no_base as base, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'ABERTA' THEN 1 ELSE 0 END ) AS numAbertas, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'FECHADA' THEN 1 ELSE 0 END ) AS numFechadas, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'RECUPERADA' THEN 1 ELSE 0 END ) AS numRecuperadas, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'NAO_LOCALIZADO' THEN 1 ELSE 0 END ) AS numNaoLocalizadas, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'OUTRAS' THEN 1 ELSE 0 END ) AS numOutras ");
        sql.append(" FROM redsm001.redvw004_relatorio_geral_requisicao ");
        sql.append(" WHERE dh_tramite >= :datainicio AND dh_tramite <= :dataFim ");
        sql.append(" GROUP BY no_base ");
        sql.append(" ORDER BY no_base ");

        SQLQuery query = ((Session) getEntityManager().unwrap(Session.class)).createSQLQuery(sql.toString());
        query.addScalar("base", StringType.INSTANCE);
        query.addScalar("numAbertas", IntegerType.INSTANCE);
        query.addScalar("numFechadas", IntegerType.INSTANCE);
        query.addScalar("numRecuperadas", IntegerType.INSTANCE);
        query.addScalar("numNaoLocalizadas", IntegerType.INSTANCE);
        query.addScalar("numOutras", IntegerType.INSTANCE);
        
        query.setParameter("datainicio", DateUtils.fitAtStart(dataInicio));
        query.setParameter("dataFim", DateUtils.fitAtEnd(dataFim));        
        
        query.setResultTransformer(Transformers.aliasToBean(ResumoAtendimentoRequisicaoDTO.class));
        List<ResumoAtendimentoRequisicaoDTO> listResumo = query.list();

        return listResumo;
    }

    @Override
    public List<RequisicaoVO> pesquisaAbertasHoje(Date hojeMeiaNoite, Date hoje) {
      
      StringBuilder hql = new StringBuilder("Select DISTINCT r");
      hql.append(" From RequisicaoVO r ");
      hql.append(" Join Fetch r.unidadeSolicitante us ");
      hql.append(" Join Fetch us.tipoUnidade tu ");
      hql.append(" Join Fetch r.requisicaoDocumento rd ");
      hql.append(" Join Fetch rd.tipoDemanda td ");
      hql.append(" Join Fetch rd.unidadeGeradora ug ");
      hql.append(" Join Fetch ug.uf ");
      hql.append(" Join Fetch r.tramiteRequisicaoAtual t ");
      hql.append(" Join Fetch r.documento d ");
      hql.append(" Join Fetch d.grupo gp ");
      hql.append(" Join Fetch gp.grupoCampos gc ");
      hql.append(" Join Fetch gc.campo ca ");
      hql.append(" Join Fetch t.situacaoRequisicao s ");
      hql.append(" Join Fetch t.situacaoRequisicao s ");
      hql.append(" Where s.id in (:situacoes) ");
      hql.append(" AND r.dataHoraAbertura BETWEEN :hojeMeiaNoite and :hoje ");
      
      TypedQuery<RequisicaoVO> query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);
      List<Long> listSituacao = new ArrayList<>();
      listSituacao.add(SituacaoRequisicaoEnum.ABERTA.getId());
      query.setParameter("situacoes", listSituacao);
      
      query.setParameter("hojeMeiaNoite", hojeMeiaNoite);
      query.setParameter("hoje", hoje);

      return query.getResultList();
    }

}
