package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc09_base_lote_seq", schema = Constantes.SCHEMADB_NAME)
public class BaseLoteSeqVO extends BaseEntity {

    private static final long serialVersionUID = -5446523870722432929L;

    @EmbeddedId
    private BaseLoteSeqPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_base_c08", insertable = false, updatable = false)
    private BaseVO base;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_base_c08", insertable = false, updatable = false)
    private LoteSequenciaVO loteSequencia;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (BaseLoteSeqPK) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public LoteSequenciaVO getLoteSequencia() {
        return loteSequencia;
    }

    public void setLoteSequencia(LoteSequenciaVO loteSequencia) {
        this.loteSequencia = loteSequencia;
    }

}
