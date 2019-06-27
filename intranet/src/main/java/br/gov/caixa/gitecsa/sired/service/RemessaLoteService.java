package br.gov.caixa.gitecsa.sired.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.apache.commons.csv.CSVRecord;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequisicaoLoteException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dao.RemessaDAO;
import br.gov.caixa.gitecsa.sired.dto.RemessaLoteDTO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class RemessaLoteService extends PaginatorService<RemessaVO> {

	private static final long serialVersionUID = -3296864569022811757L;

	private static final int TRANSACTION_TIMEOUT = 7200; // 2 horas
	
	@Resource
    private UserTransaction transaction;
	
	@Inject
    private TramiteRemessaService tramiteService;
	
	@Inject
	RemessaService remessaService;
	
	@Inject
    private RemessaDAO dao;
	
	public void salvar(List<RemessaLoteDTO> documentosRemessa, RemessaVO remessa) throws RequisicaoLoteException, RequiredException, Exception {
        
        Integer numeroRejeitadas = 0;
        List<String> msgValidacao = new ArrayList<String>();
        
        this.transaction.setTransactionTimeout(TRANSACTION_TIMEOUT);
        this.transaction.begin();
        
        for (RemessaLoteDTO remessaLote : documentosRemessa) {
            
            CSVRecord registro = remessaLote.getRegistro();
            
            try {
                // Cria a remessa
                if(remessa.getId() == null) {
                  this.save(remessa);
                  TramiteRemessaVO tramite = this.tramiteService.salvarTramiteRemessaRascunho(remessa);
                  tramite.setRemessa(remessa);
                  this.tramiteService.save(tramite);

                  // Rascunho
                  remessa.setTramiteRemessaAtual(tramite);
                }else {
                  this.update(remessa);
                }
                
            } catch (BusinessException e) {
                numeroRejeitadas++;
                msgValidacao.add(String.format("Linha %s - %s", registro.getRecordNumber(), e.getMessage()));
            } 
        }
        
        if (!ObjectUtils.isNullOrEmpty(msgValidacao)) {
        	this.transaction.rollback();
        	
            RequisicaoLoteException e = new RequisicaoLoteException(msgValidacao);
            e.setNumeroRejeitadas(numeroRejeitadas);
            
            throw e;
        }
        
        this.transaction.commit();
    }
	
	@Override
	public Integer count(Map<String, Object> filters) throws DataBaseException {
		return null;
	}

	@Override
	protected GenericDAO<RemessaVO> getDAO() {
		return this.dao;
	}

  @Override
  protected void validaCamposObrigatorios(RemessaVO entity) throws BusinessException {
    
  }

  @Override
  protected void validaRegras(RemessaVO entity) throws BusinessException {
    
  }

  @Override
  protected void validaRegrasExcluir(RemessaVO entity) throws BusinessException {
    
  }

  @Override
  public List<RemessaVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) throws DataBaseException {
    return null;
  }

}
