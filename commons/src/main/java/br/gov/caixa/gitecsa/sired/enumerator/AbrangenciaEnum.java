package br.gov.caixa.gitecsa.sired.enumerator;

public enum AbrangenciaEnum {

    NAO_PERTENCE("Não Pertence"), PERTENCE("Pertence");

    private String descricao;

    private AbrangenciaEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
}
