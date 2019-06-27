package br.gov.caixa.gitecsa.sired.service;

import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.ArquivoLoteDAO;
import br.gov.caixa.gitecsa.sired.enumerator.ConclusaoAtendimentoLoteEnum;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;

@Stateless
public class ArquivoLoteService extends AbstractService<ArquivoLoteVO> {

    private static final long serialVersionUID = 4473976909411943579L;

    private static final String TIMESTAMP_LOG = "YYMMddHHmm";

    @Inject
    private ArquivoLoteDAO dao;
    
    public ArquivoLoteVO salvar(String arquivoEnviado, UsuarioLdap usuario) throws BusinessException {

        try {

            ArquivoLoteVO arquivoLote = new ArquivoLoteVO();
            arquivoLote.setNome(FilenameUtils.getName(arquivoEnviado));
            arquivoLote.setDataEnvioArquivo(Calendar.getInstance().getTime());
            arquivoLote.setCodigoUsuario(usuario.getNuMatricula());

            String log = String.format("%s.log", FilenameUtils.getBaseName(arquivoEnviado));
            arquivoLote.setRelatorioLote(FileUtils.appendDateTimeToFileName(log, new Date(), TIMESTAMP_LOG));
            arquivoLote.setConcluido(ConclusaoAtendimentoLoteEnum.NAO);

            return this.save(arquivoLote);

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
    
    public ArquivoLoteVO atualizar(ArquivoLoteVO arquivoLote) throws BusinessException {
        try {
            return this.update(arquivoLote);

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
    
    public void concluir(ArquivoLoteVO arquivoLote, Boolean hasErros) {
        try {
            arquivoLote = this.findById((Long)arquivoLote.getId());
            arquivoLote.setConcluido(hasErros ? ConclusaoAtendimentoLoteEnum.COM_ERROS : ConclusaoAtendimentoLoteEnum.SIM);
            this.atualizar(arquivoLote);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }
    
    @Override
    protected GenericDAO<ArquivoLoteVO> getDAO() {
        return this.dao;
    }

    @Override
    protected void validaCamposObrigatorios(ArquivoLoteVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegras(ArquivoLoteVO entity) throws BusinessException {
        
    }

    @Override
    protected void validaRegrasExcluir(ArquivoLoteVO entity) throws BusinessException {
        
    }
}
