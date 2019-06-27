package br.gov.caixa.gitecsa.sired.arquitetura.exception;

import java.util.List;

public class RequisicaoLoteException extends BusinessException {

    private static final long serialVersionUID = 1L;
    
    private Integer NumeroRejeitadas;
    
    public RequisicaoLoteException(List<String> erroList) {
        super(erroList);
    }

    public Integer getNumeroRejeitados() {
        return NumeroRejeitadas;
    }

    public void setNumeroRejeitadas(Integer numeroRejeitadas) {
        NumeroRejeitadas = numeroRejeitadas;
    }

}
