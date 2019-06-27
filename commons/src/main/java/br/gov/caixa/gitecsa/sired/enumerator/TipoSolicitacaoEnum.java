package br.gov.caixa.gitecsa.sired.enumerator;

import org.apache.commons.lang.StringUtils;

public enum TipoSolicitacaoEnum {

    REQUISICAO(0, "Requisição"), REMESSA(1, "Remessa"), REQUISICAO_REMESSA(2, "Requisição e Remessa");

    private Integer valor;
    private String descricao;

    private TipoSolicitacaoEnum(Integer valor, String descricao) {
        this.descricao = descricao;
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public Integer recreateString() {
        return valor;
    }

    public String getDescricaoByValor(String valor) {

        for (TipoSolicitacaoEnum tipo : TipoSolicitacaoEnum.values()) {
            if (tipo.getValor().equals(valor)) {
                return tipo.getDescricao();
            }
        }

        return StringUtils.EMPTY;
    }

}
