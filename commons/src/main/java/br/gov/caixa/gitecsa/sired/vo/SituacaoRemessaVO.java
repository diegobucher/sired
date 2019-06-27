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
@Table(name="redtba13_situacao_remessa",schema = Constantes.SCHEMADB_NAME)
public class SituacaoRemessaVO extends BaseEntity{

	private static final long serialVersionUID = 8216745238368599371L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="nu_situacao_remessa", nullable = false, columnDefinition = "int2")
	private Long id;
	
	@Column(name="no_situacao_remessa", nullable = false, length = 30)
	private String nome;
	
	@Column(name="de_situacao_remessa", nullable = true, length = 100)
	private String descricao;
	
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
		return "nome";
	}

	@Override
	public String getAuditoria() {
		return null;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
