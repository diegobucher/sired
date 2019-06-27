package br.gov.caixa.gitecsa.sired.vo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba08_municipio", schema = Constantes.SCHEMADB_NAME)
public class MunicipioVO extends BaseEntity {

    private static final long serialVersionUID = 1237568264388213827L;

    @Id
    @Column(name = "nu_municipio", columnDefinition = "int2")
    private Long id;

    @Column(name = "no_municipio")
    private String nome;

    @OneToMany(mappedBy = "municipio", fetch = FetchType.LAZY)
    private List<FeriadoVO> feriados;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sg_uf_a03", columnDefinition = "bpchar")
    private UFVO uf;

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

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<FeriadoVO> getFeriados() {
        return feriados;
    }

    public void setFeriados(List<FeriadoVO> feriados) {
        this.feriados = feriados;
    }

    public UFVO getUf() {
        return uf;
    }

    public void setUf(UFVO uf) {
        this.uf = uf;
    }
    
    @Deprecated
    public boolean equalsViewSiico(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MunicipioVO other = (MunicipioVO) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (nome == null) {
            if (other.nome != null)
                return false;
        } else if (other.nome == null) {
            return false;
        } else if (!nome.trim().equals(other.nome.trim()))
            return false;
        if (uf == null) {
            if (other.uf != null)
                return false;
        } else if (!uf.equals(other.uf))
            return false;
        return true;
    }

    public int hashCodeSync() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((nome == null) ? 0 : nome.hashCode());
        result = prime * result + ((uf == null) ? 0 : uf.getId().hashCode());
        return result;
    }

    public boolean equalsForSync(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof MunicipioVO))
            return false;
        MunicipioVO other = (MunicipioVO) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (nome == null) {
            if (other.nome != null)
                return false;
        } else if (!nome.equals(other.nome))
            return false;
        if (uf == null) {
            if (other.uf != null)
                return false;
        } else if (!uf.equals(other.uf))
            return false;
        return true;
    }

}
