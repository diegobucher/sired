package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.OperacaoVO;

@Stateless
public class OperacaoService extends AbstractService<OperacaoVO> implements Serializable {
    private static final long serialVersionUID = 3016183975736128968L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;

    @Override
    protected void validaCampos(OperacaoVO entity) {
    }

    @Override
    protected void validaRegras(OperacaoVO entity) {
    }

    @Override
    protected void validaRegrasExcluir(OperacaoVO entity) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<OperacaoVO> findAll() {
        Criteria criteria = getSession().createCriteria(OperacaoVO.class);

        criteria.add(Restrictions.eq("icAtivo", SimNaoEnum.SIM));

        criteria.addOrder(Order.asc("id"));

        return ((List<OperacaoVO>) criteria.list());

    }

    /**
     * Consulta as operações que a Área-Meio possui permissão para solicitar documentos. RN014.
     * 
     * @param idUnidade
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<OperacaoVO> consultarOperacoesPermitidasPorAreaMeio(Long idUnidade, DocumentoVO doc) throws Exception {

        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT DISTINCT operacao FROM DocumentoVO documento ");
        hql.append(" JOIN documento.grupo gp ");
        hql.append(" JOIN gp.unidadeGrupos unidadeGrupo ");
        hql.append(" JOIN unidadeGrupo.unidade unidade ");
        hql.append(" JOIN unidadeGrupo.operacao operacao ");
        hql.append(" WHERE unidade.id = " + idUnidade);
        hql.append(" AND documento.id = " + doc.getId());

        Query query = entityManagerSistema.createQuery(hql.toString(), OperacaoVO.class);

        List<OperacaoVO> operacoes = query.getResultList();

        return operacoes;
    }

}
