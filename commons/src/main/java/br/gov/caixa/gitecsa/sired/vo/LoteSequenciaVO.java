package br.gov.caixa.gitecsa.sired.vo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba14_lote_sequencia", schema = Constantes.SCHEMADB_NAME)
public class LoteSequenciaVO extends BaseEntity implements Comparable<LoteSequenciaVO> {
    private static final long serialVersionUID = 4272613241526247090L;
    
    private static final String ORDER_BY_ID = "id";

    @Id
    @Column(name = "nu_lote_sequencia", columnDefinition = "bpchar")
    private String id;

    @ManyToMany(mappedBy = "loteSequencia", fetch = FetchType.LAZY)
    private List<BaseVO> bases;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (String) id;
    }

    @Override
    public String getColumnOrderBy() {
        return ORDER_BY_ID;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public List<BaseVO> getBases() {
        return bases;
    }

    public void setBases(List<BaseVO> bases) {
        this.bases = bases;
    }

    @Override
    public int compareTo(LoteSequenciaVO o) {
        return this.id.compareToIgnoreCase(o.id);
    }

    @Override
    public String toString() {
        return this.id;
    }
}
