package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;

@Entity
@Table(name = "redtbc13_empresa_contrato", schema = Constantes.SCHEMADB_NAME)
public class EmpresaContratoVO extends BaseEntity {

    private static final long serialVersionUID = -6343998486754182654L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_empresa_contrato", columnDefinition = "serial")
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_fim_vigencia")
    private Date dataFimVigencia;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_inicio_vigencia")
    private Date dataInicioVigencia;

    @Column(name = "nu_contrato")
    private String numeroContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_base_c08", columnDefinition = "int2")
    private BaseVO base;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_empresa_c12", columnDefinition = "int2")
    private EmpresaVO empresa;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public Date getDataFimVigencia() {
        return dataFimVigencia;
    }

    public void setDataFimVigencia(Date dataFimVigencia) {
        this.dataFimVigencia = dataFimVigencia;
    }

    public Date getDataInicioVigencia() {
        return dataInicioVigencia;
    }

    public void setDataInicioVigencia(Date dataInicioVigencia) {
        this.dataInicioVigencia = dataInicioVigencia;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public EmpresaVO getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaVO empresa) {
        this.empresa = empresa;
    }
    
    @Transient
    public String getDataInicioVigenciaFormatado() {
        return DateUtils.format(this.dataInicioVigencia, DateUtils.DEFAULT_FORMAT);
    }
    
    @Transient
    public String getDataFimVigenciaFormatado() {
        return DateUtils.format(this.dataFimVigencia, DateUtils.DEFAULT_FORMAT);
    }

}
