package br.gov.caixa.gitecsa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.repository.DataRepository;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.extra.dao.RemessaDocumentoDAO;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;

@Stateless
public class RemessaDocumentoService extends AbstractService<RemessaDocumentoVO> {

    private static final long serialVersionUID = 1L;

    @Inject
    @DataRepository
    private EntityManager entityManagerSistema;
    
    @Inject
    private RemessaDocumentoDAO remessaDocumentoDAO;

    @Override
    protected void validaCampos(RemessaDocumentoVO entity) {

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
        hql.append("WHERE item.remessa.id = " + remessa.getId());
        hql.append(" ORDER BY item.nuItem");

        Query query = entityManagerSistema.createQuery(hql.toString(), RemessaDocumentoVO.class);

        return query.getResultList();
    }

    public RemessaDocumentoVO findByIdEager(Long id) {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT item FROM RemessaDocumentoVO item ");
        hql.append(" Join Fetch item.remessa remessa ");
        hql.append(" Join Fetch item.unidadeGeradora unidadeGeradora ");
        hql.append(" Join Fetch unidadeGeradora.uf ");
        hql.append(" WHERE item.id = :id ");

        TypedQuery<RemessaDocumentoVO> query = entityManagerSistema.createQuery(hql.toString(), RemessaDocumentoVO.class);
        query.setParameter("id", id);

        return query.getSingleResult();
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
    protected void validaRegrasExcluir(RemessaDocumentoVO entity) throws AppException {

    }
    
    public RemessaDocumentoVO salvar(RemessaDocumentoVO remessaDocumento) {
      if (remessaDocumento.getId() != null) {      
        remessaDocumentoDAO.update(remessaDocumento);
      } else {
        remessaDocumentoDAO.save(remessaDocumento);
      }
      return remessaDocumento;
    }
    
    public Integer consultaRemessaDocumentoAtrelado(RemessaDocumentoVO remessaDocumento) {
      return remessaDocumentoDAO.consultaRemessaDocumentoAtrelado(remessaDocumento);
      
    }
    
    public void excluirremessaDocumento(RemessaDocumentoVO doc) {
      remessaDocumentoDAO.delete(doc);
    }

}
