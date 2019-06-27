package br.gov.caixa.gitecsa.sired.siico.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Immutable
@Table(name = "\"icovw054_sired_unidade\"", schema = Constantes.SCHEMA_SIICODB_NAME)
public class ViewUnidadeVO implements Serializable {

    private static final long serialVersionUID = 6654242427281946472L;

    @Id
    @Column(name = "nu_unidade", columnDefinition = "int2")
    private Long nuUnidade;

    @Column(name = "no_unidade")
    private String noUnidade;

    @Column(name = "nu_tp_unidade", columnDefinition = "int2")
    private Long nuTpUnidade;

    @Column(name = "sg_unidade", columnDefinition = "bpchar")
    private String sgUnidade;

    @Column(name = "nu_unidade_sub", columnDefinition = "int2")
    private Long nuUnidadeSub;

    @Column(name = "nu_localidade", columnDefinition = "int4")
    private Long nuLocalidade;

    @Column(name = "sg_localizacao", columnDefinition = "bpchar")
    private String sgLocalizacao;

    @Column(name = "sigla_tipo", columnDefinition = "bpchar")
    private String siglaTipo;

    @Column(name = "desc_tipo", columnDefinition = "bpchar")
    private String descTipo;

    @Column(name = "area_tipo", columnDefinition = "bpchar")
    private String areaTipo;

    @Column(name = "uf", columnDefinition = "bpchar")
    private String uf;

    public String getAreaTipo() {
        return this.areaTipo;
    }

    public void setAreaTipo(String areaTipo) {
        this.areaTipo = areaTipo;
    }

    public String getDescTipo() {
        return this.descTipo;
    }

    public void setDescTipo(String descTipo) {
        this.descTipo = descTipo;
    }

    public String getNoUnidade() {
        return this.noUnidade;
    }

    public void setNoUnidade(String noUnidade) {
        this.noUnidade = noUnidade;
    }

    public Long getNuTpUnidade() {
        return this.nuTpUnidade;
    }

    public void setNuTpUnidade(Long nuTpUnidade) {
        this.nuTpUnidade = nuTpUnidade;
    }

    public Long getNuUnidade() {
        return this.nuUnidade;
    }

    public void setNuUnidade(Long nuUnidade) {
        this.nuUnidade = nuUnidade;
    }

    public Long getNuUnidadeSub() {
        return this.nuUnidadeSub;
    }

    public void setNuUnidadeSub(Long nuUnidadeSub) {
        this.nuUnidadeSub = nuUnidadeSub;
    }

    public String getSgLocalizacao() {
        return this.sgLocalizacao;
    }

    public void setSgLocalizacao(String sgLocalizacao) {
        this.sgLocalizacao = sgLocalizacao;
    }

    public String getSgUnidade() {
        return this.sgUnidade;
    }

    public void setSgUnidade(String sgUnidade) {
        this.sgUnidade = sgUnidade;
    }

    public String getSiglaTipo() {
        return this.siglaTipo;
    }

    public void setSiglaTipo(String siglaTipo) {
        this.siglaTipo = siglaTipo;
    }

    public String getUf() {
        return this.uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public Long getNuLocalidade() {
        return nuLocalidade;
    }

    public void setNuLocalidade(Long nuLocalidade) {
        this.nuLocalidade = nuLocalidade;
    }

}
