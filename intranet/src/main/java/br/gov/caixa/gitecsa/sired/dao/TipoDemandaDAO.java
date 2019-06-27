package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;

public interface TipoDemandaDAO extends GenericDAO<TipoDemandaVO> {
    List<TipoDemandaVO> findByDocumento(DocumentoVO documento) throws DataBaseException;

    TipoDemandaVO findByNomeESetorial(String nome, TipoDocumentoEnum setorial) throws DataBaseException;
}
