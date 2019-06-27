package br.gov.caixa.gitecsa.sired.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dao.DocumentoDAO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;

@Stateless(name = "documentoRequisicaoService")
public class DocumentoService extends PaginatorService<DocumentoVO> {
    public static final long serialVersionUID = -2454270522433838542L;

    @Inject
    private DocumentoDAO dao;
    
    protected void validaCamposObrigatorios(DocumentoVO entity) {

    }

    @Override
    protected void validaRegras(DocumentoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(DocumentoVO entity) {

    }

    @Override
    protected GenericDAO<DocumentoVO> getDAO() {
        return this.dao;
    }

    @Override
    public List<DocumentoVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) {
        DocumentoVO filtro = (DocumentoVO) filters.get("filtro");
        return this.dao.consultar(filtro, offset, limit);
    }

    @Override
    public Integer count(Map<String, Object> filters) {
        DocumentoVO filtro = (DocumentoVO) filters.get("filtro");
        return this.dao.count(filtro);
    }

    public DocumentoVO findByIdEager(Long id) {
        return this.dao.findByIdEager(id);
    }
    
    public Set<GrupoCampoVO> getUltimaVersaoGrupoCampoDocumento(final DocumentoVO documento) {
        return this.dao.obterUltimaVersaoCamposDocumento(documento);
    }

  public List<DocumentoVO> obterListaDocumentosPorFiltro(String nomeFiltro) throws DataBaseException {
    return this.dao.obterListaDocumentosPorFiltro(nomeFiltro);
  }

  public DocumentoVO buscarMovimentoDiario() throws DataBaseException {
    return this.dao.buscarMovimentoDiario();
  }

}
