package br.gov.caixa.gitecsa.sired.service;

import java.math.BigInteger;
import java.util.Calendar;

import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.dao.CodigoRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.dto.CodigoRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Singleton
@AccessTimeout(value = 30000)
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER) 
public class SequencialRequisicaoService {

    @Inject
    private CodigoRequisicaoDAO dao;
    
    @Lock(LockType.WRITE)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Long generate(final UnidadeVO unidade) throws BusinessException {

        Integer ano = DateUtils.getShortYear(Calendar.getInstance());

        try {

            CodigoRequisicaoDTO ultima = this.dao.findByUnidadeAno(unidade, ano);
            if (ObjectUtils.isNullOrEmpty(ultima)) {
                ultima = new CodigoRequisicaoDTO();
                ultima.setUnidade(unidade);
                ultima.setNuAno(ano);
                ultima.setNuRequisicao(BigInteger.ONE.intValue());
                this.dao.insert(ultima);
            } else {
                ultima.setNuRequisicao(ultima.getNuRequisicao() + 1);
                this.dao.update(ultima);
            }
            
            return Long.valueOf(ultima.toString());

        } catch (Exception e) {
            throw new BusinessException(MensagemUtils.obterMensagem("geral.exception.cannotGenerateSequence"));
        }
    }
}
