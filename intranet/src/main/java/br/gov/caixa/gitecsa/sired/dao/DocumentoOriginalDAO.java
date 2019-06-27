package br.gov.caixa.gitecsa.sired.dao;

import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroDocumentoOriginalDTO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public interface DocumentoOriginalDAO extends GenericDAO<DocumentoOriginalVO> {

    DocumentoOriginalVO findByIdEager(Long id);

    DocumentoOriginalVO findByCodigo(Long codigo);

    List<DocumentoOriginalVO> consultar(FiltroDocumentoOriginalDTO filtro, Integer offset, Integer limit);

    List<DocumentoOriginalVO> consultar(FiltroDocumentoOriginalDTO filtro);

    List<DocumentoOriginalVO> consultar(FiltroDocumentoOriginalDTO filtro, List<UnidadeVO> unidadesAutorizadas);

    List<DocumentoOriginalVO> consultar(FiltroDocumentoOriginalDTO filtro, List<UnidadeVO> unidadesAutorizadas, Integer offset, Integer limit);

    Integer count(FiltroDocumentoOriginalDTO filtro);

    Integer count(FiltroDocumentoOriginalDTO filtro, List<UnidadeVO> unidadesAutorizadas);
    
}
