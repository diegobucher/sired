package br.gov.caixa.gitecsa.sired.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CTDAUFPK implements Serializable {

    private static final long serialVersionUID = 2236778050513917011L;

    @Column(name = "nu_ctda_c10", columnDefinition = "int2")
    private Integer idCtda;

    @Column(name = "nu_uf_a03")
    private Integer idUf;

    public Integer getIdCtda() {
        return idCtda;
    }

    public void setIdCtda(Integer idCtda) {
        this.idCtda = idCtda;
    }

    public Integer getIdUf() {
        return idUf;
    }

    public void setIdUf(Integer idUf) {
        this.idUf = idUf;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CTDAUFPK)) {
            return false;
        }
        CTDAUFPK castOther = (CTDAUFPK) other;
        return this.idCtda.equals(castOther.idCtda) && this.idUf.equals(castOther.idUf);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.idCtda.hashCode();
        hash = hash * prime + this.idUf.hashCode();

        return hash;
    }
}
