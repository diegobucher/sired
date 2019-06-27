package br.gov.caixa.gitecsa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;

import br.gov.caixa.gitecsa.dto.RelatorioAtendimentoDTO;
import br.gov.caixa.gitecsa.dto.RelatorioFaturamentoDTO;
import br.gov.caixa.gitecsa.dto.RelatorioSuporteDTO;
import br.gov.caixa.gitecsa.dto.ResultadoAtendimentoDTO;
import br.gov.caixa.gitecsa.dto.TramiteRequisicaoSuporteDTO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.OcorrenciaAtendimentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoRestricaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSuporteEnum;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;

@Stateless
public class TramiteRequisicaoDocumentoService {

    private static final long serialVersionUID = 1L;

    // @Inject
    // @DataRepository
    @PersistenceContext(unitName = "siredPU")
    private EntityManager entityManagerSistema;

    /**
     * Consulta os tramites de atendimento de requisições realizados no período passado no parâmetro.
     * 
     * @param relatorioAtendimento
     * @return
     * @throws AppException
     */
    public List<ResultadoAtendimentoDTO> consultaRelatorioAtendimento(RelatorioAtendimentoDTO relatorioAtendimento) throws AppException {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT new br.gov.caixa.gitecsa.dto.ResultadoAtendimentoDTO( base.nome, date_trunc('day',tramiteAtual.dataHoraAtendimento), ");
        hql.append(" doc.nome, SUM(req.qtSolicitadaDocumento), ");
        hql.append(" SUM(coalesce(tramiteAtual.qtdDisponibilizadaDocumento,0)), ");
        hql.append(" SUM(req.qtSolicitadaDocumento) - SUM(coalesce(tramiteAtual.qtdDisponibilizadaDocumento,0)) ) ");
        hql.append(" FROM RequisicaoVO req ");
        hql.append(" INNER JOIN req.requisicaoDocumento reqdoc ");
        hql.append(" INNER JOIN req.documento doc ");
        hql.append(" INNER JOIN req.tramiteRequisicaoAtual tramiteAtual ");
        hql.append(" INNER JOIN req.empresaContrato empCont ");
        hql.append(" INNER JOIN empCont.base base ");

        Map<String, Object> parameters = new HashMap<String, Object>();

        hql.append(" WHERE tramiteAtual.dataHoraAtendimento BETWEEN :pDataInicio AND :pDataFim  ");
        hql.append(" AND tramiteAtual.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.FECHADA.getId());

        parameters.put("pDataInicio", relatorioAtendimento.getDataInicio());
        Date pDataFim = new Date(relatorioAtendimento.getDataFim().getTime() + TimeUnit.DAYS.toMillis(1));
        parameters.put("pDataFim", pDataFim);
        if (relatorioAtendimento.getBase() != null) {
            hql.append(" AND empCont.base = :pBase ");
            parameters.put("pBase", relatorioAtendimento.getBase());
        }

        hql.append(" GROUP BY base.nome, date_trunc('day',tramiteAtual.dataHoraAtendimento), doc.nome ");
        hql.append(" ORDER BY base.nome, date_trunc('day',tramiteAtual.dataHoraAtendimento), doc.nome ");
        Query query = entityManagerSistema.createQuery(hql.toString(), ResultadoAtendimentoDTO.class);

        for (String key : parameters.keySet()) {
            query.setParameter(key, parameters.get(key));
        }

        @SuppressWarnings("unchecked")
        List<ResultadoAtendimentoDTO> lista = query.getResultList();

        return lista;

    }

    protected Session getSession() {
        return entityManagerSistema.unwrap(Session.class);
    }

    @SuppressWarnings("unchecked")
    public List<TramiteRequisicaoSuporteDTO> consultaRelatorioSuporte(RelatorioSuporteDTO relatorio) throws AppException {

        Criteria criteria = getSession().createCriteria(RequisicaoVO.class, "r").createAlias("r.requisicaoDocumento", "reqdoc", JoinType.INNER_JOIN)
                .createAlias("r.empresaContrato", "empCont", JoinType.INNER_JOIN).createAlias("r.tramiteRequisicaoAtual", "tramite", JoinType.INNER_JOIN)
                .createAlias("tramite.suporte", "suporte", JoinType.INNER_JOIN).createAlias("empCont.base", "base", JoinType.INNER_JOIN);

        ProjectionList proj = Projections.projectionList();

        proj.add(Projections.sqlProjection("sum(case when tramite3_.nu_suporte_a09=" + TipoSuporteEnum.PAPEL.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadePapel", new String[] { "quantidadePapel" }, new Type[] { new IntegerType() }));
        proj.add(Projections.sqlProjection("sum(case when tramite3_.nu_suporte_a09 =" + TipoSuporteEnum.MICROFICHA.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadeMicroficha", new String[] { "quantidadeMicroficha" },
                new Type[] { new IntegerType() }));
        proj.add(Projections.sqlProjection("sum(case when tramite3_.nu_suporte_a09 =" + TipoSuporteEnum.MICROFILME.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadeMicrofilme", new String[] { "quantidadeMicrofilme" },
                new Type[] { new IntegerType() }));
        proj.add(Projections.sqlProjection("sum(case when tramite3_.nu_suporte_a09 =" + TipoSuporteEnum.MIDIAOPTICA.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadeMidiaOptica", new String[] { "quantidadeMidiaOptica" },
                new Type[] { new IntegerType() }));
        proj.add(Projections.sqlProjection("sum(case when tramite3_.nu_suporte_a09 =" + TipoSuporteEnum.REPOSITORIO.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadeRepositorio", new String[] { "quantidadeRepositorio" },
                new Type[] { new IntegerType() }));
        proj.add(Projections.groupProperty("empCont.base"), "base");
        proj.add(Projections.groupProperty("tramite.dataHoraAtendimento"), "dataHora");
        proj.add(Projections.groupProperty("base.nome"));

        criteria.setProjection(proj);

        // Considerar apenas requisições que estejam fechadas.
        criteria.add(Restrictions.eq("tramite.situacaoRequisicao.id", SituacaoRequisicaoEnum.FECHADA.getId()));

        if (relatorio.getBase() != null) {
            criteria.add(Restrictions.eq("empCont.base", relatorio.getBase()));
        }

        if (relatorio.getDataInicio() != null && relatorio.getDataFim() != null) {
            Date pDataFim = new Date(relatorio.getDataFim().getTime() + TimeUnit.DAYS.toMillis(1));
            criteria.add(Restrictions.between("tramite.dataHoraAtendimento", relatorio.getDataInicio(), pDataFim));
        }

        criteria.addOrder(Order.asc("base.nome"));
        criteria.addOrder(Order.asc("tramite.dataHoraAtendimento"));

        criteria.setResultTransformer(Transformers.aliasToBean(TramiteRequisicaoSuporteDTO.class));

        // FIXME: Workaround. Esse código precisa ser totalmente refatorado
        List<TramiteRequisicaoSuporteDTO> list = (List<TramiteRequisicaoSuporteDTO>) criteria.list();
        for (TramiteRequisicaoSuporteDTO o : list) {
            o.getBase().getNome();
        }

        return list;

    }

    /**
     * Consulta os atendimentos realizados no período.
     * 
     * @param relatorio
     * @return
     * @throws AppException
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public List<RelatorioFaturamentoDTO> consultaRelatorioFaturamento(RelatorioSuporteDTO relatorio) throws AppException {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT req FROM RequisicaoVO req ");
        hql.append("    JOIN FETCH req.documento ");
        hql.append("    JOIN FETCH req.requisicaoDocumento reqdoc ");
        hql.append("    JOIN FETCH req.tramiteRequisicaoAtual tramiteAtual ");
        hql.append("    JOIN FETCH tramiteAtual.ocorrencia ocorrencia ");
        hql.append("    JOIN FETCH req.empresaContrato empCont ");
        hql.append("    JOIN FETCH empCont.base base ");
        hql.append("    LEFT JOIN FETCH tramiteAtual.suporte suporte ");

        Map<String, Object> parameters = new HashMap<String, Object>();

        // hql.append(" WHERE tramiteAtual.dataHoraAtendimento BETWEEN :pDataInicio AND :pDataFim  ");
        hql.append(" WHERE tramiteAtual.dataHora BETWEEN :pDataInicio AND :pDataFim  ");
        hql.append(" AND tramiteAtual.situacaoRequisicao.id = " + SituacaoRequisicaoEnum.FECHADA.getId());

        parameters.put("pDataInicio", DateUtils.fitAtStart(relatorio.getDataInicio()));
        parameters.put("pDataFim", DateUtils.fitAtEnd(relatorio.getDataFim()));

        if (relatorio.getBase() != null) {
            hql.append(" AND empCont.base = :pBase ");
            parameters.put("pBase", relatorio.getBase());
        }

        List<Long> listOcorrenciasIgnoradas = new ArrayList<Long>();

        listOcorrenciasIgnoradas.add(OcorrenciaAtendimentoEnum.NAO_RECEPCIONADO.getValor());
        listOcorrenciasIgnoradas.add(OcorrenciaAtendimentoEnum.INCONSISTENCIA.getValor());
        listOcorrenciasIgnoradas.add(OcorrenciaAtendimentoEnum.PRAZO_EXPIRADO.getValor());
        listOcorrenciasIgnoradas.add(OcorrenciaAtendimentoEnum.SEM_MOVIMENTAÇAO.getValor());
        listOcorrenciasIgnoradas.add(OcorrenciaAtendimentoEnum.SEM_MICROFORMAS.getValor());

        hql.append(" AND tramiteAtual.ocorrencia.id NOT IN (:pOcorrencias) ");
        parameters.put("pOcorrencias", listOcorrenciasIgnoradas);

        hql.append(" ORDER BY tramiteAtual.dataHoraAtendimento ");
        Query query = entityManagerSistema.createQuery(hql.toString(), RequisicaoVO.class);

        for (String key : parameters.keySet()) {
            query.setParameter(key, parameters.get(key));
        }

        List<RequisicaoVO> lista = query.getResultList();

        List<RelatorioFaturamentoDTO> listaRelatorioFaturamentoDTO = new ArrayList<RelatorioFaturamentoDTO>();

        for (RequisicaoVO requisicao : lista) {

            RelatorioFaturamentoDTO relatorioFaturamentoDTO = new RelatorioFaturamentoDTO();

            relatorioFaturamentoDTO.setRequisicao(requisicao);
            relatorioFaturamentoDTO.setBase(requisicao.getEmpresaContrato().getBase());
            relatorioFaturamentoDTO.setDocumento(requisicao.getDocumento());
            relatorioFaturamentoDTO.setDataAbertura(requisicao.getDataHoraAbertura());
            relatorioFaturamentoDTO.setPrazoAtendimento(requisicao.getPrazoAtendimento());
            relatorioFaturamentoDTO.setDataAtendimento(requisicao.getTramiteRequisicaoAtual().getDataHoraAtendimento());

            relatorioFaturamentoDTO.setQtdSolicitada(requisicao.getQtSolicitadaDocumento());

            relatorioFaturamentoDTO.setSuporte(requisicao.getTramiteRequisicaoAtual().getSuporte());

            if (ObjectUtils.isNullOrEmpty(relatorioFaturamentoDTO.getSuporte())) {
                // Necessário para que o agrupamento do subtotal funcione corretamente.
                SuporteVO suporte = new SuporteVO();
                suporte.setId(-1L);
                suporte.setNome(StringUtils.EMPTY);
                relatorioFaturamentoDTO.setSuporte(suporte);
            }

            if (!ObjectUtils.isNullOrEmpty(requisicao.getTramiteRequisicaoAtual().getQtdDisponibilizadaDocumento())) {
                relatorioFaturamentoDTO.setQtdDisponibilizada(requisicao.getTramiteRequisicaoAtual().getQtdDisponibilizadaDocumento());
            } else {
                relatorioFaturamentoDTO.setQtdDisponibilizada(0);
            }

            // caso a data de atendimento não seja maior que a do prazo, então a requisição foi atendida no prazo.
            if (!DateUtils.ehDataMaior(requisicao.getTramiteRequisicaoAtual().getDataHoraAtendimento(), requisicao.getPrazoAtendimento())) {
                relatorioFaturamentoDTO.setQtdDispNoPrazo(requisicao.getTramiteRequisicaoAtual().getQtdDisponibilizadaDocumento());
            }

            if (ObjectUtils.isNullOrEmpty(relatorioFaturamentoDTO.getQtdDispNoPrazo())) {
                relatorioFaturamentoDTO.setQtdDispNoPrazo(0);
            }

            if (OcorrenciaAtendimentoEnum.NAO_LOCALIZADO.getValor().equals(requisicao.getTramiteRequisicaoAtual().getOcorrencia().getId())) {
                relatorioFaturamentoDTO.setQtdNaoLocalizada(requisicao.getQtSolicitadaDocumento());
            } else {
                relatorioFaturamentoDTO.setQtdNaoLocalizada(0);
            }

            listaRelatorioFaturamentoDTO.add(relatorioFaturamentoDTO);
        }
        return listaRelatorioFaturamentoDTO;

    }

    /**
     * Método responsável por efetuar o tratamento de uma requisição documento em autenticação e alterá-lo para malote
     */
    public boolean tratarRequisicaoDocumento(Object idtramiteAtendimento) throws AppException {
        /*
         * Query query = entityManagerSistema.createQuery(
         * "update TramiteRequisicaoDocumentoVO set ocorrenciaAtendimento.id = :idOcorrencia" + " where id = :idtramiteAtendimento");
         * query.setParameter("idOcorrencia", OcorrenciaAtendimentoEnum. .MALOTE.getId()); query.setParameter("idtramiteAtendimento",
         * idtramiteAtendimento);
         * 
         * if (query.executeUpdate() != 1) { return false; } return true;
         */
        throw new AppException();

    }

    /**
     * Consulta a lista de bases associadas a uma Unidade.
     * 
     * @param idUnidade
     * @return
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<BaseVO> consultaBasePorIdUnidade(long idUnidade) throws AppException {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT base FROM BaseVO base, CTDAVO ctda, CTDAUFVO ufctda, UnidadeVO unidade ");
        hql.append(" WHERE base = ctda.base");
        hql.append(" AND ufctda.ctda = ctda");
        hql.append(" AND unidade.uf = ufctda.uf");
        hql.append(" AND unidade.id = " + idUnidade);
        hql.append(" AND unidade NOT IN ( SELECT rest.unidade FROM CTDAUFRestricaoVO rest ");
        hql.append(" WHERE rest.ctdaUF = ufctda AND rest.tipoResticao = " + TipoRestricaoEnum.EXCLUIR.getValor() + " )");

        Query query = entityManagerSistema.createQuery(hql.toString(), BaseVO.class);

        return query.getResultList();

    }
}
