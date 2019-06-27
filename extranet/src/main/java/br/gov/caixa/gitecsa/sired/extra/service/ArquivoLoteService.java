package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.enumerator.ConclusaoAtendimentoLoteEnum;
import br.gov.caixa.gitecsa.sired.extra.dao.ArquivoLoteDAO;
import br.gov.caixa.gitecsa.sired.extra.dto.FiltroArquivoLoteDTO;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;

@Stateless
public class ArquivoLoteService extends PaginatorService<ArquivoLoteVO> implements Serializable {

    private static final long serialVersionUID = 5168207325951324573L;

    private static final String TIMESTAMP_LOG = "YYMMddHHmm";

    @Inject
    private ArquivoLoteDAO dao;

    @Inject
    private EmpresaService empresaService;

    @Override
    protected void validaCamposObrigatorios(ArquivoLoteVO entity) throws BusinessException {

    }

    @Override
    protected void validaRegras(ArquivoLoteVO entity) throws BusinessException {

    }

    @Override
    protected void validaRegrasExcluir(ArquivoLoteVO entity) throws BusinessException {

    }

    @Override
    protected GenericDAO<ArquivoLoteVO> getDAO() {
        return this.dao;
    }

    /**
     * Registra um novo atendimento em lote na base de dados
     * 
     * @param arquivoEnviado
     *            Arquivo enviado para processamento
     * @param usuario
     *            Usuário logado no sistema
     * @return
     * @throws BusinessException
     */
    public ArquivoLoteVO salvar(String arquivoEnviado, UsuarioLdap usuario) throws BusinessException {

        try {

            ArquivoLoteVO arquivoLote = new ArquivoLoteVO();
            arquivoLote.setNome(FilenameUtils.getName(arquivoEnviado));
            arquivoLote.setDataEnvioArquivo(Calendar.getInstance().getTime());
            arquivoLote.setCodigoUsuario(usuario.getEmail());

            String log = String.format("%s.log", FilenameUtils.getBaseName(arquivoEnviado));
            arquivoLote.setRelatorioLote(FileUtils.appendDateTimeToFileName(log, new Date(), TIMESTAMP_LOG));
            arquivoLote.setEmpresa(this.empresaService.obterEmpresaCNPJ(usuario.getNuCnpj()));
            arquivoLote.setConcluido(ConclusaoAtendimentoLoteEnum.NAO);

            return this.save(arquivoLote);

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
    
    /**
     * Atualiza os dados de um atendimento em lote
     * 
     * @param arquivoLote
     *            Arquivo lote
     * @return
     * @throws BusinessException
     */
    public ArquivoLoteVO atualizar(ArquivoLoteVO arquivoLote) throws BusinessException {
        try {
            return this.update(arquivoLote);

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
    
    /**
     * Concluir atendimento em lote
     * 
     * @param arquivoLote
     *            Arquivo lote
     * @param hasErros
     *            Indica se foi concluído com erros          
     */
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
    public Integer count(Map<String, Object> filters) {
        FiltroArquivoLoteDTO filtro = (FiltroArquivoLoteDTO) filters.get("filtroDTO");
        return this.dao.count(filtro);
    }

    @Override
    public List<ArquivoLoteVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) {
        FiltroArquivoLoteDTO filtro = (FiltroArquivoLoteDTO) filters.get("filtroDTO");

        return this.dao.consultar(filtro, offset, limit);
    }

}
