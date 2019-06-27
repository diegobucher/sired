package br.gov.caixa.gitecsa.sired.enumerator;



public enum TipoAgrupamentoDocumentoEnum  {
	
	NENHUM("0", "Nenhum"),
	MENSAL("1", "Mensal"),
	ANUAL("2", "Anual");
	
	private String valor;
	private String descricao;
	
	private TipoAgrupamentoDocumentoEnum(String valor, String descricao){
		this.descricao = descricao;
		this.valor = valor;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
	
    
    public String recreateString() {  
        return valor;  
    }
	
	
}
