package br.gov.caixa.gitecsa.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.BaseAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;

@Stateless
public class DocumentoService extends AbstractService<DocumentoVO> {

    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(DocumentoVO entity) {
    }

    /**
     * Indica se o campo de mensagem é obrigatorio ou nao
     * 
     * @param entity
     * @return
     */
    public boolean isCampoMensagemObrigatoria(DocumentoVO entity) {
        if (entity.getIcAtivo() != null && entity.getIcAtivo().equals(AtivoInativoEnum.INATIVO)) {
            return true;
        }
        return false;
    }

    /**
     * Retorna a mensagem de validação para o campo de mensagem Obrigatorio
     * 
     * @param label
     * @return
     */
    public String getMensagemValidacaoMensagemObrigatoria() {
        return MensagemUtils.obterMensagem("MA017");
    }

    private boolean documentosSemGrupo(DocumentoVO documentoVO1, DocumentoVO documentoVO2) {
        return (ObjectUtils.isNullOrEmpty(documentoVO1.getGrupo()) && ObjectUtils.isNullOrEmpty(documentoVO2.getGrupo()));
    }

    @Override
    protected void validaRegras(DocumentoVO entity) {
        List<DocumentoVO> listaDocumento = findByNome(entity.getNome());
        for (DocumentoVO documentoVO : listaDocumento) {
            // Documentos são considerados iguais caso tenham o mesmo nome e seu grupo tenha o mesmo tipo de solicitação.
            if (entity.getId() == null) {
                if (documentosSemGrupo(documentoVO, entity)) { // os dois documentos não possuem grupos, logo não permite salvar.
                    mensagens.add(MensagemUtils.obterMensagem("MI018"));
                    break;
                }
            } else {
                entity = findByIdEager(entity.getId());
                if ((!documentoVO.getId().equals(entity.getId()))) { // não é o mesmo documento.
                    if (documentosSemGrupo(documentoVO, entity)) { // os dois documentos não possuem grupos, logo não permite salvar.
                        mensagens.add(MensagemUtils.obterMensagem("MI018"));
                        break;
                    } else if (ObjectUtils.isNullOrEmpty(documentoVO.getGrupo()) || ObjectUtils.isNullOrEmpty(entity.getGrupo())) { // um documento possui grupo
                                                                                                                                    // e o outro não, logo
                                                                                                                                    // ignora.
                        continue;
                    } else if (documentoVO.getGrupo().getTipoSolicitacao().equals(entity.getGrupo().getTipoSolicitacao())) { // os dois possuem grupo com tipos
                                                                                                                             // de solicitação iguais, logo, não
                                                                                                                             // permite salvar.
                        mensagens.add(MensagemUtils.obterMensagem("MI018"));
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void validaRegrasExcluir(DocumentoVO entity) {
        // SÓ É PERMITIDO EXLUIR UM DOCUMENTO QUE NÃO ESTEJA ASSOCIADO A NENHUM
        // GRUPO
        DocumentoVO documento = findAssociadoGrupoById(entity);

        if (documento != null) {
            mensagens.add(MensagemUtils.obterMensagem("MA006"));
        }

    }

    @SuppressWarnings("unchecked")
    public List<DocumentoVO> findByGrupo(DocumentoVO documento) {
        Criteria criteria = getSession().createCriteria(DocumentoVO.class).createAlias("grupo", "grupo", JoinType.INNER_JOIN);

        criteria.add(Restrictions.eq("grupo.id", documento.getGrupo().getId()));

        criteria.addOrder(Order.desc("nome"));

        return ((List<DocumentoVO>) criteria.list());

    }

    @SuppressWarnings("unchecked")
    public List<DocumentoVO> findAssociadoGrupo() {
        Criteria criteria = getSession().createCriteria(DocumentoVO.class).createAlias("grupo", "grupo", JoinType.INNER_JOIN);

        criteria.addOrder(Order.desc("nome"));

        return ((List<DocumentoVO>) criteria.list());
    }

    public DocumentoVO findAssociadoGrupoById(DocumentoVO entity) {
        Criteria criteria = getSession().createCriteria(DocumentoVO.class).createAlias("grupo", "grupo", JoinType.INNER_JOIN);

        criteria.add(Restrictions.eq("id", entity.getId()));

        criteria.addOrder(Order.desc("nome"));

        return (DocumentoVO) criteria.uniqueResult();

    }

    @SuppressWarnings("unchecked")
    public List<DocumentoVO> buscarRemessaMovimentoDiario() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT d ");
        hql.append(" FROM DocumentoVO d ");
        hql.append(" Join Fetch d.grupo g ");
        hql.append(" Join Fetch g.grupoCampos gc ");
        hql.append(" Join Fetch gc.campo ");
        hql.append(" Where g.tipoSolicitacao = :tipoSolicitacao ");
        hql.append(" and d.nome like :nome ");

        Query query = getEntityManager().createQuery(hql.toString(), DocumentoVO.class);
        query.setParameter("tipoSolicitacao", TipoSolicitacaoEnum.REMESSA);
        query.setParameter("nome", Constantes.DOCUMENTO_REMESSA_MOVIMENTO_DIARIO);

        return query.getResultList();
    }

    public List<DocumentoVO> findAllEager() {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT d ");
        hql.append(" FROM DocumentoVO d ");
        hql.append(" Left Join Fetch d.grupo g ");

        TypedQuery<DocumentoVO> query = getEntityManager().createQuery(hql.toString(), DocumentoVO.class);
        return query.getResultList();
    }

    private List<DocumentoVO> findByNome(String nome) {
        StringBuilder hql = new StringBuilder("");
        hql.append(" SELECT d ");
        hql.append(" FROM DocumentoVO d ");
        hql.append(" Where d.nome = :nome ");

        TypedQuery<DocumentoVO> query = getEntityManager().createQuery(hql.toString(), DocumentoVO.class);
        query.setParameter("nome", nome);

        return query.getResultList();
    }

    public DocumentoVO findByIdEager(Long id) {
        StringBuilder hql = new StringBuilder("");
        hql.append(" SELECT d ");
        hql.append(" FROM DocumentoVO d ");
        hql.append(" Left Join Fetch d.grupo g ");
        // hql.append(" Left Join Fetch g.tipoSolicitacao tp ");
        hql.append(" Where d.id = :id ");

        Query query = getEntityManager().createQuery(hql.toString(), DocumentoVO.class);
        query.setParameter("id", id);

        try {
            return (DocumentoVO) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public DocumentoVO getById(final Object id) {
        return this.findByIdEager((Long)id);
    }
    
    public BaseAtendimentoVO getBaseAtendimentoByDocumento(final DocumentoVO documento) {
        StringBuilder hql = new StringBuilder("");
        hql.append(" Select b ");
        hql.append(" From BaseAtendimentoVO b ");
        hql.append(" Join Fetch b.base ");
        hql.append(" Join Fetch b.documento ");
        hql.append(" Where b.id = :id ");

        TypedQuery<BaseAtendimentoVO> query = getEntityManager().createQuery(hql.toString(), BaseAtendimentoVO.class);
        query.setParameter("id", documento.getId());

        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
}
