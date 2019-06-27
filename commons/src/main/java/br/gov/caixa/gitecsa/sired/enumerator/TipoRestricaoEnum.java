package br.gov.caixa.gitecsa.sired.enumerator;



public enum TipoRestricaoEnum  {
	
	INCLUIR("0", "Incluir", "somente"),
	EXCLUIR("1", "Excluir", "exceto");
	
	private String valor;
	private String descricao;
	private String  descricaoTela;
	
	private TipoRestricaoEnum(String valor, String descricao, String descricaoTela){
		this.descricao = descricao;
		this.valor = valor;
		this.setDescricaoTela(descricaoTela);
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

	public String getDescricaoTela() {
		return descricaoTela;
	}

	public void setDescricaoTela(String descricaoTela) {
		this.descricaoTela = descricaoTela;
	}
	
	
}
