package br.gov.caixa.gitecsa.sired.enumerator;

public enum GrupoDocumentoEnum {

    CHEQUE_COMPENSADO ("Cheque Compensado"), CHEQUE_BOCA_DE_CAIXA ("Cheque Boca de Caixa"), EXTRATO ("Extrato");

    private String label;

    private GrupoDocumentoEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
