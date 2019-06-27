package br.gov.caixa.gitecsa.sired.enumerator;

public enum UnidadeParametroSistemaEnum {

    NENHUM(0, "Nenhum"), 
    HORAS(1, "Hora(s)"), 
    MINUTOS(2, "Minuto(s)"), 
    SEGUNDOS(3, "Segundo(s)"), 
    DIAS(4, "Dia(s)"), 
    MESES(5, "Mês(es)"),
    ANOS(6, "Ano(s)"),
    BOOLEAN(7, "Booleano");
    
    private Integer value;
    
    private String label;
    
    private UnidadeParametroSistemaEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getLabel() {
        return label;
    }
}
