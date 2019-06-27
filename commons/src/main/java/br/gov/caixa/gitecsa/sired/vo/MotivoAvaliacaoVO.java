package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba12_motivo_avaliacao", schema = Constantes.SCHEMADB_NAME)
public class MotivoAvaliacaoVO extends BaseEntity implements Comparable<MotivoAvaliacaoVO> {

    private static final long serialVersionUID = 1148392603351889889L;
    
    private static final String ORDER_BY_MOTIVO = "deMotivoAvaliacao";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_motivo_avaliacao", columnDefinition = "int2")
    private Long id;

    @Column(name = "de_motivo_avaliacao")
    private String deMotivoAvaliacao;

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
        return ORDER_BY_MOTIVO;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public String getDeMotivoAvaliacao() {
        return deMotivoAvaliacao;
    }

    public void setDeMotivoAvaliacao(String deMotivoAvaliacao) {
        this.deMotivoAvaliacao = deMotivoAvaliacao;
    }

    @Override
    public int compareTo(MotivoAvaliacaoVO o) {
        return this.deMotivoAvaliacao.compareToIgnoreCase(o.deMotivoAvaliacao);
    }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s]", getClass().getSimpleName(),getId().toString());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deMotivoAvaliacao == null) ? 0 : deMotivoAvaliacao.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        MotivoAvaliacaoVO other = (MotivoAvaliacaoVO) obj;
        if (deMotivoAvaliacao == null) {
            if (other.deMotivoAvaliacao != null)
                return false;
        } else if (!deMotivoAvaliacao.equals(other.deMotivoAvaliacao))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
