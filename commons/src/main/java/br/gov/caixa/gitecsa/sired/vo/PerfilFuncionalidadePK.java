package br.gov.caixa.gitecsa.sired.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PerfilFuncionalidadePK implements Serializable {

    private static final long serialVersionUID = -7054886200498514597L;

    @Column(name = "nu_perfil_s05", columnDefinition = "int2")
    private Long nuPerfil;

    @Column(name = "nu_funcionalidade_s06", columnDefinition = "int2")
    private Long nuFuncionalidade;

    public Long getNuPerfil() {
        return nuPerfil;
    }

    public void setNuPerfil(Long nuPerfil) {
        this.nuPerfil = nuPerfil;
    }

    public Long getNuFuncionalidade() {
        return nuFuncionalidade;
    }

    public void setNuFuncionalidade(Long nuFuncionalidade) {
        this.nuFuncionalidade = nuFuncionalidade;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PerfilFuncionalidadePK)) {
            return false;
        }
        PerfilFuncionalidadePK castOther = (PerfilFuncionalidadePK) other;
        return this.nuPerfil.equals(castOther.nuPerfil) && this.nuFuncionalidade.equals(castOther.nuFuncionalidade);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.nuPerfil.hashCode();
        hash = hash * prime + this.nuFuncionalidade.hashCode();

        return hash;
    }
}
