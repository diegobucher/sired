package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.UnidadeParametroSistemaEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbs01_parametro_sistema", schema = Constantes.SCHEMADB_NAME)
public class ParametroSistemaVO extends BaseEntity {

    private static final long serialVersionUID = 5683616953256674148L;

    @Id
    @Column(name = "no_parametro_sistema")
    private String noParametroSistema;

    @Column(name = "vl_parametro_sistema")
    private String vlParametroSistema;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_unidade_parametro_sistema", columnDefinition = "int2")
    private UnidadeParametroSistemaEnum icUnidadeParametroSistema;

    @Override
    public Object getId() {
        return null;
    }

    @Override
    public void setId(Object id) {
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public String getNoParametroSistema() {
        return noParametroSistema;
    }

    public void setNoParametroSistema(String noParametroSistema) {
        this.noParametroSistema = noParametroSistema;
    }

    public String getVlParametroSistema() {
        return vlParametroSistema;
    }

    public void setVlParametroSistema(String vlParametroSistema) {
        this.vlParametroSistema = vlParametroSistema;
    }

    public UnidadeParametroSistemaEnum getIcUnidadeParametroSistema() {
        return icUnidadeParametroSistema;
    }

    public void setIcUnidadeParametroSistema(UnidadeParametroSistemaEnum icUnidadeParametroSistema) {
        this.icUnidadeParametroSistema = icUnidadeParametroSistema;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((noParametroSistema == null) ? 0 : noParametroSistema.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        ParametroSistemaVO other = (ParametroSistemaVO) obj;

        if (noParametroSistema == null) {
            if (other.noParametroSistema != null) {
                return false;
            }
        } else if (!noParametroSistema.equals(other.noParametroSistema)) {
            return false;
        }

        return true;
    }

}
