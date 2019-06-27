package br.gov.caixa.gitecsa.service;

import java.util.List;

import javax.ejb.Stateless;

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
import br.gov.caixa.gitecsa.sired.enumerator.TipoSuporteEnum;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

@Stateless
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
}
