package br.gov.caixa.gitecsa.sired.arquitetura.exception;

import java.util.Date;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.Util;

public class DataBaseException extends AppException {

    private static final long serialVersionUID = 6498157868306431575L;
    
    @Inject
    private Logger logger;

    public DataBaseException() {
        super();
    }

    /**
     * Exception que carrega todas as mensagens de validação da base de dados.
     * 
     * @param e
     */
    public DataBaseException(String e) {
        super(e);
        // logger.error(e);
    }

    /**
     * Exception que carrega todas as mensagens de validação da base de dados.
     * 
     * @param e
     * @param classname
     */
    public DataBaseException(Throwable e, String classname) {
        super(e);
        String mensagemLog = " DATA: " + Util.formatData(new Date(), "dd/MM/yyyy hh:mm:ss") + FileUtils.SYSTEM_EOL + "EVENTO: " + FileUtils.SYSTEM_EOL + "ERRO: FALHA AO CONECTAR BANCO DE DADOS. ";
        logger.error(mensagemLog);
    }

    /**
     * Exception que carrega todas as mensagens de validação da base de dados.
     * 
     * @param e
     */
    public DataBaseException(Throwable e) {
        super(e);
        logger.error("Message: " + e.getMessage() + " Cause: " + getCause(), e);
    }

    /**
     * Exception que carrega todas as mensagens de validação da base de dados.
     * 
     * @param messagem
     * @param e
     */
    public DataBaseException(String mensagem, Exception e) {
        super(mensagem);
        System.err.println("/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*");
        e.printStackTrace();
        System.err.println("/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*");
        logger.error(mensagem + " - " + e.getMessage(), e);
    }

}
