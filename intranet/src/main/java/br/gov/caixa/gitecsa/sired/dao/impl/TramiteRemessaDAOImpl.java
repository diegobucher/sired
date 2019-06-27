package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.List;

import javax.persistence.Query;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.TramiteRemessaDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;

public class TramiteRemessaDAOImpl extends GenericDAOImpl<TramiteRemessaVO> implements TramiteRemessaDAO {

    @SuppressWarnings("unchecked")
    public List<TramiteRemessaVO> consultaTramitesEmailSolicitante() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT tramite FROM TramiteRemessaVO tramite ");
        hql.append(" WHERE tramite.dataTramiteEmail is null ");
        hql.append(" AND ( tramite.situacao.id = " + SituacaoRemessaEnum.BLOQUEADA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.AGENDADA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.ALTERADA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.INVALIDA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.FECHADA.getId() + " )");
        hql.append(" ORDER BY tramite.remessa.id, tramite.dataTramiteRemessa ");

        Query query = getEntityManager().createQuery(hql.toString(), TramiteRemessaVO.class);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<TramiteRemessaVO> consultaTramitesEmailBase() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT tramite FROM TramiteRemessaVO tramite ");
        hql.append(" WHERE tramite.dataTramiteEmail is null ");
        hql.append(" AND ( tramite.situacao.id = " + SituacaoRemessaEnum.EM_DISPUTA.getId() + " )");
        hql.append(" ORDER BY tramite.remessa.id, tramite.dataTramiteRemessa ");

        Query query = getEntityManager().createQuery(hql.toString(), TramiteRemessaVO.class);
        return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public List<TramiteRemessaVO> consultaTramitesEmailTerceirizada() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT tramite FROM TramiteRemessaVO tramite ");
        hql.append(" WHERE tramite.dataTramiteEmail is null ");
        hql.append(" AND ( tramite.situacao.id = " + SituacaoRemessaEnum.ABERTA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.RECEBIDA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.CONFERIDA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.ALTERACAO_CONFIRMADA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.ALTERACAO_DESFEITA.getId());
        hql.append(" OR tramite.situacao.id = " + SituacaoRemessaEnum.FECHADA_INCONSISTENTE.getId() + " )");
        hql.append(" ORDER BY tramite.remessa.id, tramite.dataTramiteRemessa ");

        Query query = getEntityManager().createQuery(hql.toString(), TramiteRemessaVO.class);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public String consultaTramiteAbertura(Long idRemessa) {

        List<String> usuarios;
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT tramite.codigoUsuario FROM TramiteRemessaVO tramite ");
        hql.append(" WHERE tramite.remessa.id = " + idRemessa);
        hql.append(" AND tramite.situacao.id = " + SituacaoRemessaEnum.ABERTA.getId());

        Query query = getEntityManager().createQuery(hql.toString(), String.class);

        usuarios = query.getResultList();

        if (!ObjectUtils.isNullOrEmpty(usuarios)) {
            return usuarios.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TramiteRemessaVO> findByRemessa(RemessaVO remessa) {
        StringBuilder hql = new StringBuilder();

        hql.append(" Select tramite FROM TramiteRemessaVO tramite ");
        hql.append(" Join Fetch tramite.situacao ");
        hql.append(" Where tramite.remessa = :remessa ");

        Query query = getEntityManager().createQuery(hql.toString(), TramiteRemessaVO.class);
        query.setParameter("remessa", remessa);

        return query.getResultList();
    }
}
