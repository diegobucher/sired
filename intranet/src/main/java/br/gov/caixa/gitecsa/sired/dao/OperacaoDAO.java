package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import javax.persistence.PersistenceException;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.OperacaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface OperacaoDAO extends GenericDAO<OperacaoVO> {
    List<OperacaoVO> findAllByAreaMeioEDocumento(UnidadeVO unidade, DocumentoVO documento);
    OperacaoVO findById(final String id) throws DataBaseException, PersistenceException, Exception;
}
