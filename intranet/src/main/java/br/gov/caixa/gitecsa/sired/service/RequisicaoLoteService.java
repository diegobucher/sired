package br.gov.caixa.gitecsa.sired.service;

import java.util.ArrayList;
import java.util.Date;
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
import br.gov.caixa.gitecsa.sired.dao.RequisicaoDAO;
import br.gov.caixa.gitecsa.sired.dto.RequisicaoLoteDTO;
import br.gov.caixa.gitecsa.sired.enumerator.FormatoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class RequisicaoLoteService extends PaginatorService<RequisicaoVO> {

	private static final long serialVersionUID = -3296864569022811757L;

	private static final int TRANSACTION_TIMEOUT = 7200; // 2 horas
	
	@Resource
    private UserTransaction transaction;
	
	@Inject
    private TramiteRequisicaoService tramiteService;
	
	@Inject
    private SequencialRequisicaoService sequencialService;
	
	@Inject
	RequisicaoService requisicaoService;
	
	@Inject
    private BaseService baseService;
	
	@Inject
    private RequisicaoDAO dao;
	
	public void salvar(List<RequisicaoLoteDTO> requisicoes) throws RequisicaoLoteException, RequiredException, Exception {
        
        Integer numeroRejeitadas = 0;
        List<String> msgValidacao = new ArrayList<String>();
        
        this.transaction.setTransactionTimeout(TRANSACTION_TIMEOUT);
        this.transaction.begin();
        
        for (RequisicaoLoteDTO requisicaoLote : requisicoes) {
            
            CSVRecord registro = requisicaoLote.getRegistro();
            RequisicaoVO requisicao = requisicaoLote.getRequisicao();
            
            try {
                // Cria a requisição
                requisicao.setCodigoRequisicao(this.sequencialService.generate(requisicao.getUnidadeSolicitante()));
                this.save(requisicao);
                
                TramiteRequisicaoVO tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.RASCUNHO);
                tramite.setRequisicao(requisicao);
                this.tramiteService.save(tramite);
                
                // Rascunho
                requisicao.setTramiteRequisicaoAtual(tramite);
                this.update(requisicao);
                
                // Concluir requisição sem validar as requisições, etapas já feitas antes da importação.
                this.concluir(requisicao);
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
	
	public void concluir(RequisicaoVO requisicao) throws BusinessException {

        try {
        	
            // a qtde solicitada depende do intervalo de datas e do tipo do
            // documento, devendo ser executada ao gravar e ao concluir a
            // requisição.
            requisicao.setQtSolicitadaDocumento(this.requisicaoService.calcularQuantidadeDocumentosSolicitada(requisicao));
            
            // Sistema verifica e registra a empresa responsável pelo atendimento à esta requisição. FE2.
            // Sistema verifica e registra a base responsável pela área onde o documento está localizado. [RN003] FE5
            EmpresaContratoVO contrato = this.baseService.findContratoBaseByDocumentoEager(requisicao.getDocumento(), requisicao
						.getRequisicaoDocumento().getDataGeracao());
            
            if (ObjectUtils.isNullOrEmpty(contrato)) {
            	contrato = this.baseService.findContratoBaseByUnidadeEager(requisicao.getRequisicaoDocumento().getUnidadeGeradora());
            	
                if (ObjectUtils.isNullOrEmpty(contrato)) {
                    throw new BusinessException(MensagemUtils.obterMensagem("ME006", requisicao.getRequisicaoDocumento().getUnidadeGeradora()
                            .getDescricaoCompleta()));
                }
            }
            
            requisicao.setBase(contrato.getBase());
            requisicao.setEmpresaContrato(contrato);

            // Caso seja solicitado documento em formato ORIGINAL, atualiza a
            // situação atual da
            // Requisição para EM AUTORIZAÇÃO, caso contrário para ABERTA.
            // Registra no trâmite o usuário e a
            // data/hora da operação.
            TramiteRequisicaoVO tramite = null;

            if (!ObjectUtils.isNullOrEmpty(requisicao.getFormato()) && requisicao.getFormato().equals(FormatoDocumentoEnum.ORIGINAL)
                    && !this.requisicaoService.hasAutorizacaoAutomaticaByUnidade(requisicao)) {
                tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.EM_AUTORIZACAO);
            } else {
                tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.ABERTA);
            }

            tramite.setRequisicao(requisicao);
            this.tramiteService.save(tramite);

            requisicao.setDataHoraAbertura(new Date());
            requisicao.setPrazoAtendimento(this.requisicaoService.calcularPrazoAtendimento(requisicao));
            requisicao.setTramiteRequisicaoAtual(tramite);
            
            this.update(requisicao);

        } catch (BusinessException e) {
        	this.logger.error("RequisicaoService.concluir.BusinessException");
            throw e;
        } catch (Exception e) {
        	this.logger.error("RequisicaoService.concluir.Exception");
            throw new BusinessException(MensagemUtils.obterMensagem("MA012"), e);
        }
    }

	@Override
	public Integer count(Map<String, Object> filters) throws DataBaseException {
		return null;
	}

	@Override
	public List<RequisicaoVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters)
			throws DataBaseException {
		return null;
	}

	@Override
	protected void validaCamposObrigatorios(RequisicaoVO entity) throws BusinessException {
		
	}

	@Override
	protected void validaRegras(RequisicaoVO entity) throws BusinessException {
		
	}

	@Override
	protected void validaRegrasExcluir(RequisicaoVO entity) throws BusinessException {
		
	}

	@Override
	protected GenericDAO<RequisicaoVO> getDAO() {
		return this.dao;
	}

}
