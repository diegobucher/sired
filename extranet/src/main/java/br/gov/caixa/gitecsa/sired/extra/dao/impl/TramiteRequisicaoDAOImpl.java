package br.gov.caixa.gitecsa.sired.extra.dao.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSuporteEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.TramiteRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.extra.dto.RelatorioSuporteDTO;
import br.gov.caixa.gitecsa.sired.extra.dto.TramiteRequisicaoSuporteDTO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

public class TramiteRequisicaoDAOImpl extends GenericDAOImpl<TramiteRequisicaoVO> implements TramiteRequisicaoDAO {

    @SuppressWarnings("unchecked")
    public void excluirByRequisicao(RequisicaoVO requisicaoVO) throws AppException {
        Criteria criteria = getSession().createCriteria(TramiteRequisicaoVO.class, "tramite");

        criteria.add(Restrictions.eq("tramite.requisicao.id", requisicaoVO.getId()));

        List<TramiteRequisicaoVO> list = (List<TramiteRequisicaoVO>) criteria.list();

        for (TramiteRequisicaoVO tramiteRequisicaoVO : list) {
            delete(tramiteRequisicaoVO);
        }

        requisicaoVO.setTramiteRequisicoes(null);
    }

    @SuppressWarnings("unchecked")
    public List<TramiteRequisicaoVO> obterTramitesPorRequisicao(Long idRequisicao) {
        StringBuilder sb = new StringBuilder();
        sb.append("select tramite FROM TramiteRequisicaoVO tramite ");
        sb.append("INNER JOIN FETCH tramite.requisicao as req ");
        sb.append("INNER JOIN FETCH tramite.situacaoRequisicao as sit ");

        sb.append("where req.id = :idRequisicao ");
        
        sb.append("order by tramite.dataHora ");

        Query query = getSession().createQuery(sb.toString());

        query.setParameter("idRequisicao", idRequisicao);

        return (List<TramiteRequisicaoVO>) query.list();
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

        proj.add(Projections.groupProperty("empCont.base"), "base");
        proj.add(Projections.groupProperty("dataHoraRegistro"), "dataHoraRegistro");
        proj.add(Projections.groupProperty("base.nome"));

        criteria.setProjection(proj);

        if (relatorio.getBase() != null) {
            criteria.add(Restrictions.eq("empCont.base", relatorio.getBase()));
        }

        if (relatorio.getDataInicio() != null && relatorio.getDataFim() != null) {
            criteria.add(Restrictions.between("dataHoraRegistro", relatorio.getDataInicio(), relatorio.getDataFim()));
        }

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

            if (tramite.getSuporte().getId().toString().equals(TipoSuporteEnum.PAPEL.getValor())) {
                Integer qtdAtual = (tramiteMap.getQuantidadePapel() == null ? 0 : tramiteMap.getQuantidadePapel());
                tramiteMap.setQuantidadePapel(qtdAtual + quantidade);
            } else if (tramite.getSuporte().getId().toString().equals(TipoSuporteEnum.MICROFICHA.getValor())) {
                Integer qtdAtual = (tramiteMap.getQuantidadeMicroficha() == null ? 0 : tramiteMap.getQuantidadeMicroficha());
                tramiteMap.setQuantidadeMicroficha(qtdAtual + quantidade);
            } else if (tramite.getSuporte().getId().toString().equals(TipoSuporteEnum.MICROFILME.getValor())) {
                Integer qtdAtual = (tramiteMap.getQuantidadeMicrofilme() == null ? 0 : tramiteMap.getQuantidadeMicrofilme());
                tramiteMap.setQuantidadeMicrofilme(qtdAtual + quantidade);
            } else if (tramite.getSuporte().getId().toString().equals(TipoSuporteEnum.MIDIAOPTICA.getValor())) {
                Integer qtdAtual = (tramiteMap.getQuantidadeMidiaOptica() == null ? 0 : tramiteMap.getQuantidadeMidiaOptica());
                tramiteMap.setQuantidadeMidiaOptica(qtdAtual + quantidade);
            }

            if (!map.containsKey(key)) {
                map.put(key, tramiteMap);
            }

        }

        return (new ArrayList<TramiteRequisicaoSuporteDTO>(map.values()));
    }

    public TramiteRequisicaoVO salvarTramiteRequisicao(RequisicaoVO requisicaoVO, SituacaoRequisicaoVO situacao) throws AppException {
        TramiteRequisicaoVO tramiteRequisicao = new TramiteRequisicaoVO();

        tramiteRequisicao.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
        tramiteRequisicao.setDataHora(new Date());
        tramiteRequisicao.setRequisicao(requisicaoVO);
        tramiteRequisicao.setSituacaoRequisicao(situacao);

        return save(tramiteRequisicao);
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
