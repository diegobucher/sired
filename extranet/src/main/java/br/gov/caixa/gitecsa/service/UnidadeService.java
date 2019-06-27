package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.UnidadeGrupoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Deprecated
// @Stateless
public class UnidadeService extends AbstractService<UnidadeVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @Override
    protected void validaCampos(UnidadeVO entity) {

    }

    @Override
    protected void validaRegras(UnidadeVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(UnidadeVO entity) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UnidadeVO> findAll() {
        try {

            StringBuilder hql = new StringBuilder();

            hql.append("select * from redsm001.redtba02_unidade u " + " left join  redsm001.redtbc06_unidade_grupo ug on ug.nu_unidade_a02   = u.nu_unidade  "
                    + " left join redsm001.redtbc07_unidade_uf uf on uf.nu_unidade_a02 = u.nu_unidade "
                    + " left join redsm001.redtbc17_unidade_grupo_operacao ugo on ugo.nu_unidade_grupo =  ug.nu_unidade_grupo "
                    + " left join redsm001.redtba11_operacao op on op.nu_operacao = ugo.nu_operacao_a11  "
                    + " where (ug.nu_unidade_a02 is not null or  uf.nu_unidade_a02 is not null)");

            Query query = entityManagerSistema.createNativeQuery(hql.toString(), UnidadeVO.class);

            return mount((List<UnidadeVO>) query.getResultList());

        } catch (Exception e) {
            if (mensagens == null || mensagens.isEmpty()) {
                mensagens = new ArrayList<String>();
            }
            mensagens.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "Unidade", "findAll"));
        }
        return null;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<UnidadeVO> mount(List<UnidadeVO> list) {
        Map<Long, UnidadeVO> map = new HashMap<Long, UnidadeVO>();
        for (UnidadeVO obj : list) {

            Collections.sort(new ArrayList(obj.getUfs()));
            Collections.sort(new ArrayList(obj.getUnidadeGrupos()));
            for (UnidadeGrupoVO unidadeVO : obj.getUnidadeGrupos()) {
                unidadeVO.getOperacao().toArray();
            }
            if (!map.containsKey(obj.getId())) {
                map.put((Long) obj.getId(), obj);
            }
        }

        return (new ArrayList<UnidadeVO>(map.values()));

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public UnidadeVO getById(Object id) {
        try {

            Criteria criteria = getSession().createCriteria(UnidadeVO.class).createAlias("unidadeGrupos", "grupos", JoinType.LEFT_OUTER_JOIN)
                    .createAlias("ufs", "ufs", JoinType.LEFT_OUTER_JOIN);

            criteria.add(Restrictions.eq("id", id));

            criteria.addOrder(Order.desc("nome"));
            criteria.addOrder(Order.desc("grupos.grupo"));
            criteria.addOrder(Order.desc("ufs.nome"));

            UnidadeVO obj = (UnidadeVO) criteria.uniqueResult();
            if (obj != null) {
                Collections.sort(new ArrayList(obj.getUfs()));
                Collections.sort(new ArrayList(obj.getUnidadeGrupos()));
            }

            return obj;

        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "Unidade", "getById"));
        }
        return null;

    }
}
