package br.gov.caixa.gitecsa.sired.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UnidadeGrupoOperacaoPK implements Serializable {

    private static final long serialVersionUID = -2708175422854949597L;

    @Column(name = "nu_unidade_grupo")
    private Integer nuUnidadeGrupo;

    @Column(name = "nu_operacao_a11", columnDefinition = "bpchar")
    private String nuOperacaoA11;

    public UnidadeGrupoOperacaoPK() {
    }

    public Integer getNuUnidadeGrupo() {
        return this.nuUnidadeGrupo;
    }

    public void setNuUnidadeGrupo(Integer nuUnidadeGrupo) {
        this.nuUnidadeGrupo = nuUnidadeGrupo;
    }

    public String getNuOperacaoA11() {
        return this.nuOperacaoA11;
    }

    public void setNuOperacaoA11(String nuOperacaoA11) {
        this.nuOperacaoA11 = nuOperacaoA11;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UnidadeGrupoOperacaoPK)) {
            return false;
        }
        UnidadeGrupoOperacaoPK castOther = (UnidadeGrupoOperacaoPK) other;
        return this.nuUnidadeGrupo.equals(castOther.nuUnidadeGrupo) && this.nuOperacaoA11.equals(castOther.nuOperacaoA11);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.nuUnidadeGrupo.hashCode();
        hash = hash * prime + this.nuOperacaoA11.hashCode();

        return hash;
    }
}
