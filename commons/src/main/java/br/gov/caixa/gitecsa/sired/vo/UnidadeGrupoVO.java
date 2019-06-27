package br.gov.caixa.gitecsa.sired.vo;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc06_unidade_grupo", schema = Constantes.SCHEMADB_NAME)
public class UnidadeGrupoVO extends BaseEntity implements Comparable<UnidadeGrupoVO> {

    private static final long serialVersionUID = -5468992534158760532L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_unidade_grupo", columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_unidade_a02", columnDefinition = "int2")
    private UnidadeVO unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_grupo_c02", columnDefinition = "int4")
    private GrupoVO grupo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "redtbc03_unidade_grupo_operacao", schema = Constantes.SCHEMADB_NAME, 
    joinColumns = { @JoinColumn(name = "nu_unidade_grupo") }, 
    inverseJoinColumns = { @JoinColumn(name = "nu_operacao_a11") })
    private Set<OperacaoVO> operacao;

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

    public UnidadeVO getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeVO unidade) {
        this.unidade = unidade;
    }

    public GrupoVO getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoVO grupo) {
        this.grupo = grupo;
    }

    public Set<OperacaoVO> getOperacao() {
        return operacao;
    }

    public void setOperacao(Set<OperacaoVO> operacao) {
        this.operacao = operacao;
    }

    @Override
    public int compareTo(UnidadeGrupoVO o) {
        return this.getGrupo().getNome().compareToIgnoreCase(o.getGrupo().getNome());
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((grupo == null) ? 0 : grupo.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((unidade == null) ? 0 : unidade.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnidadeGrupoVO other = (UnidadeGrupoVO) obj;
		if (grupo == null) {
			if (other.grupo != null)
				return false;
		} else if (!grupo.equals(other.grupo))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (unidade == null) {
			if (other.unidade != null)
				return false;
		} else if (!unidade.equals(other.unidade))
			return false;
		return true;
	}

}
