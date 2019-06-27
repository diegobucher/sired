package br.gov.caixa.gitecsa.sired.service;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dao.DocumentoOriginalDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroDocumentoOriginalDTO;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.vo.DocumentoOriginalVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless
public class DocumentoOriginalService extends PaginatorService<DocumentoOriginalVO> {

    private static final long serialVersionUID = 8099054745374358827L;

    @Inject
    private DocumentoOriginalDAO dao;
    
    @Inject
    private UnidadeService unidadeService;
    
    public List<DocumentoOriginalVO> pesquisar(Map<String, Object> filters) throws DataBaseException {
        return this.pesquisar(null, null, filters);
    }

    @Override
    public List<DocumentoOriginalVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) throws DataBaseException {

        FiltroDocumentoOriginalDTO filtro = (FiltroDocumentoOriginalDTO) filters.get("filtroDTO");

        if (ObjectUtils.isNullOrEmpty(filtro.getUnidadeSolicitante()) && !JSFUtil.isPerfil(Constantes.PERFIL_GESTOR)
                && !JSFUtil.isPerfil(Constantes.PERFIL_AUDITOR)) {

            UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
            List<UnidadeVO> unidades = this.unidadeService.findAllByPerfil(usuario);

            if (JSFUtil.isPerfil(Constantes.PERFIL_GESTOR)) {
                filtro.setMatriculaUsuario(usuario.getNuMatricula());
            }

            return (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) ? this.dao.consultar(filtro, unidades, offset, limit) : this.dao
                    .consultar(filtro, unidades);
        }

        return (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) ? this.dao.consultar(filtro, offset, limit) : this.dao
                .consultar(filtro);
    }

    @Override
    public Integer count(Map<String, Object> filters) throws DataBaseException {

        FiltroDocumentoOriginalDTO filtro = (FiltroDocumentoOriginalDTO) filters.get("filtroDTO");

        if (ObjectUtils.isNullOrEmpty(filtro.getUnidadeSolicitante()) && !JSFUtil.isPerfil(Constantes.PERFIL_GESTOR)
                && !JSFUtil.isPerfil(Constantes.PERFIL_AUDITOR)) {

            UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
            List<UnidadeVO> unidades = this.unidadeService.findAllByPerfil(usuario);

            return this.dao.count(filtro, unidades);
        }

        return this.dao.count(filtro);
    }

    public DocumentoOriginalVO findByIdEager(Long id) {
        return this.dao.findByIdEager(id);
    }
    
    public DocumentoOriginalVO findByCodigo(Long codigo) {
        return this.dao.findByCodigo(codigo);
    }

    @Override
    protected void validaCamposObrigatorios(DocumentoOriginalVO entity) throws BusinessException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void validaRegras(DocumentoOriginalVO entity) throws BusinessException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void validaRegrasExcluir(DocumentoOriginalVO entity) throws BusinessException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected GenericDAO<DocumentoOriginalVO> getDAO() {
        return this.dao;
    }

}
