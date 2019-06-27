package br.gov.caixa.gitecsa.sired.arquitetura.exception;

import java.util.List;

public class ArquivoLoteValidationException extends AppException {

    private static final long serialVersionUID = -5916866998120970055L;
    
    private List<String> erroList;

    public ArquivoLoteValidationException() {
        super();
    }

    public ArquivoLoteValidationException(String mensagem) {
        super(mensagem);
    }

    public ArquivoLoteValidationException(List<String> erroList) {
        this.erroList = erroList;
    }

    public ArquivoLoteValidationException(String mensagem, List<String> erroList) {
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
