package br.gov.caixa.gitecsa.sired.vo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba10_tipo_unidade", schema = Constantes.SCHEMADB_NAME)
public class TipoUnidadeVO extends BaseEntity {

    private static final long serialVersionUID = 376424100955207489L;

    public static final String INDICADOR_PONTO_VENDA = "PV";

    @Id
    @Column(name = "nu_tipo_unidade", columnDefinition = "int2")
    private Long id;

    @Column(name = "de_tipo_unidade")
    private String descricao;

    @Column(name = "ic_unidade", columnDefinition = "bpchar")
    private String indicadorUnidade;

    @Column(name = "sg_tipo_unidade", columnDefinition = "bpchar")
    private String siglaTipoUnidade;

    @OneToMany(mappedBy = "tipoUnidade", fetch = FetchType.LAZY)
    private List<UnidadeVO> unidades;

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getIndicadorUnidade() {
        return indicadorUnidade;
    }

    public void setIndicadorUnidade(String indicadorUnidade) {
        this.indicadorUnidade = indicadorUnidade;
    }

    public String getSiglaTipoUnidade() {
        return siglaTipoUnidade;
    }

    public void setSiglaTipoUnidade(String siglaTipoUnidade) {
        this.siglaTipoUnidade = siglaTipoUnidade;
    }

    public List<UnidadeVO> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<UnidadeVO> unidades) {
        this.unidades = unidades;
    }

    public boolean equalsViewSiico(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TipoUnidadeVO other = (TipoUnidadeVO) obj;
        if (descricao == null) {
            if (other.descricao != null)
                return false;
        } else if (!descricao.trim().equals(other.descricao.trim()))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (indicadorUnidade == null) {
            if (other.indicadorUnidade != null)
                return false;
        } else if (!indicadorUnidade.trim().equals(other.indicadorUnidade.trim()))
            return false;
        if (siglaTipoUnidade == null) {
            if (other.siglaTipoUnidade != null)
                return false;
        } else if (other.siglaTipoUnidade == null) {
            return false;
        } else if (!siglaTipoUnidade.trim().equals(other.siglaTipoUnidade.trim()))
            return false;
        return true;
    }

}
