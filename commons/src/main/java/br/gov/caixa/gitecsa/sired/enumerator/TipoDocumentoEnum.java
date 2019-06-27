package br.gov.caixa.gitecsa.sired.enumerator;


public enum TipoDocumentoEnum {
	
	SETORIAL("0", "Setorial"),
	NAOSETORIAL("1", "Não Setorial");
	
	private String valor;
	private String descricao;
	
	private TipoDocumentoEnum(String valor, String descricao){
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
    
    public String getDescricaoByValor(String valor){
    	
    	for (TipoDocumentoEnum tipo : TipoDocumentoEnum.values()) {
    		if(tipo.getValor().equalsIgnoreCase(valor)){
    			return tipo.getDescricao();
    		}
		}
    	
    	return "";
    	
    }
	
	
}
