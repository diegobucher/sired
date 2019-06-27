package br.gov.caixa.gitecsa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;

@Stateless
public class RemessaDocumentoService extends AbstractService<RemessaDocumentoVO> {

    private static final long serialVersionUID = 1L;

    private List<GrupoVO> grupos;

    @Inject
    private GrupoService grupoService;

    /*
     * @Inject
     * 
     * @DataRepository private EntityManager entityManagerSistema;
     */

    @Override
    protected void validaCampos(RemessaDocumentoVO entity) {

    }
    
    /**
     * Verifica existem documento do tipo Movimento Diario, na mesma "Data de Movimento", Mesma Unidade Geradora, e que não sejam rascunho.
     */
    public RemessaDocumentoVO obterRemessaDocumentoComplemento(RemessaDocumentoVO remessaDocumentoVO){
      
      StringBuilder hql = new StringBuilder();
      hql.append(" SELECT item FROM RemessaDocumentoVO item ");
      hql.append(" JOIN FETCH item.remessa remessa ");
      hql.append(" JOIN remessa.documento documento ");
      hql.append(" JOIN remessa.tramiteRemessaAtual tramiteAtual ");
      hql.append(" WHERE item.dataGeracao = :dataGeracao ");
      hql.append(" AND item.unidadeGeradora.id = :unidadeGeradoraId ");
      hql.append(" AND documento.id = :documentoId ");
      hql.append(" AND tramiteAtual.situacao.id != :situacaoRemessaId ");
      if(remessaDocumentoVO.getId() != null){
        hql.append(" AND item.id != :itemId ");
      }
      hql.append(" ORDER BY item.id DESC ");
      
      TypedQuery<RemessaDocumentoVO> query = getEntityManager().createQuery(hql.toString(), RemessaDocumentoVO.class);
      query.setParameter("dataGeracao", remessaDocumentoVO.getDataGeracao());
      query.setParameter("unidadeGeradoraId", remessaDocumentoVO.getUnidadeGeradora().getId());
      query.setParameter("documentoId", remessaDocumentoVO.getRemessa().getDocumento().getId());
      query.setParameter("situacaoRemessaId", SituacaoRemessaEnum.RASCUNHO.getId());
      if(remessaDocumentoVO.getId() != null){
        query.setParameter("itemId", remessaDocumentoVO.getId());
      }
      
      query.setMaxResults(1);
      
      try {
        return query.getSingleResult();
      } catch (Exception e) {
        return null;
      }
      
    }

    /**
     * Consulta os itens de uma remessa.
     * 
     * @param tramite
     * @return
     * @throws AppException
     */
    @SuppressWarnings("unchecked")
    public List<RemessaDocumentoVO> findByRemessa(RemessaVO remessa) throws AppException {

        StringBuilder hql = new StringBuilder();
        hql.append("SELECT item FROM RemessaDocumentoVO item ");
        hql.append("JOIN FETCH item.remessa rem ");
        hql.append("JOIN FETCH item.unidadeGeradora un ");
        hql.append("JOIN FETCH un.uf ");
        hql.append("WHERE rem.id = " + remessa.getId());
//        hql.append(" ORDER BY item.nuItem");

        Query query = getEntityManager().createQuery(hql.toString(), RemessaDocumentoVO.class);

        return query.getResultList();
    }

    public void validaDados(RemessaDocumentoVO entity) throws BusinessException {
        mensagens = new ArrayList<String>();
        validaRegras(entity);
        if (!Util.isNullOuVazio(mensagens)) {
            throw new BusinessException(mensagens);
        }
    }

    @Override
    protected void validaRegras(RemessaDocumentoVO entity) {
        try {
            grupos = grupoService.findGrupoAssociadoDocumento(entity.getRemessa().getDocumento());
        } catch (AppException e1) {
            mensagens.add(e1.getMessage());
        }

    }

    @Override
    public RemessaDocumentoVO getById(Object id) {
        RemessaDocumentoVO vo = super.getById(id);
        vo.getUnidadeGeradora();
        return vo;
    }

    @SuppressWarnings("unchecked")
    public List<RemessaDocumentoVO> buscaPorRemessa(RemessaVO vo) {
        Criteria criteria = getSession().createCriteria(RemessaDocumentoVO.class);
        // criteria.createAlias("tipoDemanda", "tipoDemanda",
        // JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("remessa.id", vo.getId()));

        return (List<RemessaDocumentoVO>) criteria.list();
    }

    protected RemessaDocumentoVO saveImpl(RemessaDocumentoVO remessaDocumento) throws AppException {
        try {
            entityManager.persist(remessaDocumento);
        } catch (Exception e) {
            mensagens.add(e.getMessage());
            if (!Util.isNullOuVazio(mensagens))
                throw new BusinessException(e.getMessage());
        }

        return remessaDocumento;
    }

    public static byte[] getFileBytes(File file) throws Exception {
        int len = (int) file.length();
        byte[] sendBuf = new byte[len];
        FileInputStream inFile = null;
        try {
            inFile = new FileInputStream(file);
            inFile.read(sendBuf, 0, len);
            inFile.close();

        } catch (FileNotFoundException fnfex) {
            throw fnfex;
        } catch (IOException ioex) {
            throw ioex;
        }
        return sendBuf;
    }

    @Override
//    @SuppressWarnings("unchecked")
    public void delete(RemessaDocumentoVO entity) throws AppException {
//        Integer numeroItem = entity.getNuItem();
//
//        super.delete(entity);
//
//        Criteria criteria = getSession().createCriteria(RemessaDocumentoVO.class, "this");
//        criteria.add(Restrictions.eq("remessa.id", entity.getRemessa().getId()));
//        criteria.add(Restrictions.gt("nuItem", numeroItem));
//        criteria.addOrder(Order.asc("nuItem"));
//        List<RemessaDocumentoVO> listRemessaDocumento = (List<RemessaDocumentoVO>) criteria.list();
//
//        for (RemessaDocumentoVO item : listRemessaDocumento) {
//            item.setNuItem(numeroItem++);
//            super.update(item);
//        }
    }

    @Override
    protected void validaRegrasExcluir(RemessaDocumentoVO entity) throws AppException {

    }

}
