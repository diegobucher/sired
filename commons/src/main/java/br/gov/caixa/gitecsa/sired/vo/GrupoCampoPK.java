package br.gov.caixa.gitecsa.sired.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class GrupoCampoPK implements Serializable {

    private static final long serialVersionUID = -749757492477906068L;

    @Column(name = "nu_grupo_c02", columnDefinition = "int4")
    private Long idGrupo;

    @Column(name = "nu_campo_a01", columnDefinition = "int4")
    private Long idCampo;

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(final Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Long getIdCampo() {
        return idCampo;
    }

    public void setIdCampo(final Long idCampo) {
        this.idCampo = idCampo;
    }
    
}
