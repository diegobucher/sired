package br.gov.caixa.gitecsa.sired.enumerator;


public enum TipoUnidadeEnum {
	
	DC("DIRETORIA COLEGIADA",1),
	DI("DIRETORIA DE FUNDOS",12),
	GA("GERENCIA DE AREA",2),
	GE("GERENCIA EXECUTIVA",3),
	GI("GERENCIA DE FILIAL",14),
	SU("SUPERVISAO AVANCADA",18),
	RE("REPRESENTACAO",16),
	EN("ESCRITORIO DE NEGOCIO",6),
	PV("PONTO DE VENDA",8),
	CI("CONTROLE INTERNO",19),
	PAB("POSTO DE ATENDIMENTO BANCARIO",9 ),
	PAA("POSTO DE ATENDIMENTO AVANCADO",10),  
	TE("TESOURARIA",11);  
	
	
	private String descricao;
	private Integer valor;
	
	private TipoUnidadeEnum(String descricao, Integer valor){
		this.descricao = descricao;
		this.valor = valor;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Integer getValor() {
		return valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}
	
    
   
}
