package br.gov.caixa.gitecsa.sired.enumerator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum SimNaoEnum implements BaseEnumNumberValue {

    NAO(0L, "Não"), SIM(1L, "Sim");

    private Long id;

    private String descricao;

    private SimNaoEnum(Long id, String descricao) {
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
