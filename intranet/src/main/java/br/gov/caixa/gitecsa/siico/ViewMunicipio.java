package br.gov.caixa.gitecsa.siico;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Immutable
@Table(name = "\"icovw056_sired_municipio\"", schema = Constantes.SCHEMA_SIICODB_NAME)
public class ViewMunicipio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nu_localidade", columnDefinition = "int4")
    private Long nuLocalidade;

    @Column(name = "no_localidade", columnDefinition = "bpchar")
    private String noLocalidade;

    @Column(name = "co_tipo", columnDefinition = "bpchar")
    private String coTipo;

    @Column(name = "sg_uf", columnDefinition = "bpchar")
    private String sgUf;

    public String getCoTipo() {
        return this.coTipo;
    }

    public void setCoTipo(String coTipo) {
        this.coTipo = coTipo;
    }

    public String getNoLocalidade() {
        return this.noLocalidade;
    }

    public void setNoLocalidade(String noLocalidade) {
        this.noLocalidade = noLocalidade;
    }

    public Long getNuLocalidade() {
        return this.nuLocalidade;
    }

    public void setNuLocalidade(Long nuLocalidade) {
        this.nuLocalidade = nuLocalidade;
    }

    public String getSgUf() {
        return this.sgUf;
    }

    public void setSgUf(String sgUf) {
        this.sgUf = sgUf;
    }

}
