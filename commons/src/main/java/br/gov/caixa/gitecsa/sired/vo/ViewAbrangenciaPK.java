package br.gov.caixa.gitecsa.sired.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ViewAbrangenciaPK implements Serializable {

    private static final long serialVersionUID = 5671117023382500358L;

    @Column(name = "pk1", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer pk1;

    @Column(name = "pk2", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer pk2;

    @Column(name = "pk3", columnDefinition = "int2", insertable = false, updatable = false)
    private Integer pk3;

    public Integer getPk1() {
        return pk1;
    }

    public void setPk1(final Integer pk1) {
        this.pk1 = pk1;
    }

    public Integer getPk2() {
        return pk2;
    }

    public void setPk2(final Integer pk2) {
        this.pk2 = pk2;
    }

    public Integer getPk3() {
        return pk3;
    }

    public void setPk3(final Integer pk3) {
        this.pk3 = pk3;
    }

}
