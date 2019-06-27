package br.gov.caixa.gitecsa.sired.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.OperacaoDAO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.OperacaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Stateless(name = "operacaoContaService")
public class OperacaoService extends AbstractService<OperacaoVO> {

    private static final long serialVersionUID = -3788038285088139936L;

    @Inject
    private OperacaoDAO dao;

    @Override
    protected void validaCamposObrigatorios(OperacaoVO entity) {

    }

    @Override
    protected void validaRegras(OperacaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(OperacaoVO entity) {

    }

    @Override
    protected GenericDAO<OperacaoVO> getDAO() {
        return this.dao;
    }
    
    public List<OperacaoVO> findAllByAreaMeioEDocumento(UnidadeVO unidade, DocumentoVO documento) {
        return this.dao.findAllByAreaMeioEDocumento(unidade, documento);
    }
    
    public OperacaoVO findById(final String id) throws DataBaseException, PersistenceException, Exception {
    	return this.dao.findById(id);
    }
}
