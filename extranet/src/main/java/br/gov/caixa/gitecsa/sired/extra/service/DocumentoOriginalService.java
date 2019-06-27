package br.gov.caixa.gitecsa.sired.extra.service;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dto.FiltroDocumentoOriginalDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoDocumentoOriginalEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.DocumentoOriginalDAO;
import br.gov.caixa.gitecsa.sired.extra.dao.SituacaoDocumentoOriginalDAO;
import br.gov.caixa.gitecsa.sired.extra.dao.TramiteDocumentoOriginalDAO;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteDocumentoOriginalVO;

@Stateless
public class DocumentoOriginalService extends PaginatorService<DocumentoOriginalVO> {

    private static final long serialVersionUID = -8402864675421960939L;
    
    @Inject
    private DocumentoOriginalDAO dao;
    
    @Inject
    private SituacaoDocumentoOriginalDAO situacaoDao;
    
    @Inject
    private TramiteDocumentoOriginalDAO tramiteDao;
    
    @Override
    protected void validaCamposObrigatorios(DocumentoOriginalVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(DocumentoOriginalVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(DocumentoOriginalVO entity) throws BusinessException {
        
    }

    @Override
    protected GenericDAO<DocumentoOriginalVO> getDAO() {
        return this.dao;
    }
    
    /**
     * Registra o andamento do envio de um documento original.
     * 
     * @param situacaoEnum
     * @param requisicao
     */
    public void registrarAndamento(final SituacaoDocumentoOriginalEnum situacaoEnum, final RequisicaoVO requisicao, final String usuario) {
        
        DocumentoOriginalVO docOriginal = new DocumentoOriginalVO();
        
        docOriginal.setRequisicao(requisicao);
        docOriginal.setBase(requisicao.getBase());
        docOriginal.setEmpresaContrato(requisicao.getEmpresaContrato());
        docOriginal = this.dao.save(docOriginal);
        
        TramiteDocumentoOriginalVO tramiteDocOriginal = new TramiteDocumentoOriginalVO();
        tramiteDocOriginal.setDocumentoOriginal(docOriginal);
        tramiteDocOriginal.setSituacaoDocOriginal(this.situacaoDao.findById(situacaoEnum.getId()));
        tramiteDocOriginal.setCodigoUsuario(usuario);
        tramiteDocOriginal = this.tramiteDao.save(tramiteDocOriginal);
        
        docOriginal.setTramiteDocOriginalAtual(tramiteDocOriginal);
        this.dao.update(docOriginal);
    }
    
    public List<DocumentoOriginalVO> pesquisar(Map<String, Object> filters) {
        return this.pesquisar(null, null, filters);
    }

    @Override
    public List<DocumentoOriginalVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) {

        FiltroDocumentoOriginalDTO filtro = (FiltroDocumentoOriginalDTO) filters.get("filtroDTO");

        return (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) ? this.dao.consultar(filtro, offset, limit) : this.dao
                .consultar(filtro);
    }

    @Override
    public Integer count(Map<String, Object> filters) {
        FiltroDocumentoOriginalDTO filtro = (FiltroDocumentoOriginalDTO) filters.get("filtroDTO");
        return this.dao.count(filtro);
    }

    public DocumentoOriginalVO findByIdEager(Long id) {
        return this.dao.findByIdEager(id);
    }
    
    public DocumentoOriginalVO findByCodigo(Long codigo) {
        return this.dao.findByCodigo(codigo);
    }
}
