package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.CTDAVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.LoteSequenciaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless
public class BaseService extends AbstractService<BaseVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    LoteSequenciaService loteSequenciaService;

    @Inject
    EmpresaContratoService empresaContratoService;

    @Inject
    CTDAService ctdaService;

    @Inject
    TramiteRequisicaoDocumentoService tramiteRequisicaoDocumentoService;

    public EmpresaContratoService getEmpresaContratoService() {
        return empresaContratoService;
    }

    public void setEmpresaContratoService(EmpresaContratoService empresaContratoService) {
        this.empresaContratoService = empresaContratoService;
    }

    public LoteSequenciaService getLoteSequenciaService() {
        return loteSequenciaService;
    }

    public void setLoteSequenciaService(LoteSequenciaService loteSequenciaService) {
        this.loteSequenciaService = loteSequenciaService;
    }

    @Override
    protected void validaCampos(BaseVO entity) {
    }

    @Override
    protected void validaRegras(BaseVO entity) {
        duplicidadeBase(entity);
        garanteUnicidadeLoteByBase(entity);

    }

    // se existe outra base com o mesmo nome e/ou se a unidade informada já possui base relacionada.
    private void duplicidadeBase(BaseVO entity) {
        List<BaseVO> lista = findAll();
        for (BaseVO base : lista) {
            if (entity.getId() == null) {
                if (base.getNome().equalsIgnoreCase(entity.getNome().trim()) || base.getUnidade().getId().equals(entity.getUnidade().getId())) {
                    mensagens.add(MensagemUtils.obterMensagem("MA004", entity.getNome(), entity.getUnidade().getId()));
                    break;
                }
            } else {
                if ((!base.getId().equals(entity.getId()))
                        && (base.getNome().trim().equalsIgnoreCase(entity.getNome().trim()) || base.getUnidade().getId().equals(entity.getUnidade().getId()))) {
                    mensagens.add(MensagemUtils.obterMensagem("MA004", entity.getNome(), entity.getUnidade().getDescricaoCompleta()));
                    break;
                }
            }
        }
    }

    // se o(s) número(s) de Lote/Seqüência já está(ão) associado(s) a outra base
    private void garanteUnicidadeLoteByBase(BaseVO entity) {
        List<LoteSequenciaVO> loteSequenciaAssociado = loteSequenciaService.findAssociadoBase();
        for (LoteSequenciaVO loteEntity : entity.getLoteSequencia()) {
            for (LoteSequenciaVO loteAssociadoSomeGroup : loteSequenciaAssociado) {
                if (loteAssociadoSomeGroup.getId().equals(loteEntity.getId()))
                    if (entity.getId() == null) {
                        mensagens.add(MensagemUtils.obterMensagem("MI024", loteAssociadoSomeGroup.getBases().get(0).getNome()));
                        break;
                    }

                    else {
                        BaseVO associado = null;
                        for (BaseVO base : loteAssociadoSomeGroup.getBases()) {
                            if (!base.getId().equals(entity.getId())) {
                                associado = base;
                            }
                        }

                        if (associado != null) {
                            mensagens.add(MensagemUtils.obterMensagem("MI024", associado.getNome()));
                            break;
                        }

                    }
            }
        }
    }

    @Override
    protected void validaRegrasExcluir(BaseVO entity) throws AppException {

        relacionamentoEmpresaContrato(entity);
        relacionamentoCTDA(entity);
    }

    private void relacionamentoEmpresaContrato(BaseVO entity) throws AppException {
        EmpresaContratoVO empCon = new EmpresaContratoVO();
        empCon.setBase(entity);

        List<EmpresaContratoVO> lista = empresaContratoService.findByParameters(empCon);

        if (lista != null && lista.size() > 0) {
            mensagens.add(MensagemUtils.obterMensagem("MA030"));
        }
    }

    private void relacionamentoCTDA(BaseVO entity) throws AppException {
        CTDAVO ctda = new CTDAVO();
        ctda.setBase(entity);

        List<CTDAVO> lista = ctdaService.findByParameters(ctda);

        if (lista != null && lista.size() > 0) {
            mensagens.add(MensagemUtils.obterMensagem("MA030"));
        }
    }

    @Override
    public BaseVO getById(final Object id) {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT b FROM BaseVO b ");
        hql.append(" JOIN FETCH b.loteSequencia ls ");
        hql.append(" JOIN FETCH b.unidade u ");
        hql.append(" WHERE b.id = :id ");

        TypedQuery<BaseVO> query = getEntityManager().createQuery(hql.toString(), BaseVO.class);

        query.setParameter("id", id);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BaseVO> findAll() {
        Criteria criteria = getSession().createCriteria(BaseVO.class).createAlias("loteSequencia", "loteSequencia", JoinType.LEFT_OUTER_JOIN)
                .createAlias("unidade", "unidade", JoinType.LEFT_OUTER_JOIN);

        criteria.addOrder(Order.asc("nome"));

        return mountBase(((List<BaseVO>) criteria.list()));
    }

    private List<BaseVO> mountBase(List<BaseVO> list) {
        Map<Long, BaseVO> map = new HashMap<Long, BaseVO>();
        List<BaseVO> listaNova = new ArrayList<BaseVO>();
        for (BaseVO baseVO : list) {
            if (!map.containsKey(baseVO.getId())) {
                map.put((Long) baseVO.getId(), baseVO);
                listaNova.add(baseVO);
            }
        }
        return listaNova;
    }

    public List<LoteSequenciaVO> loteSequenciaVinculado(String base, UnidadeVO unidadeVO) throws AppException {
        List<BaseVO> bases = tramiteRequisicaoDocumentoService.consultaBasePorIdUnidade(Long.parseLong(unidadeVO.getId().toString()));
        BaseVO baseVO = bases.get(0);

        Criteria criteria = getSession().createCriteria(LoteSequenciaVO.class);
        criteria.createAlias("bases", "bas", JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("bas.id", baseVO.getId()));
        criteria.add(Restrictions.eq("id", base));

        return (List<LoteSequenciaVO>) criteria.list();
    }

    public BaseVO getBaseByUnidade(UnidadeVO unidadeVO) {
        Criteria criteria = getSession().createCriteria(BaseVO.class).createAlias("loteSequencia", "loteSequencia", JoinType.LEFT_OUTER_JOIN);
        criteria.createAlias("unidade", "unidade", JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("unidade.id", unidadeVO.getId()));
        List<BaseVO> list = mountBase((List<BaseVO>) criteria.list());

        if (list.size() > 0)
            return list.get(0);
        return null;
    }
}
