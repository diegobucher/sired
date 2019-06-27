package br.gov.caixa.gitecsa.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class AppException extends Exception {

    private static final long serialVersionUID = -5177640136821474101L;

    public AppException() {
        super();
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);

    }

    public AppException(String message) {
        super(message);
    }

    public AppException(Throwable cause) {
        super(cause);
    }

}
