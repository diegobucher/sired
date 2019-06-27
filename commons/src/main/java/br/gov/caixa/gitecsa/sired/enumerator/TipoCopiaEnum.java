package br.gov.caixa.gitecsa.sired.enumerator;



public enum TipoCopiaEnum  {
		
	AUTENTICADA("0", "Autenticada"),
	NAO_AUTENTICADA("1", "Não autenticada");
	
	private String valor;
	private String descricao;
	
	private TipoCopiaEnum(String valor, String descricao){
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
