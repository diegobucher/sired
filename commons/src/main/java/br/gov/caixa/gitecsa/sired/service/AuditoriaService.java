package br.gov.caixa.gitecsa.sired.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.AuditoriaDAO;
import br.gov.caixa.gitecsa.sired.enumerator.TipoAuditoriaEnum;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.AuditoriaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoAuditoriaVO;

@Stateless
public class AuditoriaService extends AbstractService<AuditoriaVO> {

	private static final String NOME_DOC_DIGITAL = "<NOME_DOC_DIGITAL>";

	private static final long serialVersionUID = 3931237232045278453L;
	
	@Inject
    private AuditoriaDAO dao;
    
    public void gravar(final RequisicaoVO requisicao, final UsuarioLdap usuario) throws RequiredException, DataBaseException {
    	
    	try {
	    	AuditoriaVO registro = new AuditoriaVO();
	    	
	    	registro.setTipo(this.findTipoAuditoriaById(TipoAuditoriaEnum.DOWNLOAD_EFETUADO.getId()));
	    	
	    	if (!ObjectUtils.isNullOrEmpty(usuario.getNuMatricula())) {		// matricula da intranet
	    		registro.setCodigoUsuario(usuario.getNuMatricula());
			} else if (!ObjectUtils.isNullOrEmpty(usuario.getEmail())) {	// empresa da extranet
				registro.setCodigoUsuario(usuario.getEmail());
			}
	    	registro.setDataHoraOcorrencia(new Date());
	    	registro.setIdentificador(((Long) requisicao.getId()).intValue());
	    	
	    	String texto = registro.getTipo().getTexto();
	    	texto = texto.replace(NOME_DOC_DIGITAL, requisicao.getTramiteRequisicaoAtual().getArquivoDisponibilizado());
	    	
	    	registro.setDescricao(texto);
    	
			this.save(registro);
		} catch (RequiredException e) {
			throw e;
		} catch (Exception e) {
			throw new DataBaseException(e);
		}
    }
    
    public List<AuditoriaVO> findAllByIdentificador(final Integer identificador) {
    	return this.dao.findAllByIdentificador(identificador);
    }
    
    @Override
    protected GenericDAO<AuditoriaVO> getDAO() {
        return this.dao;
    }

    @Override
    protected void validaCamposObrigatorios(AuditoriaVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(AuditoriaVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(AuditoriaVO entity) throws BusinessException {
        
    }
    
    public TipoAuditoriaVO findTipoAuditoriaById(final Integer id) {
    	return this.dao.findTipoAuditoriaById(id);
    }
}
