package br.gov.caixa.gitecsa.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSuporteEnum;
import br.gov.caixa.gitecsa.sired.extra.dto.RelatorioSuporteDTO;
import br.gov.caixa.gitecsa.sired.extra.dto.TramiteRequisicaoSuporteDTO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Deprecated
// @Stateless
public class TramiteRequisicaoService extends AbstractService<TramiteRequisicaoVO> {

    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(TramiteRequisicaoVO entity) {

    }

    @Override
    protected void validaRegras(TramiteRequisicaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(TramiteRequisicaoVO entity) throws AppException {

    }

    @SuppressWarnings("unchecked")
    public void excluirByRequisicao(RequisicaoVO requisicaoVO) throws AppException {
        Criteria criteria = getSession().createCriteria(TramiteRequisicaoVO.class, "tramite");
        criteria.add(Restrictions.eq("tramite.requisicao.id", requisicaoVO.getId()));
        List<TramiteRequisicaoVO> list = (List<TramiteRequisicaoVO>) criteria.list();
        for (TramiteRequisicaoVO tramiteRequisicaoVO : list) {
            super.delete(tramiteRequisicaoVO);
        }
        requisicaoVO.setTramiteRequisicoes(null);

    }

    public TramiteRequisicaoVO salvarTramiteRequisicaoAberta(RequisicaoVO requisicaoVO) throws AppException {
        return salvarTramiteRequisicao(requisicaoVO, this.buscarSituacaoRequisicao(SituacaoRequisicaoEnum.ABERTA.getId()));
    }

    public TramiteRequisicaoVO salvarTramiteRequisicaoFechada(RequisicaoVO requisicaoVO) throws AppException {
        return salvarTramiteRequisicao(requisicaoVO, this.buscarSituacaoRequisicao(SituacaoRequisicaoEnum.FECHADA.getId()));
    }

    public TramiteRequisicaoVO salvarTramiteRequisicaoReaberta(RequisicaoVO requisicaoVO) throws AppException {
        return salvarTramiteRequisicao(requisicaoVO, this.buscarSituacaoRequisicao(SituacaoRequisicaoEnum.REABERTA.getId()));
    }

    private TramiteRequisicaoVO salvarTramiteRequisicao(RequisicaoVO requisicaoVO, SituacaoRequisicaoVO situacao) throws AppException {
        TramiteRequisicaoVO vo = new TramiteRequisicaoVO();
        vo.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
        vo.setDataHora(new Date());
        vo.setRequisicao(requisicaoVO);
        vo.setSituacaoRequisicao(situacao);
        return this.save(vo);
    }

    private SituacaoRequisicaoVO buscarSituacaoRequisicao(Long id) {
        Criteria criteria = getSession().createCriteria(SituacaoRequisicaoVO.class);
        criteria.add(Restrictions.eq("id", id));
        return (SituacaoRequisicaoVO) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<TramiteRequisicaoSuporteDTO> getGroupByTipoSuporte(RelatorioSuporteDTO relatorio) throws AppException {

        Criteria criteria = getSession().createCriteria(TramiteRequisicaoVO.class, "tramite").createAlias("requisicaoDocumento", "reqdoc", JoinType.INNER_JOIN)
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

    public ArrayList<TramiteRequisicaoSuporteDTO> mounRetornoConsultaByTipoSuporte(List<TramiteRequisicaoSuporteDTO> lista) {
        Map<String, TramiteRequisicaoSuporteDTO> map = new HashMap<String, TramiteRequisicaoSuporteDTO>();

        for (TramiteRequisicaoSuporteDTO tramite : lista) {
            String key = tramite.getBase().getId() + " - " + tramite.getDataHoraRegistro();
            TramiteRequisicaoSuporteDTO tramiteMap = null;

            if (map.containsKey(key)) {
                tramiteMap = map.get(key);
            } else {
                tramiteMap = new TramiteRequisicaoSuporteDTO();
            }
            DecimalFormat df = new DecimalFormat("###");
            String formatado = df.format(tramite.getQuantidade());
            Integer quantidade = Integer.parseInt(formatado);

            if (tramite.getSuporte().getId().toString().equalsIgnoreCase(TipoSuporteEnum.PAPEL.getValor().toString())) {
                Integer qtdAtual = (tramiteMap.getQuantidadePapel() == null ? 0 : tramiteMap.getQuantidadePapel());
                tramiteMap.setQuantidadePapel(qtdAtual + quantidade);
            }

            else if (tramite.getSuporte().getId().toString().equalsIgnoreCase(TipoSuporteEnum.MICROFICHA.getValor().toString())) {
                Integer qtdAtual = (tramiteMap.getQuantidadeMicroficha() == null ? 0 : tramiteMap.getQuantidadeMicroficha());
                tramiteMap.setQuantidadeMicroficha(qtdAtual + quantidade);
            }

            else if (tramite.getSuporte().getId().toString().equalsIgnoreCase(TipoSuporteEnum.MICROFILME.getValor().toString())) {
                Integer qtdAtual = (tramiteMap.getQuantidadeMicrofilme() == null ? 0 : tramiteMap.getQuantidadeMicrofilme());
                tramiteMap.setQuantidadeMicrofilme(qtdAtual + quantidade);
            }

            else if (tramite.getSuporte().getId().toString().equalsIgnoreCase(TipoSuporteEnum.MIDIAOPTICA.getValor().toString())) {
                Integer qtdAtual = (tramiteMap.getQuantidadeMidiaOptica() == null ? 0 : tramiteMap.getQuantidadeMidiaOptica());
                tramiteMap.setQuantidadeMidiaOptica(qtdAtual + quantidade);
            }
            if (!map.containsKey(key)) {
                map.put(key, tramiteMap);
            }
        }

        return (new ArrayList<TramiteRequisicaoSuporteDTO>(map.values()));

    }

}
