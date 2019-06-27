package br.gov.caixa.gitecsa.sired.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.DocumentoOriginalDAO;
import br.gov.caixa.gitecsa.sired.dao.TramiteDocumentoOriginalDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoDocumentoOriginalEnum;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoDocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteDocumentoOriginalVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class TramiteDocumentoOriginalService extends AbstractService<TramiteDocumentoOriginalVO> {

    private static final long serialVersionUID = -7684951052662237158L;

    @Inject
    private TramiteDocumentoOriginalDAO dao;

    @Inject
    private DocumentoOriginalDAO docOriginalDao;

    public List<TramiteDocumentoOriginalVO> pesquisarHistorico(DocumentoOriginalVO docOriginal) {
        return this.dao.findByDocumentoOriginal(docOriginal);
    }

    public TramiteDocumentoOriginalVO getBySituacao(SituacaoDocumentoOriginalEnum value) {
        return this.getBySituacao(value, JSFUtil.getUsuario().getNuMatricula());
    }

    public TramiteDocumentoOriginalVO getBySituacao(SituacaoDocumentoOriginalEnum value, String usuario) {

        TramiteDocumentoOriginalVO instance = new TramiteDocumentoOriginalVO();
        instance.setCodigoUsuario(usuario);
        instance.setDataHora(new Date());
        
        SituacaoDocumentoOriginalVO situacao = new SituacaoDocumentoOriginalVO();
        situacao.setId(value.getId());
        instance.setSituacaoDocOriginal(situacao);

        return instance;
    }

    @Override
    protected void validaCamposObrigatorios(TramiteDocumentoOriginalVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(TramiteDocumentoOriginalVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(TramiteDocumentoOriginalVO entity) throws BusinessException {
        
    }

    @Override
    protected GenericDAO<TramiteDocumentoOriginalVO> getDAO() {
        return this.dao;
    }
    
    public Date getDataEnvio(DocumentoOriginalVO docOriginal) {
        return this.dao.getDataEnvio(docOriginal);
    }

    public void atualizarTramite(TramiteDocumentoOriginalVO tramite) throws RequiredException, BusinessException, Exception {
        tramite = this.save(tramite);
        tramite.getDocumentoOriginal().setTramiteDocOriginalAtual(tramite);
        this.docOriginalDao.update(tramite.getDocumentoOriginal());
    }
}
