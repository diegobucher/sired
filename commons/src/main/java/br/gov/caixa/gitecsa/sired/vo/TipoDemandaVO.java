package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba04_tipo_demanda", schema = Constantes.SCHEMADB_NAME)
public class TipoDemandaVO extends BaseEntity implements Comparable<TipoDemandaVO> {

    private static final long serialVersionUID = 6333758627363199721L;
    
    private static final String ORDER_BY_NOME = "nome";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_tipo_demanda", columnDefinition = "serial")
    private Long id;

    @Column(name = "de_documento_exigido")
    private String descricaoDocumentoExigido;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_setorial", columnDefinition = "int2")
    private TipoDocumentoEnum icSetorial;

    @Column(name = "no_tipo_demanda")
    private String nome;

    @Column(name = "nu_prazo_dias", columnDefinition = "int2")
    private Integer prazoDias;
    
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
        return ORDER_BY_NOME;
    }

    @Override
    public String getAuditoria() {
        return null;
    }
   
    public String getDescricaoDocumentoExigido() {
        return descricaoDocumentoExigido;
    }

    public void setDescricaoDocumentoExigido(String descricaoDocumentoExigido) {
        this.descricaoDocumentoExigido = descricaoDocumentoExigido;
    }

    public TipoDocumentoEnum getIcSetorial() {
        return icSetorial;
    }

    public void setIcSetorial(TipoDocumentoEnum icSetorial) {
        this.icSetorial = icSetorial;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

	public Integer getPrazoDias() {
        return prazoDias;
    }

    public void setPrazoDias(Integer prazoDias) {
        this.prazoDias = prazoDias;
    }

    @Override
    public int compareTo(TipoDemandaVO o) {
        return this.nome.compareToIgnoreCase(o.nome);
    }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s]", getClass().getSimpleName(),getId().toString());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((icSetorial == null) ? 0 : icSetorial.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((nome == null) ? 0 : nome.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TipoDemandaVO other = (TipoDemandaVO) obj;
        if (icSetorial != other.icSetorial)
            return false;
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
        return true;
    }
}
