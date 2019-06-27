package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.AbrangenciaEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redvw002_abrangencia", schema = Constantes.SCHEMADB_NAME)
public class ViewAbrangenciaVO extends BaseEntity {

    private static final long serialVersionUID = 8539613618346597834L;

    @EmbeddedId
    private ViewAbrangenciaPK id;

    @Column(name = "base", columnDefinition = "int2", insertable = false, updatable = false)
    private Long idBase;

    @Column(name = "unid_base", columnDefinition = "int2", insertable = false, updatable = false)
    private Long idUnidadeBase;

    @Column(name = "unid_solicitada", columnDefinition = "int2", insertable = false, updatable = false)
    private Long idUnidadeSolicitada;

    @Column(name = "nome_ctda", insertable = false, updatable = false)
    private String nomeCtda;

    @Column(name = "ufs", columnDefinition = "bpchar", insertable = false, updatable = false)
    private String siglaUf;
    
    @Column(name = "ic_comando", columnDefinition = "int2", insertable = false, updatable = false)
    private String comando;

    @Column(name = "nu_empresa_contrato", columnDefinition = "int4", insertable = false, updatable = false)
    private String nuEmpresaContrato;

    @Enumerated(EnumType.STRING)
    @Column(name = "abrangencia", insertable = false, updatable = false)
    private AbrangenciaEnum abrangencia;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (ViewAbrangenciaPK) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public Long getIdBase() {
        return idBase;
    }

    public void setIdBase(final Long idBase) {
        this.idBase = idBase;
    }

    public Long getIdUnidadeBase() {
        return idUnidadeBase;
    }

    public void setIdUnidadeBase(final Long idUnidadeBase) {
        this.idUnidadeBase = idUnidadeBase;
    }

    public Long getIdUnidadeSolicitada() {
        return idUnidadeSolicitada;
    }

    public void setIdUnidadeSolicitada(final Long idUnidadeSolicitada) {
        this.idUnidadeSolicitada = idUnidadeSolicitada;
    }

    public String getNomeCtda() {
        return nomeCtda;
    }

    public void setNomeCtda(final String nomeCtda) {
        this.nomeCtda = nomeCtda;
    }

    public String getSiglaUf() {
        return siglaUf;
    }

    public void setSiglaUf(final String siglaUf) {
        this.siglaUf = siglaUf;
    }

    public String getComando() {
        return comando;
    }

    public void setComando(final String comando) {
        this.comando = comando;
    }

    public String getNuEmpresaContrato() {
        return nuEmpresaContrato;
    }

    public void setNuEmpresaContrato(String nuEmpresaContrato) {
        this.nuEmpresaContrato = nuEmpresaContrato;
    }

    public AbrangenciaEnum getAbrangencia() {
        return abrangencia;
    }

    public void setAbrangencia(final AbrangenciaEnum abrangencia) {
        this.abrangencia = abrangencia;
    }

}
