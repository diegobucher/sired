package br.gov.caixa.gitecsa.sired.enumerator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum TipoCampoEnum implements BaseEnumNumberValue {

    TEXTO(0L, "Texto"), NUMERICO(1L, "Numérico"), DATA(2L, "Data"), BOOLEANO(3L, "Booleano");

    private Long id;

    private String descricao;

    private TipoCampoEnum(Long id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public Long getValue() {
        return id;
    }

}
