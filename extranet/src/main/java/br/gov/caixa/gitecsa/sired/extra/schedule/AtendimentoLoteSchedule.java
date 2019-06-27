package br.gov.caixa.gitecsa.sired.extra.schedule;

import java.util.Date;
import java.util.TimerTask;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.extra.service.ArquivoLoteService;
import br.gov.caixa.gitecsa.sired.extra.service.AtendimentoLoteBatchService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;

public class AtendimentoLoteSchedule extends TimerTask {

    private static final String LOOKUP = "java:global/";

    private static final String EXTRANET = "extranet";

    private String arquivoCompactado;

    private String diretorioTemporario;

    private UsuarioLdap usuarioLogado;

    private AtendimentoLoteBatchService atendimentoLoteService;
    
    private ArquivoLoteService arquivoLoteService;

    private transient Logger logger = LogUtils.getLogger(this.getClass().getName(), System.getProperty(Constantes.EXTRANET_LOG4J));

    public AtendimentoLoteSchedule(String arquivoCompactado, String diretorioTemporario, UsuarioLdap usuarioLogado) throws AppException {

        this.arquivoCompactado = arquivoCompactado;
        this.diretorioTemporario = diretorioTemporario;
        this.usuarioLogado = usuarioLogado;

        try {
            this.atendimentoLoteService = (AtendimentoLoteBatchService) ObjectUtils.lookup(EXTRANET, LOOKUP, AtendimentoLoteBatchService.class.getSimpleName());
            this.arquivoLoteService = (ArquivoLoteService) ObjectUtils.lookup(EXTRANET, LOOKUP, ArquivoLoteService.class.getSimpleName());
        } catch (NamingException e) {
            logger.error(criarLog(e.getMessage()), e);
            throw new AppException(e.getMessage());
        }
    }

    @Override
    public void run() {

        try {

            logger.info(criarLog("PROCESSANDO ARQUIVO " + this.arquivoCompactado));
            ArquivoLoteVO arquivoLote = this.arquivoLoteService.salvar(arquivoCompactado, this.usuarioLogado);
            this.atendimentoLoteService.atenderBatch(this.arquivoCompactado, this.diretorioTemporario, this.usuarioLogado, arquivoLote);
            logger.info(criarLog("ARQUIVO " + this.arquivoCompactado + " PROCESSADO COM SUCESSO"));

        } catch (Exception e) {
            logger.error(criarLog(e.getMessage()), e);
        } finally {
            this.cancel();
        }
    }

    private static String criarLog(String mensagem) {
        return Constantes.LOG_MARCADOR + DateUtils.format(new Date(), DateUtils.DATETIME_FORMAT) + " BATCH DE ATENDIMENTO EM LOTE: " + mensagem
                + Constantes.LOG_MARCADOR;
    }
}
