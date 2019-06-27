package br.gov.caixa.gitecsa.sired.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAOImpl;
import br.gov.caixa.gitecsa.sired.dao.TramiteDocumentoOriginalDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoDocumentoOriginalEnum;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteDocumentoOriginalVO;

public class TramiteDocumentoOriginalDAOImpl extends GenericDAOImpl<TramiteDocumentoOriginalVO> implements TramiteDocumentoOriginalDAO {

    @Override
    public List<TramiteDocumentoOriginalVO> findByDocumentoOriginal(DocumentoOriginalVO docOriginal) {
        StringBuilder hql = new StringBuilder();

        hql.append(" Select t FROM TramiteDocumentoOriginalVO t ");
        hql.append(" Join Fetch t.situacaoDocOriginal ");
        hql.append(" Where t.documentoOriginal = :doc ");
        hql.append(" Order By t.dataHora ");

        TypedQuery<TramiteDocumentoOriginalVO> query = getEntityManager().createQuery(hql.toString(), TramiteDocumentoOriginalVO.class);
        query.setParameter("doc", docOriginal);

        return query.getResultList();
    }
    
    @Override
    public Date getDataEnvio(DocumentoOriginalVO docOriginal) {
        StringBuilder hql = new StringBuilder();

        hql.append(" Select t.dataHora FROM TramiteDocumentoOriginalVO t ");
        hql.append(" Where t.documentoOriginal = :doc And t.situacaoDocOriginal.id = :situacao ");

        Query query = getEntityManager().createQuery(hql.toString());
        query.setParameter("doc", docOriginal);
        query.setParameter("situacao", SituacaoDocumentoOriginalEnum.ENVIADO.getId());

        try {
            return ((Date) query.getSingleResult());
        } catch (Exception e) {
            return null;
        }
    }
}
