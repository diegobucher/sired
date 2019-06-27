package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.TramiteRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

public class TramiteRequisicaoDAOImpl extends GenericDAOImpl<TramiteRequisicaoVO> implements TramiteRequisicaoDAO {

    @SuppressWarnings("unchecked")
    @Override
    public List<TramiteRequisicaoVO> findByRequisicao(RequisicaoVO requisicao) {

        StringBuilder hql = new StringBuilder();

        hql.append(" Select tramite FROM TramiteRequisicaoVO tramite ");
        hql.append(" Join Fetch tramite.situacaoRequisicao ");
        hql.append(" Where tramite.requisicao = :requisicao ");
        hql.append(" ORDER BY tramite.dataHora ");

        Query query = getEntityManager().createQuery(hql.toString(), TramiteRequisicaoVO.class);
        query.setParameter("requisicao", requisicao);

        return query.getResultList();
    }

    public List<TramiteRequisicaoVO> consultaTramitesEmailSolicitante() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT tramite FROM TramiteRequisicaoVO tramite ");
        hql.append(" WHERE tramite.dataHoraTramiteEmail is null ");
        hql.append(" AND ( tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.CANCELADA.getId());
        hql.append(" OR tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.ATENDIDA.getId());
        hql.append(" OR tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.REATENDIDA.getId());
        // hql.append(" OR tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.FECHADA.getId());
        hql.append(" )");
        hql.append(" ORDER BY tramite.requisicao.id, tramite.dataHora ");

        TypedQuery<TramiteRequisicaoVO> query = getEntityManager().createQuery(hql.toString(), TramiteRequisicaoVO.class);
        return query.getResultList();
    }

    public List<TramiteRequisicaoVO> consultaTramitesEmailBase() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT tramite FROM TramiteRequisicaoVO tramite ");
        hql.append(" WHERE tramite.dataHoraTramiteEmail is null ");
        hql.append(" AND ( tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.EM_AUTORIZACAO.getId());
        hql.append(" OR tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.EM_TRATAMENTO.getId());
        hql.append(" OR tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.REABERTA_EM_TRATAMENTO.getId() + " )");
        hql.append(" ORDER BY tramite.requisicao.id, tramite.dataHora ");

        TypedQuery<TramiteRequisicaoVO> query = getEntityManager().createQuery(hql.toString(), TramiteRequisicaoVO.class);
        return query.getResultList();
    }

    public List<TramiteRequisicaoVO> consultaTramitesEmailTerceirizada() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT tramite FROM TramiteRequisicaoVO tramite ");
        hql.append(" WHERE tramite.dataHoraTramiteEmail is null ");
        hql.append(" AND ( tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.ABERTA.getId());
        hql.append(" OR tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId());
        hql.append(" OR tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.REABERTA.getId());
        hql.append(" OR tramite.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId() + " )");
        hql.append(" ORDER BY tramite.requisicao.id, tramite.dataHora ");

        TypedQuery<TramiteRequisicaoVO> query = getEntityManager().createQuery(hql.toString(), TramiteRequisicaoVO.class);
        return query.getResultList();
    }

    public List<TramiteRequisicaoVO> findAtendimentosRequisicao(RequisicaoVO requisicao) {
        StringBuilder hql = new StringBuilder();

        List<Long> listTramite = new ArrayList<Long>();
        listTramite.add(SituacaoRequisicaoEnum.ATENDIDA.getId());
        listTramite.add(SituacaoRequisicaoEnum.REATENDIDA.getId());

        hql.append(" Select tramite FROM TramiteRequisicaoVO tramite ");
        hql.append(" Join Fetch tramite.situacaoRequisicao ");
        hql.append(" Where tramite.requisicao = :requisicao ");
        hql.append(" And tramite.situacaoRequisicao.id in (:situacoes) ");
        hql.append(" ORDER BY tramite.dataHora ");

        TypedQuery<TramiteRequisicaoVO> query = getEntityManager().createQuery(hql.toString(), TramiteRequisicaoVO.class);
        query.setParameter("requisicao", requisicao);
        query.setParameter("situacoes", listTramite);

        return query.getResultList();
    }

}
