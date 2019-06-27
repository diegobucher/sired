package br.gov.caixa.gitecsa.siico;

import java.io.Serializable;
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
@Table(name = "\"icovw057_sired_tipo_unidade\"", schema = Constantes.SCHEMA_SIICODB_NAME)
public class ViewTipoUnidade implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2412705477914485547L;

    @Id
    @Column(name = "nu_tipo_unidade", columnDefinition = "int2")
    private Long nuTipoUnidade;

    @Column(name = "sg_tipo_unidade", columnDefinition = "bpchar")
    private String sgTipoUnidade;

    @Column(name = "de_tipo_unidade", columnDefinition = "bpchar")
    private String deTipoUnidade;

    @Column(name = "ic_unidade", columnDefinition = "bpchar")
    private String icUnidade;

    @Column(name = "ic_faixa_nu_undde", columnDefinition = "bpchar")
    private String icFaixaNuUnidade;

    @Column(name = "ic_origem_nu_undde", columnDefinition = "bpchar")
    private String icOrigemNuUnidade;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_inicio", columnDefinition = "date")
    private Date dtInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_fim", columnDefinition = "date")
    private Date dtFim;

    @Column(name = "co_sbssa_orgnl_u14", columnDefinition = "bpchar")
    private String coSbssaOrininal;

    public ViewTipoUnidade() {
    }

    public Long getNuTipoUnidade() {
        return nuTipoUnidade;
    }

    public void setNuTipoUnidade(Long nuTipoUnidade) {
        this.nuTipoUnidade = nuTipoUnidade;
    }

    public String getSgTipoUnidade() {
        return sgTipoUnidade;
    }

    public void setSgTipoUnidade(String sgTipoUnidade) {
        this.sgTipoUnidade = sgTipoUnidade;
    }

    public String getDeTipoUnidade() {
        return deTipoUnidade;
    }

    public void setDeTipoUnidade(String deTipoUnidade) {
        this.deTipoUnidade = deTipoUnidade;
    }

    public String getIcUnidade() {
        return icUnidade;
    }

    public void setIcUnidade(String icUnidade) {
        this.icUnidade = icUnidade;
    }

    public String getIcFaixaNuUnidade() {
        return icFaixaNuUnidade;
    }

    public void setIcFaixaNuUnidade(String icFaixaNuUnidade) {
        this.icFaixaNuUnidade = icFaixaNuUnidade;
    }

    public Date getDtInicio() {
        return dtInicio;
    }

    public void setDtInicio(Date dtInicio) {
        this.dtInicio = dtInicio;
    }

    public Date getDtFim() {
        return dtFim;
    }

    public void setDtFim(Date dtFim) {
        this.dtFim = dtFim;
    }

    public String getCoSbssaOrininal() {
        return coSbssaOrininal;
    }

    public void setCoSbssaOrininal(String coSbssaOrininal) {
        this.coSbssaOrininal = coSbssaOrininal;
    }

    public String getIcOrigemNuUnidade() {
        return icOrigemNuUnidade;
    }

    public void setIcOrigemNuUnidade(String icOrigemNuUnidade) {
        this.icOrigemNuUnidade = icOrigemNuUnidade;
    }
}
