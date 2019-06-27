package br.gov.caixa.gitecsa.sired.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UnidadeGrupoPK implements Serializable {

    private static final long serialVersionUID = -7765789194446312532L;

    @Column(name = "nu_unidade_a02")
    private Integer idUnidade;

    @Column(name = "nu_grupo_c02")
    private Integer idGrupo;
    
    public Integer getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Integer idUnidade) {
        this.idUnidade = idUnidade;
    }

    public Integer getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Integer idGrupo) {
        this.idGrupo = idGrupo;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof UnidadeGrupoPK)) {
            return false;
        }
        UnidadeGrupoPK castOther = (UnidadeGrupoPK) other;
        return this.idUnidade.equals(castOther.idUnidade) && this.idGrupo.equals(castOther.idGrupo);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.idUnidade.hashCode();
        hash = hash * prime + this.idGrupo.hashCode();

        return hash;
    }

}
