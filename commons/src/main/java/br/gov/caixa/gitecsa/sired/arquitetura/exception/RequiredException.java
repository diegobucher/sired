package br.gov.caixa.gitecsa.sired.arquitetura.exception;

import java.util.List;

/**
 * Classe que trata todas as exceptions de campos requeridos 
 *
 */
public class RequiredException extends AppException {

	private static final long serialVersionUID = -4806939480794045985L;

	private List<String> erroList;
	
    public RequiredException() {
    }
    
    /**
     * Exception que carrega todas as mensagens de validação dos campos requeridos.
     * @param mensagem
     */
    public RequiredException(String mensagem) {
        super(mensagem);
    }
    
    /**
     * Exception que carrega todas as mensagens de validação dos campos requeridos.
     * @param erroList
     */
    public RequiredException(List<String> erroList) {
    	this.erroList = erroList;
    }
    
    /**
     * Exception que carrega todas as mensagens de validação dos campos requeridos.
     * @param mensagem
     * @param erroList
     */
    public RequiredException(String mensagem, List<String> erroList) {
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
