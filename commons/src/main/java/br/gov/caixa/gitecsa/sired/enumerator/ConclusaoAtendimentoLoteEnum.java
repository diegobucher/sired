package br.gov.caixa.gitecsa.sired.enumerator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum ConclusaoAtendimentoLoteEnum implements BaseEnumNumberValue {

    SIM(0L, "Sim"), NAO(1L, "Não"), COM_ERROS(2L, "Erro");
    
    private Long id;
    
    private String label;

    private ConclusaoAtendimentoLoteEnum(Long id, String label) {
        this.id = id;
        this.label = label;
    }
    
    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public Long getValue() {
        return this.id;
    }
}
