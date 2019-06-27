package br.gov.caixa.gitecsa.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless
public class RemessaService extends AbstractService<RemessaVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @Inject
    private EmpresaContratoService empresaContratoService;

    @Inject
    private TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private TramiteRemessaService tramiteRemessaService;

    @Override
    protected void validaCampos(RemessaVO entity) {

    }

    @Override
    protected void validaRegras(RemessaVO entity) {
//        consultarContratoVigenteUnidadeSolicitante(entity);
    }

    @Override
    protected void validaRegrasExcluir(RemessaVO entity) {

    }

    /**
     * Método que remove uma entidade. Foi sobrescrito para realizar a remoção dos trâmites antes da operação de delete.
     * 
     * @param entity
     * @throws BusinessException
     * @throws Exception
     */
    @Override
    protected void deleteImpl(RemessaVO entity) throws AppException {

        // Remove os tramites da remessa
        tramiteRemessaService.excluirByRemessa(entity);
        // Remove a Remessa
        super.deleteImpl(entity);

    }

    /**
     * Verifica se existe contrato vigente para abrir a remessa. Tem que haver uma empresa contrato vinculada a Base da Unidade Solicitante
     * da Remessa.
     * 
     * @param vo
     */
    private void consultarContratoVigenteUnidadeSolicitante(RemessaVO vo) {
        try {
            EmpresaContratoVO retorno = empresaContratoService.buscarContratoVigente(vo.getUnidadeSolicitante());
            if (retorno == null) {
                List<BaseVO> baseVOs = tramiteRequisicaoDocumentoService
                        .consultaBasePorIdUnidade(Long.parseLong(vo.getUnidadeSolicitante().getId().toString()));
                BaseVO baseVO = baseVOs.get(0);
                String nomeBase = Util.isNullOuVazio(baseVO) ? "" : baseVO.getNome();

                mensagens.add(MensagemUtils.obterMensagem("MA060", nomeBase));
            } else {
                vo.setEmpresaContrato(retorno);
            }
        } catch (AppException e) {
            mensagens.add(MensagemUtils.obterMensagem("MA060"));
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), "RemessaService",
                    "consultarContratoVigenteUnidadeSolicitante"));
        }

    }

    @SuppressWarnings({ "unchecked" })
    public List<RemessaVO> consultarRemessa(Long numeroRemessa, Date dataInicio, Date dataTermino, String matricula, UnidadeVO unidade,
            SituacaoRemessaEnum situacao, BaseVO baseVO, int inicio, int fim) throws AppException {

        Criteria criteria = getSession().createCriteria(RemessaVO.class, "this");
        criteria.createAlias("unidadeSolicitante", "unidade", JoinType.INNER_JOIN);
        criteria.createAlias("empresaContrato", "contrato", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("contrato.base", "base", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("tramiteRemessaAtual", "tramite", JoinType.INNER_JOIN);
        criteria.createAlias("tramite.situacao", "situacao", JoinType.INNER_JOIN);

        if (!Util.isNullOuVazio(numeroRemessa)) {
            criteria.add(Restrictions.eq("id", numeroRemessa));
        }

        if (!Util.isNullOuVazio(matricula)) {
            criteria.add(Restrictions.eq("codigoUsuarioAbertura", matricula).ignoreCase());
        }

        if (!Util.isNullOuVazio(unidade)) {
            criteria.add(Restrictions.eq("unidadeSolicitante", unidade));
        }

        if (!Util.isNullOuVazio(baseVO)) {
            criteria.add(Restrictions.eq("contrato.base", baseVO));
        }

        if (!Util.isNullOuVazio(situacao)) {
            // Se a situação for diferente de TODAS, então efetua o filtro da
            // consulta
            if (!situacao.getId().equals(SituacaoRemessaEnum.TODAS.getId())) {
                criteria.add(Restrictions.eq("situacao.id", situacao.getId()));
            }
        } else {
            // Usuário deixou a opção default - PENDENTES (RASCUNHO, BLOQUEADA,
            // DEVOLVIDA PARA CORREÇÃO ou INVÁLIDA(INCONSISTENTE))
            criteria.add(Restrictions.in("situacao.id", SituacaoRemessaEnum.valuesSituacoesPendentes()));
        }

        if (!Util.isNullOuVazio(dataInicio) && Util.isNullOuVazio(dataTermino)) {
            dataTermino = Calendar.getInstance().getTime();
        }

        if (!Util.isNullOuVazio(dataInicio) && !Util.isNullOuVazio(dataTermino)) {
            Calendar dateStart = Calendar.getInstance();
            dateStart.setTime(dataInicio);
            dateStart.set(Calendar.HOUR, 0);
            dateStart.set(Calendar.MINUTE, 0);
            dateStart.set(Calendar.SECOND, 0);

            Calendar dateEnd = Calendar.getInstance();
            dateEnd.setTime(dataTermino);
            dateEnd.set(Calendar.HOUR, 23);
            dateEnd.set(Calendar.MINUTE, 59);
            dateEnd.set(Calendar.SECOND, 59);
            dataTermino = dateEnd.getTime();
            criteria.add(Restrictions.between("dataHoraAbertura", dateStart.getTime(), dateEnd.getTime()));

        }
        criteria.addOrder(Order.asc("id"));
        List<RemessaVO> list = (List<RemessaVO>) criteria.setFirstResult(inicio).setMaxResults(fim).list();
        for (RemessaVO remessa : list) {
            Hibernate.initialize(remessa.getRemessaDocumentos());
            Hibernate.initialize(remessa.getMovimentosDiarioList());
            for (RemessaDocumentoVO remessaDocumentoVO : remessa.getRemessaDocumentos()) {
                Hibernate.initialize(remessaDocumentoVO.getUnidadeGeradora().getUf());
                Hibernate.initialize(remessaDocumentoVO.getDocumento());
            }
        }
        return list;
    }

    public int countConsultarRemessa(Long numeroRemessa, Date dataInicio, Date dataTermino, String matricula, UnidadeVO unidade, SituacaoRemessaEnum situacao,
            BaseVO baseVO) throws AppException {

        Criteria criteria = getSession().createCriteria(RemessaVO.class, "this");
        criteria.createAlias("unidadeSolicitante", "unidade", JoinType.INNER_JOIN);
        criteria.createAlias("empresaContrato", "contrato", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("contrato.base", "base", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("tramiteRemessaAtual", "tramite", JoinType.INNER_JOIN);
        criteria.createAlias("tramite.situacao", "situacao", JoinType.INNER_JOIN);

        if (!Util.isNullOuVazio(numeroRemessa)) {
            criteria.add(Restrictions.eq("id", numeroRemessa));
        }

        if (!Util.isNullOuVazio(matricula)) {
            criteria.add(Restrictions.eq("codigoUsuarioAbertura", matricula).ignoreCase());
        }

        if (!Util.isNullOuVazio(unidade)) {
            criteria.add(Restrictions.eq("unidadeSolicitante", unidade));
        }

        if (!Util.isNullOuVazio(baseVO)) {
            criteria.add(Restrictions.eq("contrato.base", baseVO));
        }

        if (!Util.isNullOuVazio(situacao)) {
            // Se a situação for diferente de TODAS, então efetua o filtro da
            // consulta
            if (!situacao.getId().equals(SituacaoRemessaEnum.TODAS.getId())) {
                criteria.add(Restrictions.eq("situacao.id", situacao.getId()));
            }
        } else {
            // Usuário deixou a opção default - PENDENTES (RASCUNHO, BLOQUEADA,
            // DEVOLVIDA PARA CORREÇÃO ou INVÁLIDA(INCONSISTENTE))
            criteria.add(Restrictions.in("situacao.id", SituacaoRemessaEnum.valuesSituacoesPendentes()));
        }

        if (!Util.isNullOuVazio(dataInicio) && Util.isNullOuVazio(dataTermino)) {
            dataTermino = Calendar.getInstance().getTime();
        }

        if (!Util.isNullOuVazio(dataInicio) && !Util.isNullOuVazio(dataTermino)) {
            Calendar dateStart = Calendar.getInstance();
            dateStart.setTime(dataInicio);
            dateStart.set(Calendar.HOUR, 0);
            dateStart.set(Calendar.MINUTE, 0);
            dateStart.set(Calendar.SECOND, 0);

            Calendar dateEnd = Calendar.getInstance();
            dateEnd.setTime(dataTermino);
            dateEnd.set(Calendar.HOUR, 23);
            dateEnd.set(Calendar.MINUTE, 59);
            dateEnd.set(Calendar.SECOND, 59);
            dataTermino = dateEnd.getTime();
            criteria.add(Restrictions.between("dataHoraAbertura", dateStart.getTime(), dateEnd.getTime()));

        }

        criteria.setProjection(Projections.rowCount());
        return (Integer.parseInt(criteria.uniqueResult().toString()));
    }
}
