package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.extra.dao.SituacaoRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;

@Stateless
public class SituacaoRequisicaoService extends AbstractService<SituacaoRequisicaoVO> implements Serializable {

    private static final long serialVersionUID = -6626811316174022428L;

    @Inject
    private SituacaoRequisicaoDAO dao;

    @Override
    protected void validaCamposObrigatorios(SituacaoRequisicaoVO entity) throws BusinessException {

    }

    @Override
    protected void validaRegras(SituacaoRequisicaoVO entity) throws BusinessException {

    }

    @Override
    protected void validaRegrasExcluir(SituacaoRequisicaoVO entity) throws BusinessException {

    }

    @Override
    protected GenericDAO<SituacaoRequisicaoVO> getDAO() {
        return this.dao;
    }

}
