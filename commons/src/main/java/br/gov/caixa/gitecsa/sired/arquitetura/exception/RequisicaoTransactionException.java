package br.gov.caixa.gitecsa.sired.arquitetura.exception;

import java.util.Date;

import javax.ejb.ApplicationException;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.Util;

@ApplicationException(rollback = true)
public class RequisicaoTransactionException extends DataBaseException {

  private static final long serialVersionUID = 6498157868306431575L;
 
  @Inject
  private Logger logger;

  public RequisicaoTransactionException() {
      super();
  }

  /**
   * Exception que carrega todas as mensagens de validação da base de dados.
   * 
   * @param e
   */
  public RequisicaoTransactionException(String e) {
      super(e);
      // logger.error(e);
  }

  /**
   * Exception que carrega todas as mensagens de validação da base de dados.
   * 
   * @param e
   * @param classname
   */
  public RequisicaoTransactionException(Throwable e, String classname) {
      super(e);
      String mensagemLog = " DATA: " + Util.formatData(new Date(), "dd/MM/yyyy hh:mm:ss") + FileUtils.SYSTEM_EOL + "EVENTO: " + FileUtils.SYSTEM_EOL + "ERRO: FALHA AO CONECTAR BANCO DE DADOS. ";
      logger.error(mensagemLog);
  }

  /**
   * Exception que carrega todas as mensagens de validação da base de dados.
   * 
   * @param e
   */
  public RequisicaoTransactionException(Throwable e) {
      super(e);
      logger.error("Message: " + e.getMessage() + " Cause: " + getCause(), e);
  }

  /**
   * Exception que carrega todas as mensagens de validação da base de dados.
   * 
   * @param messagem
   * @param e
   */
  public RequisicaoTransactionException(String mensagem, Exception e) {
      super(mensagem);
      System.err.println("/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*");
      e.printStackTrace();
      System.err.println("/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*");
      logger.error(mensagem + " - " + e.getMessage(), e);
  }
}
