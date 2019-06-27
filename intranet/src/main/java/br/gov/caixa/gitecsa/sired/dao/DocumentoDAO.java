package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;
import java.util.Set;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;

public interface DocumentoDAO extends GenericDAO<DocumentoVO> {
    Integer count(DocumentoVO filtro);

    List<DocumentoVO> consultar(DocumentoVO filtro, Integer offset, Integer limit);

    DocumentoVO findByIdEager(Long id);
    
    Set<GrupoCampoVO> obterUltimaVersaoCamposDocumento(final DocumentoVO filtro);

    List<DocumentoVO> obterListaDocumentosPorFiltro(String nomeFiltro) throws DataBaseException;

    DocumentoVO buscarMovimentoDiario() throws DataBaseException;
}
