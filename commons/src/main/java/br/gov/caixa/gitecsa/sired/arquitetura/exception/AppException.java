package br.gov.caixa.gitecsa.sired.arquitetura.exception;

public class AppException extends Exception {

    private static final long serialVersionUID = 6684049221066265039L;

    public AppException() {
        super();
    }

    public AppException(String mensagem, Throwable cause) {
        super(mensagem, cause);
    }

    public AppException(String mensagem) {
        super(mensagem);
    }

    public AppException(Throwable cause) {
        super(cause);
    }

}
