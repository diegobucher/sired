package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dto.FiltroRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.RequisicaoDAO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class RequisicaoDAOImpl extends GenericDAOImpl<RequisicaoVO> implements RequisicaoDAO {

    private static final String SEPARADOR_NUMERO_REQUISICOES = ",";

    @SuppressWarnings("unchecked")
    public List<RequisicaoVO> regraVerificarRequisicaoComSitRascunho(RequisicaoVO requisicao) {
        String sHql = "select req from RequisicaoVO as req " + " join req.documento as doc " + " join req.tramiteRequisicoes tra "
                + " join tra.situacaoRequisicao st " + " where tra.dataHora = ( " + " 	select max(tra1.dataHora) from TramiteRequisicaoVO tra1 "
                + " 	where tra1.requisicao.id = req.id ) " + " and st.nome = :pSituacao " + " and doc.id = :pDocumento" + " and req.unidade.id = :pUnidade "
                + " and req.id != :pId ";

        org.hibernate.Query query = getSession().createQuery(sHql);
        query.setParameter("pSituacao", "RASCUNHO");
        query.setParameter("pDocumento", requisicao.getDocumento().getId());
        query.setParameter("pUnidade", requisicao.getUnidadeSolicitante().getId());
        query.setParameter("pId", requisicao.getId());

        return (List<RequisicaoVO>) query.list();
    }

    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, Integer offset, Integer limit) {
        return this.consultar(filtro, null, offset, limit);
    }

    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro) {
        return this.consultar(filtro, null, null, null);
    }

    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas) {
        return this.consultar(filtro, unidadesAutorizadas, null, null);
    }

    @SuppressWarnings("unchecked")
    public List<RequisicaoVO> consultar(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas, Integer offset, Integer limit) {
        StringBuilder hql = new StringBuilder("Select Distinct requisicao ");
        hql.append(" From RequisicaoVO requisicao ");
        hql.append(" Join Fetch requisicao.unidadeSolicitante ");
        hql.append(" Join Fetch requisicao.documento documento ");
        hql.append(" Join Fetch documento.grupo grupo ");
        hql.append(" Join Fetch grupo.grupoCampos grupoCampos ");
        hql.append(" Join Fetch grupoCampos.campo ");
        hql.append(" Join Fetch requisicao.requisicaoDocumento reqDocumento ");
        hql.append(" Join Fetch reqDocumento.unidadeGeradora unidadeGeradora ");
        hql.append(" Join Fetch unidadeGeradora.uf ");
        hql.append(" Join Fetch reqDocumento.tipoDemanda ");
        hql.append(" Left Join Fetch reqDocumento.operacao ");
        hql.append(" Join Fetch requisicao.tramiteRequisicaoAtual tramite ");
        hql.append(" Join Fetch tramite.situacaoRequisicao ");
        hql.append(" Left Join Fetch tramite.ocorrencia ");
        hql.append(" Left Join fetch tramite.suporte ");
        hql.append(" Left Join Fetch requisicao.empresaContrato contrato ");
        hql.append(" Left Join Fetch contrato.base ");
        hql.append(" left join fetch contrato.empresa empresa ");
        hql.append(" Where 0 = 0 ");

        hql.append(this.buildSqlConsulta(filtro, unidadesAutorizadas));
        hql.append(" order by requisicao.id asc ");

        Query query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);
        query = this.bindParametersConsulta(query, filtro, unidadesAutorizadas);

        if (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) {
            return query.setFirstResult(offset).setMaxResults(limit).getResultList();
        } else {
            return query.getResultList();
        }
    }

    private StringBuilder buildSqlConsulta(FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas) {

        StringBuilder hql = new StringBuilder();

        hql.append(" AND requisicao.empresaContrato.empresa.id = :idEmpresa ");

        if (StringUtils.isNotBlank(filtro.getNumeroRequisicoes())) {
            hql.append(" AND requisicao.codigoRequisicao in (:ids) ");
        } else {
            if (!ObjectUtils.isNullOrEmpty(filtro.getUnidadeSolicitante()) && (filtro.getUnidadeSolicitante().getId() != null)) {
                hql.append(" AND requisicao.unidadeSolicitante = :unidadeSolicitante ");
            }

            if (!ObjectUtils.isNullOrEmpty(filtro.getBase())) {
                hql.append(" AND contrato.base = :base ");
            } else if (!ObjectUtils.isNullOrEmpty(unidadesAutorizadas)) {
                hql.append(" AND requisicao.unidadeSolicitante in (:unidadesAutorizadas) ");
            }

            if ((filtro.getSituacao() == null) || (filtro.getSituacao().getId().equals(SituacaoRequisicaoEnum.EM_TRATAMENTO.getId()))
                    || (filtro.getSituacao().getId().equals(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId()))
                    || (filtro.getSituacao().getId().equals(SituacaoRequisicaoEnum.PEND_DADOS_FAT.getId()))) {
                hql.append(" AND tramite.situacaoRequisicao.id in (:situacao) ");
            } else {
                hql.append(" AND tramite.situacaoRequisicao.id = :situacao ");
            }

            if (!ObjectUtils.isNullOrEmpty(filtro.getDataInicio()) && !ObjectUtils.isNullOrEmpty(filtro.getDataFim())) {
                hql.append(" AND requisicao.dataHoraAbertura between :dataInicio and :dataFim ");
            }
        }

        return hql;
    }

    private Query bindParametersConsulta(Query query, FiltroRequisicaoDTO filtro, List<UnidadeVO> unidadesAutorizadas) {
        List<Long> pendentes = new ArrayList<Long>();
        pendentes.add(SituacaoRequisicaoEnum.ABERTA.getId());
        pendentes.add(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId());
        pendentes.add(SituacaoRequisicaoEnum.PEND_DADOS_FAT.getId());
        pendentes.add(SituacaoRequisicaoEnum.REABERTA.getId());
        pendentes.add(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId());
        pendentes.add(SituacaoRequisicaoEnum.REABERTA_PEND_DADOS_FAT.getId());

        List<Long> emTratamento = new ArrayList<Long>();
        emTratamento.add(SituacaoRequisicaoEnum.EM_TRATAMENTO.getId());
        emTratamento.add(SituacaoRequisicaoEnum.REABERTA_EM_TRATAMENTO.getId());

        List<Long> pendenteUpload = new ArrayList<Long>();
        pendenteUpload.add(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId());
        pendenteUpload.add(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId());

        List<Long> pendenteAtendimento = new ArrayList<Long>();
        pendenteAtendimento.add(SituacaoRequisicaoEnum.PEND_DADOS_FAT.getId());
        pendenteAtendimento.add(SituacaoRequisicaoEnum.REABERTA_PEND_DADOS_FAT.getId());

        query.setParameter("idEmpresa", filtro.getEmpresa().getId());

        if (StringUtils.isNotBlank(filtro.getNumeroRequisicoes())) {
            List<Long> ids = new ArrayList<Long>();
            String[] requisicoes = StringUtils.split(filtro.getNumeroRequisicoes(), SEPARADOR_NUMERO_REQUISICOES);

            for (String id : requisicoes) {
                ids.add(Long.valueOf(id));
            }

            query.setParameter("ids", ids);
        } else {
            if (!ObjectUtils.isNullOrEmpty(filtro.getUnidadeSolicitante()) && (filtro.getUnidadeSolicitante().getId() != null)) {
                query.setParameter("unidadeSolicitante", filtro.getUnidadeSolicitante());
            }

            if (!ObjectUtils.isNullOrEmpty(filtro.getBase())) {
                query.setParameter("base", filtro.getBase());
            } else if (!ObjectUtils.isNullOrEmpty(unidadesAutorizadas)) {
                query.setParameter("unidadesAutorizadas", unidadesAutorizadas);
            }

            if (filtro.getSituacao() == null) {
                query.setParameter("situacao", pendentes);
            } else if (filtro.getSituacao().getId().equals(SituacaoRequisicaoEnum.EM_TRATAMENTO.getId())) {
                query.setParameter("situacao", emTratamento);
            } else if (filtro.getSituacao().getId().equals(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId())) {
                query.setParameter("situacao", pendenteUpload);
            } else if (filtro.getSituacao().getId().equals(SituacaoRequisicaoEnum.PEND_DADOS_FAT.getId())) {
                query.setParameter("situacao", pendenteAtendimento);
            } else {
                query.setParameter("situacao", filtro.getSituacao().getId());
            }

            if (!ObjectUtils.isNullOrEmpty(filtro.getDataInicio()) && !Util.isNullOuVazio(filtro.getDataFim())) {
                query.setParameter("dataInicio", filtro.getDataInicio());
                query.setParameter("dataFim", filtro.getDataFim());
            }
        }

        return query;
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

    public RequisicaoVO obterPorNumeroID(Long numeroIdentificacao) {
        StringBuilder hql = new StringBuilder();

        hql.append("SELECT requisicao FROM RequisicaoVO requisicao ");
        hql.append(" JOIN FETCH requisicao.tramiteRequisicaoAtual tramite ");
        hql.append(" JOIN FETCH tramite.situacaoRequisicao situacao");
        hql.append(" JOIN FETCH requisicao.empresaContrato empresaContrato");
        hql.append(" JOIN FETCH empresaContrato.empresa empresa");
        hql.append(" WHERE ");
        hql.append(" requisicao.codigoRequisicao = :codigoRequisicao ");

        Query query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);
        query.setParameter("codigoRequisicao", numeroIdentificacao);

        try {
            return (RequisicaoVO) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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
        hql.append(" Join Fetch documento.grupo grupo ");
        hql.append(" Left Join Fetch reqDocumento.operacao ");
        hql.append(" Left Join Fetch grupo.grupoCampos grpCampos ");
        hql.append(" Left Join Fetch grpCampos.campo ");
        hql.append(" Join Fetch requisicao.tramiteRequisicaoAtual tramite ");
        hql.append(" Join Fetch tramite.situacaoRequisicao ");
        hql.append(" Left Join Fetch requisicao.empresaContrato contrato ");
        hql.append(" Left Join Fetch contrato.base ");
        hql.append(" Where requisicao.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), RequisicaoVO.class);
        query.setParameter("id", id);

        try {
            return (RequisicaoVO) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
