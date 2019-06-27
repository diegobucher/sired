package br.gov.caixa.gitecsa.sired.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.TipoDemandaDAO;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

@Stateless(name = "tipoDemandaDocumentoService")
public class TipoDemandaService extends AbstractService<TipoDemandaVO> {

    private static final long serialVersionUID = -2929975075342477977L;

    @Inject
    private TipoDemandaDAO dao;

    @Override
    protected void validaCamposObrigatorios(TipoDemandaVO entity) {

    }

    @Override
    protected void validaRegras(TipoDemandaVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(TipoDemandaVO entity) {

    }

    @Override
    protected GenericDAO<TipoDemandaVO> getDAO() {
        return this.dao;
    }

    public List<TipoDemandaVO> findByDocumento(DocumentoVO documento) throws DataBaseException {
        return this.dao.findByDocumento(documento);
    }

    public TipoDemandaVO findByNomeESetorial(String nome, TipoDocumentoEnum setorial) throws DataBaseException {
        return this.dao.findByNomeESetorial(nome, setorial);
    }
}
