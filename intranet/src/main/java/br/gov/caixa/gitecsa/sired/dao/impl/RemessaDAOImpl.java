package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.RemessaDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroFaturamentoRemessaDTO;
import br.gov.caixa.gitecsa.sired.dto.ResumoAtendimentoRemessaDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.ViewRelatorioFaturamentoABVO;

public class RemessaDAOImpl extends GenericDAOImpl<RemessaVO> implements RemessaDAO {

    @Override
    public List<RemessaVO> relatorioFaturamentoTipoC(FiltroFaturamentoRemessaDTO filtro) {

        StringBuilder hql = new StringBuilder("");
        hql.append(" Select Distinct r From RemessaVO r ");
        hql.append(" Join Fetch r.empresaContrato c ");
        hql.append(" Join Fetch c.base ");
        hql.append(" Join Fetch c.empresa ");
        hql.append(" Join Fetch r.unidadeSolicitante ");
        hql.append(" Join Fetch r.remessaDocumentos d ");
        hql.append(" Join Fetch d.unidadeGeradora ");
        hql.append(" Join Fetch r.tramiteRemessaAtual t ");
        hql.append(" Join Fetch t.situacao s ");
        hql.append(" Where s.id In (:situacoes) ");
        hql.append(" AND r.documento.nome = :documentoMovimentoDiario ");

        Map<String, Object> params = new HashMap<String, Object>();

        // Long[] situacoes = {SituacaoRemessaEnum.FECHADA.getId(), SituacaoRemessaEnum.FECHADA_INCONSISTENTE.getId()};
        params.put("situacoes", SituacaoRemessaEnum.FECHADA.getId());

        // DOCUMENTO = REMESSA - MOVIMENTO DIARIO.
        params.put("documentoMovimentoDiario", Constantes.DOCUMENTO_REMESSA_MOVIMENTO_DIARIO);

        if (!ObjectUtils.isNullOrEmpty(filtro.getBase())) {
            hql.append(" And c.base = :base ");
            params.put("base", filtro.getBase());
        }

        if (!Util.isNullOuVazio(filtro.getDataInicio()) && !Util.isNullOuVazio(filtro.getDataFim())) {
            hql.append(" And t.dataTramiteRemessa Between :dataInicio And :dataFim ");
            params.put("dataInicio", DateUtils.fitAtStart(filtro.getDataInicio()));
            params.put("dataFim", DateUtils.fitAtEnd(filtro.getDataFim()));
        }

        hql.append(" order by r.id asc ");

        TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);

        for (String key : params.keySet()) {
            query.setParameter(key, params.get(key));
        }

        return query.getResultList();
    }

    @Override
    public List<ViewRelatorioFaturamentoABVO> relatorioFaturamentoTipoAB(FiltroFaturamentoRemessaDTO filtro) {

        StringBuilder hql = new StringBuilder("");
        hql.append(" Select r From ViewRelatorioFaturamentoABVO r ");
        hql.append(" Where 0 = 0 ");

        Map<String, Object> params = new HashMap<String, Object>();

        if (!ObjectUtils.isNullOrEmpty(filtro.getBase())) {
            hql.append(" And r.nuBase = :base ");
            params.put("base", filtro.getBase().getId());
        }

        if (!Util.isNullOuVazio(filtro.getDataInicio()) && !Util.isNullOuVazio(filtro.getDataFim())) {
            hql.append(" And r.dataHoraAbertura Between :dataInicio And :dataFim ");
            params.put("dataInicio", DateUtils.fitAtStart(filtro.getDataInicio()));
            params.put("dataFim", DateUtils.fitAtEnd(filtro.getDataFim()));
        }

        hql.append(" order by r.id asc ");

        TypedQuery<ViewRelatorioFaturamentoABVO> query = getEntityManager().createQuery(hql.toString(), ViewRelatorioFaturamentoABVO.class);

        for (String key : params.keySet()) {
            query.setParameter(key, params.get(key));
        }

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ResumoAtendimentoRemessaDTO> getResumoAtendimentos(Date dataInicio, Date dataFim) {
        StringBuilder sql = new StringBuilder("");
        sql.append(" SELECT no_base as base, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'ABERTA' THEN 1 ELSE 0 END ) AS numAbertas, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'FECHADA' THEN 1 ELSE 0 END ) AS numFechadas, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'AGENDADA' THEN 1 ELSE 0 END ) AS numAgendadas, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'RECEBIDA' THEN 1 ELSE 0 END ) AS numRecebidas, ");
        sql.append(" SUM(CASE WHEN ic_situacao = 'CONFERIDA' THEN 1 ELSE 0 END ) AS numConferidas ");
        sql.append(" FROM redsm001.redvw005_relatorio_geral_remessa ");
        sql.append(" WHERE dh_tramite >= :datainicio AND dh_tramite <= :dataFim ");
        sql.append(" GROUP BY no_base ");
        sql.append(" ORDER BY no_base ");

        SQLQuery query = ((Session) getEntityManager().unwrap(Session.class)).createSQLQuery(sql.toString());
        query.addScalar("base", StringType.INSTANCE);
        query.addScalar("numAbertas", IntegerType.INSTANCE);
        query.addScalar("numFechadas", IntegerType.INSTANCE);
        query.addScalar("numAgendadas", IntegerType.INSTANCE);
        query.addScalar("numRecebidas", IntegerType.INSTANCE);
        query.addScalar("numConferidas", IntegerType.INSTANCE);
        
        query.setParameter("datainicio", DateUtils.fitAtStart(dataInicio));
        query.setParameter("dataFim", DateUtils.fitAtEnd(dataFim));        
        
        query.setResultTransformer(Transformers.aliasToBean(ResumoAtendimentoRemessaDTO.class));
        List<ResumoAtendimentoRemessaDTO> listResumo = query.list();

        return listResumo;
    }

    @Override
    public Integer getQtdItensPorBase(BaseVO base, Date dataInicio, Date dataFim) {
        
        StringBuilder hql = new StringBuilder("");
        hql.append(" Select COUNT(d) ");
        hql.append(" From RemessaDocumentoVO d ");
        hql.append(" Join d.remessa r ");
        hql.append(" Join r.empresaContrato e ");
        hql.append(" Join e.base b ");
        hql.append(" Where b.nome = :base And d.dataGeracao Between :dataInicio And :dataFim ");

        Query query = getEntityManager().createQuery(hql.toString());
        
        query.setParameter("base", base.getNome());
        query.setParameter("dataInicio", DateUtils.fitAtStart(dataInicio));
        query.setParameter("dataFim", DateUtils.fitAtEnd(dataFim)); 

        try {
            return ((Long) query.getSingleResult()).intValue();
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public Integer getQtdRemessasPorBase(BaseVO base, Date dataInicio, Date dataFim) {
        
        StringBuilder hql = new StringBuilder("");
        hql.append(" Select COUNT(r) ");
        hql.append(" From RemessaVO r ");
        hql.append(" Join r.empresaContrato e ");
        hql.append(" Join e.base b ");
        hql.append(" Where b.nome = :base And r.dataHoraAbertura Between :dataInicio And :dataFim ");

        Query query = getEntityManager().createQuery(hql.toString());
        
        query.setParameter("base", base.getNome());
        query.setParameter("dataInicio", DateUtils.fitAtStart(dataInicio));
        query.setParameter("dataFim", DateUtils.fitAtEnd(dataFim)); 

        try {
            return ((Long) query.getSingleResult()).intValue();
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public Integer getQtdRemessasDentroPrazoPorBase(BaseVO base, Integer prazo, Date dataInicio, Date dataFim) {
        
        StringBuilder hql = new StringBuilder("");
        hql.append(" Select t ");
        hql.append(" From TramiteRemessaVO t ");
        hql.append(" Join Fetch t.remessa r ");
        hql.append(" Join t.situacao s ");
        hql.append(" Join r.empresaContrato e ");
        hql.append(" Join e.base b ");
        hql.append(" Where b.nome = :base And t.dataTramiteRemessa Between :dataInicio And :dataFim And s.id = :situacao ");

        TypedQuery<TramiteRemessaVO> query = getEntityManager().createQuery(hql.toString(), TramiteRemessaVO.class);
        
        query.setParameter("base", base.getNome());
        query.setParameter("dataInicio", DateUtils.fitAtStart(dataInicio));
        query.setParameter("dataFim", DateUtils.fitAtEnd(dataFim));
        query.setParameter("situacao", SituacaoRemessaEnum.RECEBIDA.getId());
        
        List<TramiteRemessaVO> listTramite = query.getResultList();
        
        int qtdDentroPrazo = 0;
        
        for (TramiteRemessaVO tramite : listTramite) {
        
            Calendar dtLimite = Calendar.getInstance();
            dtLimite.setTime(tramite.getRemessa().getDataHoraAbertura());
            dtLimite.setLenient(false);
            dtLimite.add(Calendar.DAY_OF_MONTH, prazo);
            
            Calendar dtRecebimento = Calendar.getInstance();
            dtLimite.setLenient(false);
            dtRecebimento.setTime(tramite.getDataTramiteRemessa());
            
            if (dtLimite.after(dtRecebimento) ) {
                qtdDentroPrazo++;
            }
        }

        return qtdDentroPrazo;        
    }

    @Override
    public RemessaVO obterRemessaComMovimentosDiarios(long id) {
      try {
        StringBuilder hql = new StringBuilder("");
        
        hql.append(" SELECT Distinct remessa  ");
        hql.append(" FROM RemessaVO remessa ");
        hql.append(" LEFT JOIN FETCH remessa.movimentosDiarioList movimentosDiarioList ");
        hql.append(" LEFT JOIN FETCH movimentosDiarioList.unidadeGeradora unidadeGeradora  ");
        hql.append(" LEFT JOIN FETCH remessa.tramiteRemessaAtual t  ");
        hql.append(" LEFT JOIN FETCH t.situacao s  ");
        hql.append(" WHERE remessa.id = :idRemessa ");
        
        TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);
        
        query.setParameter("idRemessa", id);
        
        return query.getSingleResult();
        
      } catch (Exception e) {
        return null;
      }
    }

    @Override
    public RemessaVO obterRemessaComListaDocumentos(Long id) {
      try {
        StringBuilder hql = new StringBuilder("");
        
        hql.append(" SELECT Distinct remessa  ");
        hql.append(" FROM RemessaVO remessa ");
        hql.append(" LEFT JOIN FETCH remessa.remessaDocumentos remessaDocumentos ");
        hql.append(" LEFT JOIN FETCH remessaDocumentos.documento documento ");
        hql.append(" LEFT JOIN FETCH remessaDocumentos.unidadeGeradora unidadeGeradora  ");
        hql.append(" LEFT JOIN FETCH remessa.tramiteRemessaAtual t  ");
        hql.append(" LEFT JOIN FETCH t.situacao s  ");
        hql.append(" WHERE remessa.id = :idRemessa ");
        
        TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);
        
        query.setParameter("idRemessa", id);
        
        return query.getSingleResult();
        
      } catch (Exception e) {
        return null;
      }
    }
    
    @Override
    public List<RemessaVO> findAllPendentesConfirmacaoTipoAb() {

        StringBuilder hql = new StringBuilder("Select DISTINCT r");
        hql.append(" From RemessaVO r ");
        hql.append(" Inner Join Fetch r.remessaDocumentos rd ");
        hql.append(" Join Fetch r.unidadeSolicitante ");
        hql.append(" Join Fetch r.tramiteRemessaAtual t ");
        hql.append(" Join Fetch t.situacao s ");
        hql.append(" Where s.id in (:situacoes) ");

        TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);

        List<Long> listSituacao = new ArrayList<Long>();
        listSituacao.add(SituacaoRemessaEnum.ALTERADA.getId());
        query.setParameter("situacoes", listSituacao);

        return query.getResultList();
    }
    
    @Override
    public List<RemessaVO> findAllPendentesConfirmacaoTipoC() {

        StringBuilder hql = new StringBuilder("Select DISTINCT r");
        hql.append(" From RemessaVO r ");
        hql.append(" Inner Join Fetch r.movimentosDiarioList md ");
        hql.append(" Join Fetch r.unidadeSolicitante ");
        hql.append(" Join Fetch r.tramiteRemessaAtual t ");
        hql.append(" Join Fetch t.situacao s ");
        hql.append(" Where s.id in (:situacoes) ");

        TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);

        List<Long> listSituacao = new ArrayList<Long>();
        listSituacao.add(SituacaoRemessaEnum.ALTERADA.getId());
        query.setParameter("situacoes", listSituacao);

        return query.getResultList();
    }
    
    @Override
    public RemessaVO findByIdFetchAll(RemessaVO remessaVO) {
      
      StringBuilder hql = new StringBuilder("Select r");
      hql.append(" From RemessaVO r ");
      hql.append(" Left Join Fetch r.movimentosDiarioList md ");
      hql.append(" Join Fetch r.unidadeSolicitante ");
      hql.append(" Join Fetch r.tramiteRemessaAtual t ");
      hql.append(" Join Fetch r.base b ");
      hql.append(" Join Fetch r.empresaContrato ec ");
      hql.append(" Join Fetch ec.empresa e ");
      hql.append(" Join Fetch b.unidade u ");
      hql.append(" Join Fetch t.situacao s ");
      hql.append(" Where r.id in (:id) ");

      TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);

      query.setParameter("id", remessaVO.getId());

      return query.getSingleResult();
    }

    @Override
    public List<RemessaVO> findAllConferidasConfirmadas() {
     
      StringBuilder hql = new StringBuilder(" Select DISTINCT r ");
      hql.append(" From RemessaVO r ");
      hql.append(" Join Fetch r.unidadeSolicitante ");
      hql.append(" Join Fetch r.tramiteRemessaAtual t ");
      hql.append(" Join Fetch t.situacao s ");
      hql.append(" Where s.id in (:situacoes) ");

      TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);

      List<Long> listSituacao = new ArrayList<>();
      listSituacao.add(SituacaoRemessaEnum.CONFERIDA.getId());
      listSituacao.add(SituacaoRemessaEnum.ALTERACAO_CONFIRMADA.getId());
      query.setParameter("situacoes", listSituacao);

      return query.getResultList();
    }

    @Override
    public List<RemessaVO> pesquisaAbertasHojeAB(Date hojeMeiaNoite, Date hoje) {
      
      StringBuilder hql = new StringBuilder("Select DISTINCT r");
      hql.append(" From RemessaVO r ");
      hql.append(" Left Join Fetch r.documento ");
      hql.append(" Join Fetch r.empresaContrato c ");
      hql.append(" Join Fetch c.base ");
      hql.append(" Join Fetch c.empresa ");
      hql.append(" Join Fetch r.unidadeSolicitante us");
      hql.append(" Join Fetch us.tipoUnidade ");
      hql.append(" Join Fetch r.remessaDocumentos rd ");
      hql.append(" Join Fetch rd.unidadeGeradora ");
      hql.append(" Join Fetch rd.unidadeGeradora ");
      hql.append(" Join Fetch rd.documento rddoc ");
      hql.append(" Join Fetch r.tramiteRemessaAtual t ");
      hql.append(" Join Fetch t.situacao s ");
      hql.append(" Where s.id In (:situacoes) ");
      hql.append(" And r.dataHoraAbertura BETWEEN :hojeMeiaNoite AND :hoje ");
      hql.append(" And r.codigoRemessaTipoC is null ");

      TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);

      List<Long> listSituacao = new ArrayList<>();
      listSituacao.add(SituacaoRemessaEnum.ABERTA.getId());
      listSituacao.add(SituacaoRemessaEnum.AGENDADA.getId());
      query.setParameter("situacoes", listSituacao);
      
      query.setParameter("hojeMeiaNoite", hojeMeiaNoite);
      query.setParameter("hoje", hoje);

      return query.getResultList();
    }
    
    @Override
    public List<RemessaVO> pesquisaAbertasHojeC(Date hojeMeiaNoite, Date hoje) {
      
      StringBuilder hql = new StringBuilder("Select DISTINCT r");
      hql.append(" From RemessaVO r ");
      hql.append(" Left Join Fetch r.documento ");
      hql.append(" Join Fetch r.empresaContrato c ");
      hql.append(" Join Fetch c.base ");
      hql.append(" Join Fetch c.empresa ");
      hql.append(" Join Fetch r.unidadeSolicitante us");
      hql.append(" Join Fetch us.tipoUnidade ");
      hql.append(" Join Fetch r.tramiteRemessaAtual t ");
      hql.append(" Join Fetch t.situacao s ");
      hql.append(" LEFT JOIN FETCH r.movimentosDiarioList movimentosDiarioList ");
      hql.append(" LEFT JOIN FETCH movimentosDiarioList.unidadeGeradora unidGer ");
      hql.append(" Where s.id In (:situacoes) ");
      hql.append(" And r.dataHoraAbertura BETWEEN :hojeMeiaNoite AND :hoje ");
      hql.append(" And r.codigoRemessaTipoC is not null ");

      TypedQuery<RemessaVO> query = getEntityManager().createQuery(hql.toString(), RemessaVO.class);

      List<Long> listSituacao = new ArrayList<>();
      listSituacao.add(SituacaoRemessaEnum.ABERTA.getId());
      listSituacao.add(SituacaoRemessaEnum.AGENDADA.getId());
      query.setParameter("situacoes", listSituacao);
      
      query.setParameter("hojeMeiaNoite", hojeMeiaNoite);
      query.setParameter("hoje", hoje);

      return query.getResultList();
    }
    
}
