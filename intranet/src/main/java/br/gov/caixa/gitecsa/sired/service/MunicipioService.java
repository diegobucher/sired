package br.gov.caixa.gitecsa.sired.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.MunicipioDAO;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;

@Stateless(name = "municipioSistemaService")
public class MunicipioService extends AbstractService<MunicipioVO> {

    private static final long serialVersionUID = 1334585599832215913L;
    
    @Inject
    private MunicipioDAO dao;

    @Override
    protected void validaCamposObrigatorios(MunicipioVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(MunicipioVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(MunicipioVO entity) throws BusinessException {
        
    }

    @Override
    protected GenericDAO<MunicipioVO> getDAO() {
        return this.dao;
    }

}
