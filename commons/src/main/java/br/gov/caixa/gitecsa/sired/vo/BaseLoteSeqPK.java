package br.gov.caixa.gitecsa.sired.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class BaseLoteSeqPK implements Serializable {

    private static final long serialVersionUID = 6107309858590445009L;

    @Column(name = "nu_base_c08", columnDefinition = "int2")
    private Integer idBase;

    @Column(name = "nu_lote_sequencia_a14", columnDefinition = "bpchar")
    private Integer idLoteSequencia;

    public Integer getIdBase() {
        return idBase;
    }

    public void setIdBase(Integer idBase) {
        this.idBase = idBase;
    }

    public Integer getIdLoteSequencia() {
        return idLoteSequencia;
    }

    public void setIdLoteSequencia(Integer idLoteSequencia) {
        this.idLoteSequencia = idLoteSequencia;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BaseLoteSeqPK)) {
            return false;
        }
        
        BaseLoteSeqPK castOther = (BaseLoteSeqPK) other;
        return this.idBase.equals(castOther.idBase) && this.idLoteSequencia.equals(castOther.idLoteSequencia);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.idBase.hashCode();
        hash = hash * prime + this.idLoteSequencia.hashCode();

        return hash;
    }
}
