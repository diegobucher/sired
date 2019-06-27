package br.gov.caixa.gitecsa.sired.extra.dao;

import java.util.Date;
import java.util.List;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteDocumentoOriginalVO;

public interface TramiteDocumentoOriginalDAO extends GenericDAO<TramiteDocumentoOriginalVO> {

    List<TramiteDocumentoOriginalVO> findByDocumentoOriginal(DocumentoOriginalVO docOriginal);

    Date getDataEnvio(DocumentoOriginalVO docOriginal);

}
