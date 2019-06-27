package br.gov.caixa.gitecsa.service;

import java.util.List;

import javax.ejb.Stateless;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;

@Stateless
public class DocumentoService extends AbstractService<DocumentoVO> {

    private static final long serialVersionUID = 1L;

    @Override
    protected void validaCampos(DocumentoVO entity) {
    }

    @Override
    protected void validaRegras(DocumentoVO entity) {
        List<DocumentoVO> listaDocumento = findAll();
        for (DocumentoVO documentoVO : listaDocumento) {
            if (entity.getId() == null) {
                if (documentoVO.getNome().equalsIgnoreCase(entity.getNome().trim())) {
                    mensagens.add(MensagemUtils.obterMensagem("geral.crud.duplicidade"));
                    break;
                }
            } else {
                if ((!documentoVO.getId().equals(entity.getId())) && documentoVO.getNome().trim().equalsIgnoreCase(entity.getNome().trim())) {
                    mensagens.add(MensagemUtils.obterMensagem("geral.crud.duplicidade"));
                    break;
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
            mensagens.add(MensagemUtils.obterMensagem("geral.message.erro.relacionamentoExistente"));
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

}
