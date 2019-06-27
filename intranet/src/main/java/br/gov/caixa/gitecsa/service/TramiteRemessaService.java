package br.gov.caixa.gitecsa.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.dto.RelatorioSuporteDTO;
import br.gov.caixa.gitecsa.dto.TramiteRequisicaoSuporteDTO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSuporteEnum;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class TramiteRemessaService extends AbstractService<TramiteRemessaVO> {

    private static final long serialVersionUID = 1L;

    // @Inject
    // @DataRepository
    // private EntityManager entityManagerSistema;

    @Inject
    private br.gov.caixa.gitecsa.sired.service.FeriadoService feriadoService;

    @SuppressWarnings("unchecked")
    public void excluirByRemessa(RemessaVO remessaVO) throws AppException {
        Criteria criteria = getSession().createCriteria(TramiteRemessaVO.class, "tramite");
        criteria.add(Restrictions.eq("tramite.remessa.id", remessaVO.getId()));
        List<TramiteRemessaVO> list = (List<TramiteRemessaVO>) criteria.list();
        for (TramiteRemessaVO tramiteRemessaVO : list) {
            super.delete(tramiteRemessaVO);
        }
        remessaVO.setTramiteRemessas(null);

    }

    public TramiteRemessaVO salvarTramiteRemessaRascunho(RemessaVO remessaVO) throws AppException {
        return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.RASCUNHO.getId()));

    }

    public TramiteRemessaVO salvarTramiteRemessaAberta(RemessaVO remessaVO) throws AppException {
        return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.ABERTA.getId()));
    }

    public TramiteRemessaVO salvarTramiteRemessaAlteracaoConfirmada(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.ALTERACAO_CONFIRMADA.getId()));
  }
    
    public TramiteRemessaVO salvarTramiteRemessaEmDisputa(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.EM_DISPUTA.getId()));
  }
    
    public TramiteRemessaVO salvarTramiteRemessaInvalida(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.INVALIDA.getId()));
  }
    
    public TramiteRemessaVO salvarTramiteAlteracaoDesfeita(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.ALTERACAO_DESFEITA.getId()));
  }
    
    public TramiteRemessaVO salvarTramiteRemessaFechada(RemessaVO remessaVO) throws AppException {
      return salvarTramiteRemesssa(remessaVO, this.buscarSituacaoRemessa(SituacaoRemessaEnum.FECHADA.getId()));
  }

    public TramiteRemessaVO salvarTramiteRemessaAgendada(RemessaVO remessaVO) throws AppException {
        TramiteRemessaVO vo = new TramiteRemessaVO();
        vo.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());
        vo.setDataTramiteRemessa(new Date());
        vo.setDataAgendamento(feriadoService.findProximaDataUtil(new Date(), 1, remessaVO.getUnidadeSolicitante()));
        vo.setRemessa(remessaVO);
        vo.setSituacao(this.buscarSituacaoRemessa(SituacaoRemessaEnum.AGENDADA.getId()));
        return this.save(vo);
    }

    private TramiteRemessaVO salvarTramiteRemesssa(RemessaVO remessaVO, SituacaoRemessaVO situacao) throws AppException {
        TramiteRemessaVO vo = new TramiteRemessaVO();
        vo.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());
        vo.setDataTramiteRemessa(new Date());
        vo.setRemessa(remessaVO);
        vo.setSituacao(situacao);
        return this.save(vo);
    }
    
    private SituacaoRemessaVO buscarSituacaoRemessa(Long id) {
        Criteria criteria = getSession().createCriteria(SituacaoRemessaVO.class);
        criteria.add(Restrictions.eq("id", id));
        return (SituacaoRemessaVO) criteria.uniqueResult();
    }
    
    @SuppressWarnings("unchecked")
    public List<TramiteRequisicaoSuporteDTO> getGroupByTipoSuporte(RelatorioSuporteDTO relatorio) throws AppException {

        Criteria criteria = getSession().createCriteria(TramiteRemessaVO.class, "tramite").createAlias("requisicaoDocumento", "reqdoc", JoinType.INNER_JOIN)
                .createAlias("suporte", "suporte", JoinType.INNER_JOIN).createAlias("reqdoc.requisicao", "r", JoinType.INNER_JOIN)
                .createAlias("r.empresaContrato", "empCont", JoinType.INNER_JOIN).createAlias("empCont.base", "base", JoinType.INNER_JOIN);
        ProjectionList proj = Projections.projectionList();
        proj.add(Projections.sqlProjection("sum(case when this_.nu_suporte_a09=" + TipoSuporteEnum.PAPEL.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadePapel", new String[] { "quantidadePapel" }, new Type[] { new IntegerType() }));
        proj.add(Projections.sqlProjection("sum(case when this_.nu_suporte_a09 =" + TipoSuporteEnum.MICROFICHA.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadeMicroficha", new String[] { "quantidadeMicroficha" },
                new Type[] { new IntegerType() }));
        proj.add(Projections.sqlProjection("sum(case when this_.nu_suporte_a09 =" + TipoSuporteEnum.MICROFILME.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadeMicrofilme", new String[] { "quantidadeMicrofilme" },
                new Type[] { new IntegerType() }));
        proj.add(Projections.sqlProjection("sum(case when this_.nu_suporte_a09 =" + TipoSuporteEnum.MIDIAOPTICA.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadeMidiaOptica", new String[] { "quantidadeMidiaOptica" },
                new Type[] { new IntegerType() }));
        proj.add(Projections.sqlProjection("sum(case when this_.nu_suporte_a09 =" + TipoSuporteEnum.REPOSITORIO.getValor()
                + " then qt_disponibilizada_documento else 0 end ) as quantidadeRepositorio", new String[] { "quantidadeRepositorio" },
                new Type[] { new IntegerType() }));

        // proj.add(Projections.groupProperty("suporte"), "suporte");
        proj.add(Projections.groupProperty("empCont.base"), "base");
        proj.add(Projections.groupProperty("dataHoraRegistro"), "dataHoraRegistro");
        proj.add(Projections.groupProperty("base.nome"));
        // proj.add(Projections.rowCount(), "quantidade");

        criteria.setProjection(proj);

        if (relatorio.getBase() != null)
            criteria.add(Restrictions.eq("empCont.base", relatorio.getBase()));
        if (relatorio.getDataInicio() != null && relatorio.getDataFim() != null)
            criteria.add(Restrictions.between("dataHoraRegistro", relatorio.getDataInicio(), relatorio.getDataFim()));

        criteria.addOrder(Order.asc("base.nome"));
        criteria.addOrder(Order.asc("dataHoraRegistro"));

        criteria.setResultTransformer(Transformers.aliasToBean(TramiteRequisicaoSuporteDTO.class));

        return (List<TramiteRequisicaoSuporteDTO>) criteria.list();

    }

    /**
     * Consulta os trâmites de determina remessa.
     * 
     * @param tramite
     * @return
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<TramiteRemessaVO> findByRemessa(RemessaVO remessa) throws AppException {

        StringBuilder hql = new StringBuilder();
        hql.append("SELECT tramite FROM TramiteRemessaVO tramite ");
        hql.append("JOIN FETCH tramite.situacao ");
        hql.append("WHERE tramite.remessa.id = " + remessa.getId());
        hql.append(" ORDER BY tramite.dataTramiteRemessa");

        Query query = getEntityManager().createQuery(hql.toString(), TramiteRemessaVO.class);
        // necessário retornar um List ao invés de um Set para manter a ordem dos trâmites.
        return query.getResultList();
    }

    @Override
    protected void validaCampos(TramiteRemessaVO entity) {

    }

    @Override
    protected void validaRegras(TramiteRemessaVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(TramiteRemessaVO entity) throws AppException {

    }

}
