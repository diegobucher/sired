package br.gov.caixa.gitecsa.exception;

import java.util.Date;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.Util;

public class ViewException extends AppException {

    private static final long serialVersionUID = 6498157868306431575L;
    @Inject
    private Logger logger;

    public ViewException() {
        super();

        String mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: "
                + Util.formatData(new Date(), "dd/MM/yyyy hh:mm:ss") + "EVENTO: ATUALIZAR TABELA DE [TIPOS DE UNIDADE/UNIDADES/FERIADOS] (SIICO)"
                + FileUtils.SYSTEM_EOL + "EVENTO: CARREGAR TABELA DE MUNICÍPIOS (SIICO)" + FileUtils.SYSTEM_EOL + "ERRO: FONTE DE DADOS INCONSISTENTE"
                + Constantes.LOG_MARCADOR;

        logger.info(mensagemLog);
        logger.error(mensagemLog);

    }

    /**
     * Exception que carrega todas as mensagens de validação da base de dados.
     * 
     * @param e
     */
    public ViewException(String e) {
        super(e);
        logger.error(e);
    }

    /**
     * Exception que carrega todas as mensagens de validação da base de dados.
     * 
     * @param e
     * @param classname
     */
    public ViewException(Throwable e, String classname) {

        super(e);

        String mensagemLog = " DATA: " + Util.formatData(new Date(), "dd/MM/yyyy hh:mm:ss") + FileUtils.SYSTEM_EOL + "EVENTO: " + FileUtils.SYSTEM_EOL
                + "ERRO: FALHA AO CONECTAR BANCO DE DADOS. ";
        logger.error(mensagemLog);
    }

    /**
     * Exception que carrega todas as mensagens de validação da base de dados.
     * 
     * @param e
     */
    public ViewException(Throwable e) {
        super(e);
        logger.error("Message: " + e.getMessage() + " Cause: " + getCause(), e);
    }

    /**
     * Exception que carrega todas as mensagens de validação da base de dados.
     * 
     * @param messagem
     * @param e
     */
    public ViewException(String mensagem, Exception e) {
        super(mensagem);
        logger.error(mensagem + " - " + e.getMessage(), e);
    }

    public ViewException(Exception e) {
        super(e);

        String mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: "
                + Util.formatData(new Date(), "dd/MM/yyyy hh:mm:ss") + "EVENTO: ATUALIZAR TABELA DE [TIPOS DE UNIDADE/UNIDADES/FERIADOS] (SIICO)"
                + FileUtils.SYSTEM_EOL + "EVENTO: CARREGAR TABELA DE MUNICÍPIOS (SIICO)" + FileUtils.SYSTEM_EOL + "REGISTROS NAO ATUALIZADOS - VIEW VAZIA"
                + Constantes.LOG_MARCADOR;

        logger.info(mensagemLog);
        logger.error(mensagemLog);
        logger.error("Message: " + e.getMessage() + " Cause: " + getCause(), e);
    }
}
