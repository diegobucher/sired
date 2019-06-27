package br.gov.caixa.gitecsa.sired.enumerator;

public enum TipoRelatorioFaturamentoEnum {
    
    REQUISICAO("Requisição"),
    REMESSA_AB("Remessa Tipo A/B"),
    REMESSA_C("Remessa Tipo C");
    
    private String label;
    
    private TipoRelatorioFaturamentoEnum(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}
