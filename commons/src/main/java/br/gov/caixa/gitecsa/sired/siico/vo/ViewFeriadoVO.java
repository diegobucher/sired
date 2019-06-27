package br.gov.caixa.gitecsa.sired.siico.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Immutable;

import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Immutable
@Table(name = "\"icovw055_sired_feriado\"", schema = Constantes.SCHEMA_SIICODB_NAME)
public class ViewFeriadoVO implements Serializable {

    private static final long serialVersionUID = -1733542708845603608L;

    @Id
    @Column(name = "nu_feriado", columnDefinition = "int4")
    private Long nuFeriado;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_feriado", columnDefinition = "date")
    private Date dtFeriado;

    @Column(name = "ic_modalidade", columnDefinition = "int2")
    private Integer icModalidade;

    @Column(name = "nu_localidade", columnDefinition = "int4")
    private Long nuLocalidade;

    @Column(name = "sg_uf", columnDefinition = "bpchar")
    private String sgUf;

    @Column(name = "ts_inclusao", columnDefinition = "date")
    private Date tsInclusao;

    public Date getDtFeriado() {
        return this.dtFeriado;
    }

    public void setDtFeriado(Date dtFeriado) {
        this.dtFeriado = dtFeriado;
    }

    public Integer getIcModalidade() {
        return this.icModalidade;
    }

    public void setIcModalidade(Integer icModalidade) {
        this.icModalidade = icModalidade;
    }

    public Long getNuFeriado() {
        return this.nuFeriado;
    }

    public void setNuFeriado(Long nuFeriado) {
        this.nuFeriado = nuFeriado;
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

    public Timestamp getTsInclusao() {
        return new Timestamp(this.tsInclusao.getTime());
    }

    public void setTsInclusao(Date tsInclusao) {
        this.tsInclusao = tsInclusao;
    }

}
