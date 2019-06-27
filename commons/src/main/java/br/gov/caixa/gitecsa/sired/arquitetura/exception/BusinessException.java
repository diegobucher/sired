package br.gov.caixa.gitecsa.sired.arquitetura.exception;

import java.util.List;

/**
 * Classe que trata todas as exceptions do negocio
 * 
 * @author jsouzaa
 *
 */
public class BusinessException extends AppException {

    private static final long serialVersionUID = 3215441466753248692L;

    private List<String> erroList;
    
    /**
     * Exception que carrega todas as mensagens de validação da regra de negocio.
     * 
     * @param mensagem
     */
    public BusinessException(String mensagem) {
        super(mensagem);
    }
    
   /**
    * Cria uma nova instância com uma mensagem de validação e a causa associada.
    * 
    * @param mensagem
    * @param cause
    */
    public BusinessException(String mensagem, Throwable cause) {
    	super(mensagem, cause);
    }

    /**
     * Exception que carrega todas as mensagens de validação da regra de negocio.
     * 
     * @param erroList
     */
    public BusinessException(List<String> erroList) {
        this.erroList = erroList;
    }

    /**
     * Exception que carrega todas as mensagens de validação da regra de negocio.
     * 
     * @param mensagem
     * @param erroList
     */
    public BusinessException(String mensagem, List<String> erroList) {
        super(mensagem);
        this.erroList = erroList;
    }

    public List<String> getErroList() {
        return erroList;
    }

    public void setErroList(List<String> erroList) {
        this.erroList = erroList;
    }
}
